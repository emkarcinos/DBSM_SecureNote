package emkarcinos.dbsm_securenote.backend

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


object Security {
    private const val hashAlgorithm = "SHA-512"
    val hashSize = MessageDigest.getInstance(hashAlgorithm).digestLength

    const val keystoreAlias = "securenote_rsa"

    // Used to cipher/decipher IV
    // ECB is safe here - we will encrypt only one block of data
    private val cipherAESECB: Cipher = Cipher.getInstance("AES/ECB/NoPadding")

    // Used to cipher/decipher the data
    private val cipherAESCBC: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    // Used to cipher/decipher passphrase by fingerpint
    val cipherRSA: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

    const val saltSize = 8

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private fun generatePBKDF(password: String, salt: String): String {
        val keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 10000, cipherAESCBC.blockSize * 8)
        val key = keyFac.generateSecret(keySpec)
        return key.encoded.toHex()
    }

    fun generateHash(message: String): String {
        val bytes = message.toByteArray()
        val hasher = MessageDigest.getInstance(hashAlgorithm)
        val digested = hasher.digest(bytes)
        return digested.toHex()
    }

    /**
     * Generates random salt value.
     *
     * The salt is generated in a following way:
     * A new array of bytes is created and filled with random values.
     * Then, this array is encoded to hex values.
     *
     * @return Salt as a string.
     */
    fun generateSalt(): String {
        val randomBytes = ByteArray(saltSize)

        val random = SecureRandom()
        random.nextBytes(randomBytes)

        return randomBytes.toHex()
    }

    /**
     * Encrypts the string data using AES/CBC method.
     * The key is hashed with PBKDF before encrypting the message.
     * The IV is generated randomly and encrypted together with the data
     * @param lines: input data
     * @param key: key as a string
     * @param salt: salt as a string
     * @return ByteArray with IV and encrypted data.
     */
    fun encryptString(lines: String, key: String, salt: String): ByteArray {
        val hashedPassword = generatePBKDF(key, salt).toByteArray()
        val dataBytes: ByteArray = lines.toByteArray()

        val iv = ByteArray(cipherAESCBC.blockSize)
        val random = SecureRandom()
        random.nextBytes(iv)
        val ivParameterSpec = IvParameterSpec(iv)

        val secretKeySpec = SecretKeySpec(hashedPassword, "AES")

        // Encrypt note
        cipherAESCBC.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encryptedData = cipherAESCBC.doFinal(dataBytes)

        // Encrypt IV
        cipherAESECB.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encryptedIv = cipherAESECB.doFinal(iv)

        val finalData = ByteArray(encryptedIv.size + encryptedData.size)
        System.arraycopy(encryptedIv, 0, finalData, 0, encryptedIv.size)
        System.arraycopy(encryptedData, 0, finalData, encryptedIv.size, encryptedData.size)

        return finalData
    }

    /**
     * Gets RSA key pair from AndroidKeyStore.
     * If the key dosen't exits, new one will be generated and returned.
     * @return KeyPair object
     */
    fun getOrCreateKeyFromKeystore(): KeyPair {
        val keystore = KeyStore.getInstance("AndroidKeyStore")
        keystore.load(null)
        val privateKey = keystore.getKey(keystoreAlias, null) as PrivateKey?
        val publicKey = if (privateKey != null) keystore.getCertificate(keystoreAlias)?.publicKey else null
        return if (privateKey != null && publicKey != null) KeyPair(publicKey, privateKey)
        else {
            val paramsBuilder = KeyGenParameterSpec.Builder(keystoreAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            paramsBuilder.apply {
                setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                setUserAuthenticationRequired(true)
                setRandomizedEncryptionRequired(false)
            }
            val keygen = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
            keygen.initialize(paramsBuilder.build())
            return keygen.generateKeyPair()
        }

    }

    /**
     * Attempts to decrypt the data.
     * The key is hashed with PBKDF before decrypting the message.
     * Data decryption may not be successful - if the keys don't match this may throw an exception,
     * or deciphered data will be wrong.
     *
     * Reverse method to encryptString.
     * @param bytes: data to decrypt
     * @param key: key as a string
     * @param salt: salt as a string
     * @return String with deciphered text
     */
    fun decryptToString(bytes: ByteArray, key: String, salt: String): String {
        val hashedPassword = generatePBKDF(key, salt).toByteArray()

        val iv = ByteArray(cipherAESCBC.blockSize)
        System.arraycopy(bytes, 0, iv, 0, iv.size)

        val encryptedBytes = ByteArray(bytes.size - iv.size)
        System.arraycopy(bytes, iv.size, encryptedBytes, 0, encryptedBytes.size)

        val secretKeySpec = SecretKeySpec(hashedPassword, "AES")

        // Decrypt IV
        cipherAESECB.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decryptedIv: ByteArray = cipherAESECB.doFinal(iv)

        val ivParameterSpec = IvParameterSpec(decryptedIv)

        // Decrypt data
        cipherAESCBC.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decryptedData: ByteArray = cipherAESCBC.doFinal(encryptedBytes)
        return String(decryptedData)
    }

    /**
     * Encrypts passphrase using RSA.
     * The key used in encryption is acquired from Android KeyStore
     * @param passphrase: plaintext passphrase
     * @param salt: salt as string
     * @return ByteArray with encrypted data
     */
    fun encryptPassphrase(passphrase: String, salt: String): ByteArray {
        val key: PublicKey = getOrCreateKeyFromKeystore().public
        cipherRSA.init(Cipher.ENCRYPT_MODE, key)
        cipherRSA.update(passphrase.toByteArray())
        cipherRSA.update(salt.toByteArray())

        return cipherRSA.doFinal()
    }

    /**
     * Decrypts the passphrase.
     * NOTE - cipher should be initialized by CyptoObject beofre decrypting!
     * @param data: Data to decrypt
     * @return decrypted passphrase
     */
    fun decryptPassphrase(data: ByteArray): String {
        val decrypted = cipherRSA.doFinal(data)
        return String(decrypted).dropLast(saltSize * 2)
    }
}
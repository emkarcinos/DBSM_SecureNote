package emkarcinos.dbsm_securenote.backend

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.Key
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


object Security {
    private const val hashAlgorithm = "SHA-512"
    val hashSize = MessageDigest.getInstance(hashAlgorithm).digestLength

    // Used to cipher/decipher IV
    // ECB is safe here - we will encrypt only one block of data
    private val cipherAESECB = Cipher.getInstance("AES/ECB/NoPadding")
    // Used to cipher/decipher the data
    private val cipherAESCBC = Cipher.getInstance("AES/CBC/PKCS5Padding")

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
    fun encryptString(lines: String, key: String, salt: String): ByteArray{
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

    fun getOrCreateKeyFromKeystore(keyAlias: String): String {
        val keystore = KeyStore.getInstance("AndroidKeyStore")
        keystore.load(null)
        val key = keystore.getKey(keyAlias, null)
        return if(key != null)
            key.encoded.toHex()
        else {
            val paramsBuilder = KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            paramsBuilder.apply {
                setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                setKeySize(256)
                setUserAuthenticationRequired(true)
            }
            val keygen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keygen.init(paramsBuilder.build())
            keygen.generateKey().encoded.toHex()
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
    fun decryptToString(bytes: ByteArray, key: String, salt: String): String{
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

}
package emkarcinos.dbsm_securenote.backend

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


object Security {
    const val hashAlgorithm = "SHA-256"
    val hashSize = MessageDigest.getInstance(hashAlgorithm).digestLength
    const val saltSize = 8

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    fun generatePBKDF(password: String, salt: String): String {
        val keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 10000, 256)
        val key = keyFac.generateSecret(keySpec)
        return key.encoded.toHex()
    }

    fun generateHash(message: String): String {
        val bytes = message.toByteArray()
        val hasher = MessageDigest.getInstance(hashAlgorithm)
        val digested = hasher.digest(bytes)
        return digested.toHex()
    }

    fun generateHashBytes(message: String): ByteArray {
        val bytes = message.toByteArray() 
        val hasher = MessageDigest.getInstance(hashAlgorithm)
        return hasher.digest(bytes)
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
     * The IV is generated randomly and encrypted together with the data.
     * @param lines: input data
     * @param key: key as a string
     * @return ByteArray with IV and encrypted data.
     */
    fun encryptString(lines: String, key: String): ByteArray{
        val dataBytes: ByteArray = lines.toByteArray()
        // Used to cipher the data
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        // Used to cipher IV
        // ECB is safe here - we will encrypt only one block of data
        val cipherECB = Cipher.getInstance("AES/ECB/NoPadding")

        val iv = ByteArray(cipher.blockSize)
        val random = SecureRandom()
        random.nextBytes(iv)
        val ivParameterSpec = IvParameterSpec(iv)

        var keyBytes = generateHashBytes(key)
        // trimming
        keyBytes = keyBytes.copyOf(cipher.blockSize)

        val secretKeySpec = SecretKeySpec(keyBytes, "AES")

        // Encrypt note
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encryptedData = cipher.doFinal(dataBytes)

        // Encrypt IV
        cipherECB.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encryptedIv = cipherECB.doFinal(iv)

        val finalData = ByteArray(encryptedIv.size + encryptedData.size)
        System.arraycopy(encryptedIv, 0, finalData, 0, encryptedIv.size)
        System.arraycopy(encryptedData, 0, finalData, encryptedIv.size, encryptedData.size)

        return finalData
    }

    /**
     * Attempts to decrypt the data.
     * Data decryption may not be successful - if the keys don't match this may throw an exception,
     * or deciphered data will be wrong.
     *
     * Reverse method to encryptString.
     * @param bytes: data to decrypt
     * @param key: key as a string
     * @return String with deciphered text
     */
    fun decryptToString(bytes: ByteArray, key: String): String {
        // Used to decipher the data
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        // Used to decipher IV
        val cipherECB = Cipher.getInstance("AES/ECB/NoPadding")

        val iv = ByteArray(cipher.blockSize)
        System.arraycopy(bytes, 0, iv, 0, iv.size)

        val encryptedBytes = ByteArray(bytes.size - iv.size)
        System.arraycopy(bytes, iv.size, encryptedBytes, 0, encryptedBytes.size)

        var keyBytes = generateHashBytes(key)
        // trimming
        keyBytes = keyBytes.copyOf(cipher.blockSize)

        val secretKeySpec = SecretKeySpec(keyBytes, "AES")

        // Decrypt IV
        cipherECB.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decryptedIv: ByteArray = cipherECB.doFinal(iv)

        val ivParameterSpec = IvParameterSpec(decryptedIv)

        // Decrypt data
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decryptedData: ByteArray = cipher.doFinal(encryptedBytes)
        return String(decryptedData)
    }
}
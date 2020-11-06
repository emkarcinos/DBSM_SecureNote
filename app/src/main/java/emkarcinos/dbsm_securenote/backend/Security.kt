package emkarcinos.dbsm_securenote.backend

import android.R.attr
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object Security {

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    fun generateHash(message: String): String {
        val bytes = message.toByteArray()
        val hasher = MessageDigest.getInstance("SHA-256")
        val digested = hasher.digest(bytes)
        return digested.toHex()
    }

    fun generateHashBytes(message: String): ByteArray {
        val bytes = message.toByteArray() 
        val hasher = MessageDigest.getInstance("SHA-256")
        return hasher.digest(bytes)
    }

    fun encryptString(lines: String, key: String): ByteArray{
        val clean: ByteArray = lines.toByteArray()

        // Generating IV.
        val ivSize = 16
        val iv = ByteArray(ivSize)
        val random = SecureRandom()
        random.nextBytes(iv)
        val ivParameterSpec = IvParameterSpec(iv)

        // Hashing key.
        var keyBytes = generateHashBytes(key)
        //trimming
        keyBytes = keyBytes.copyOf(16)

        val secretKeySpec = SecretKeySpec(keyBytes, "AES")

        // Encrypt.
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encrypted = cipher.doFinal(clean)

        // Combine IV and encrypted part.
        val encryptedIVAndText = ByteArray(ivSize + encrypted.size)
        System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize)
        System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.size)

        return encryptedIVAndText
    }

    fun decryptToString(bytes: ByteArray, key: String): String {
        val ivSize = 16
        val keySize = 16

        // Extract IV.
        val iv = ByteArray(ivSize)
        System.arraycopy(bytes, 0, iv, 0, iv.size)
        val ivParameterSpec = IvParameterSpec(iv)

        // Extract encrypted part.
        val encryptedSize: Int = bytes.size - ivSize
        val encryptedBytes = ByteArray(encryptedSize)
        System.arraycopy(bytes, ivSize, encryptedBytes, 0, encryptedSize)

        // Hash key.
        // Hashing key.
        var keyBytes = generateHashBytes(key)
        //trimming
        keyBytes = keyBytes.copyOf(16)

        val secretKeySpec = SecretKeySpec(keyBytes, "AES")

        // Decrypt.
        val cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decrypted = cipherDecrypt.doFinal(encryptedBytes)

        return String(decrypted)
    }
}
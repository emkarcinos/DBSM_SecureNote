package emkarcinos.dbsm_securenote.backend

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList

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

    fun encryptString(lines: String, key: String, iv: ByteArray): ByteArray{
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        var keyHash = generateHashBytes(key)
        //trimming
        keyHash = keyHash.copyOf(16)

        val secretKey = SecretKeySpec(keyHash, "AES")

        var ivHash = generateHashBytes(iv.toString())
        //trimming
        ivHash = ivHash.copyOf(16)
        val ivParameterSpec = IvParameterSpec(ivHash)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

        cipher.update(lines.toByteArray())

        return cipher.doFinal()
    }

    fun decryptToString(bytes: ByteArray, key: String, iv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        var keyHash = generateHashBytes(key)
        //trimming
        keyHash = keyHash.copyOf(16)

        val secretKey = SecretKeySpec(keyHash, "AES")

        var ivHash = generateHashBytes(iv.toString())
        //trimming
        ivHash = ivHash.copyOf(16)
        val ivParameterSpec = IvParameterSpec(ivHash)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

        cipher.update(bytes)

        return String(cipher.doFinal())
    }
}
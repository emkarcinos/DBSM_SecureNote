package emkarcinos.dbsm_securenote.backend

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


object Security {
    // Used to cipher/decipher the data
    val cipherAESCBC: Cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);

    /**
     * Encrypts the string data using AES/CBC method.
     * This method does not setup cipher object (dosen't call .init)
     * Make sure to do the initialization when building CryptoObject for biometrics.
     * @param lines: input data
     * @return ByteArray with IV and encrypted data.
     */
    fun encryptString(lines: String): ByteArray{
        val dataBytes: ByteArray = lines.toByteArray()
        val encryptedData = cipherAESCBC.doFinal(dataBytes)

        val encryptedIv = cipherAESCBC.iv

        val finalData = ByteArray(encryptedIv.size + encryptedData.size)
        System.arraycopy(encryptedIv, 0, finalData, 0, encryptedIv.size)
        System.arraycopy(encryptedData, 0, finalData, encryptedIv.size, encryptedData.size)

        return finalData
    }

    /**
     * Gets a key from AndroidKeyStore.
     * If a key dosen't exits, new one will be generated and returned.
     * @param keyAlias: Alias after which to look for a key
     * @return SecretKey object
     */
    fun getOrCreateKeyFromKeystore(keyAlias: String): SecretKey? {
        val keystore = KeyStore.getInstance("AndroidKeyStore")
        keystore.load(null)
        val key = keystore.getKey(keyAlias, null)
        return if(key != null) key as SecretKey
        else {
            val paramsBuilder = KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            paramsBuilder.apply {
                setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                setUserAuthenticationRequired(true)
                setRandomizedEncryptionRequired(false)
            }
            val keygen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keygen.init(paramsBuilder.build())
            return keygen.generateKey() as SecretKey
        }

    }

    /**
     * Decrypts a string from encrypted data.
     * This method does not setup cipher object (dosen't call .init)
     * Make sure to do the initialization when building CryptoObject for biometrics.
     * @param bytes: An array of raw encrypted data
     */
    fun decryptToString(bytes: ByteArray): String{
        val encryptedBytes = ByteArray(bytes.size - cipherAESCBC.blockSize)
        System.arraycopy(bytes, cipherAESCBC.blockSize, encryptedBytes, 0, encryptedBytes.size)

        val decryptedData: ByteArray = cipherAESCBC.doFinal(encryptedBytes)
        return String(decryptedData)
    }

}
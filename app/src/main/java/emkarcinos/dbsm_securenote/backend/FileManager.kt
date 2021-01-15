package emkarcinos.dbsm_securenote.backend

import java.io.*
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

object FileManager {
    // File pointing to the main data folder
    lateinit var appDirectory: File

    // Subdirectory name containing encrypted notes
    private const val notesFolderName: String = "notes"
    // Subdirectory name with user data
    private const val userdataFolderName: String = "user"

    private const val noteFileName : String = "note"
    // Pointers to subdirectories
    private lateinit var noteSubdirectory: File
    private lateinit var usersSubdirectory: File

    // File name contaning SHA512 hash with salt
    private const val hashedPasswordFileName: String = "login"
    // File name contaning RSA public key
    private const val rsaPublicKeyFileName: String = "rsa_pub"
    // File name contaning RSA encrypted secret
    private const val encryptedPassphraseFileName: String = "mk"

    private var isInit = false

    /**
     * Initializes the manager in a given directory
     * @param directory: Directory file
     */
    fun init(directory: File){
        if(!isInit){
            this.appDirectory = directory
            this.noteSubdirectory = File(directory, notesFolderName)
            this.usersSubdirectory = File(directory, userdataFolderName)

            when {!usersSubdirectory.exists() -> usersSubdirectory.mkdir()}
            when {!noteSubdirectory.exists() -> noteSubdirectory.mkdir()}


            isInit = true
        } else
            println("Alredy initialized!")
    }

    /**
     * Checks if the note already exists.
     * @return true, if exists. Otherwise false
     */
    fun noteExists(): Boolean {
        val file = File(noteSubdirectory, noteFileName)
        return file.exists()
    }

    /**
     * Checks whether the user has alredy created an account.
     * @return true, if it has. Otherwise false
     */
    fun userFileExists(): Boolean {
        val file = File(usersSubdirectory, hashedPasswordFileName)
        return file.exists()
    }

    fun saveUserData(user: User) {
        try {
            val hashFile = File(usersSubdirectory, hashedPasswordFileName)

            if(!hashFile.exists())
                hashFile.createNewFile()

            val hashPrinter = FileOutputStream(hashFile)
            hashPrinter.write(user.passwordHash.toByteArray())
            hashPrinter.write(user.salt.toByteArray())
            hashPrinter.close()

            if(user.hasFinerprint){
                //TODO: Get RSA public key, use it to encrypt the passphrase and save it to a file
            }

        } catch (e: IOException){
            e.printStackTrace()
        }
    }

    /**
     * Returns salt from a file.
     * @return salt as String
     */
    fun getSalt(): String {
        var salt: String = ""
        try {
            val hashFile = File(usersSubdirectory, hashedPasswordFileName)

            val stream = FileInputStream(hashFile)
            val bytes = ByteArray(hashFile.length().toInt())
            stream.read(bytes)
            salt = String(bytes).drop(Security.hashSize * 2)
        } catch (e: IOException){
            e.printStackTrace()
        }
        return salt
    }

    /**
     * Attempts to securely save a note.
     * Make sure to setup cipher object before!
     * @param note : string data
     */
    fun saveNote(note: String) {
        if(!isInit)
            throw ExceptionInInitializerError()

        try {
            val file = File(noteSubdirectory, noteFileName)

            if (!noteExists())
                file.createNewFile()

            val printer = FileOutputStream(file)

            //TODO val data = Security.encryptString(note)
            //printer.write(data)
            printer.close()
        } catch (e: IOException){
            e.printStackTrace()
        }
    }


    /**
     * Saves RSA public key to a file
     * @param key: RSA PublicKey
     */
    fun saveRSAPublicKey(key: PublicKey){
        if(!isInit)
            throw ExceptionInInitializerError()

        try {
            val file = File(usersSubdirectory, rsaPublicKeyFileName)

            if(!file.exists())
                file.createNewFile()

            val printer = FileOutputStream(file)
            printer.write(key.encoded)
            printer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    /**
     * Attempts to read RSA public key from a file.
     * @return RSA PublicKey
     */
    fun readRSAPublicKey(): PublicKey?{
        if(!isInit)
            throw ExceptionInInitializerError()

        try {
            val file = File(usersSubdirectory, rsaPublicKeyFileName)

            val stream = FileInputStream(file)
            val bytes = ByteArray(file.length().toInt())
            stream.read(bytes)
            stream.close()

            return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(bytes))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Attempts to read an encrypted note.
     * Make sure to setup cipher object before!
     * @return: On success, return a note as a string.
     */
    fun readNote(): String? {
        if(!isInit)
            throw ExceptionInInitializerError()

        try {
            val file = File(noteSubdirectory, noteFileName)
            if(!file.exists())
                return null

            val stream = FileInputStream(file)
            val bytes = ByteArray(file.length().toInt())
            stream.read(bytes)
            stream.close()

            // TODO return Security.decryptToString(bytes)
        } catch (e: IOException){
            e.printStackTrace()
        }

        return null
    }

    /**
     * Reads IV vector from note file.
     * @return ByteArray with IV data.
     */
    fun readIv(): ByteArray? {
        if (!isInit)
            throw ExceptionInInitializerError()

        try {
            val file = File(noteSubdirectory, noteFileName)
            if (!file.exists())
                return null

            val stream = FileInputStream(file)
            val bytes = ByteArray(file.length().toInt())
            stream.read(bytes)
            stream.close()
            return bytes.copyOfRange(0, 16)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}
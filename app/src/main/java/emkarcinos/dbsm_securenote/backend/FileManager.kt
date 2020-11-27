package emkarcinos.dbsm_securenote.backend

import java.io.*
import java.security.MessageDigest

object FileManager {
    // File pointing to the main data folder
    lateinit var appDirectory: File

    // Subdirectory name containing encrypted notes
    private const val notesFolderName: String = "notes"
    // Subdirectory name with user data
    private const val userdataFolderName: String = "users"

    // Pointers to subdirectories
    private lateinit var noteSubdirectory: File
    private lateinit var usersSubdirectory: File

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
            System.out.println("Alredy initialized!")
    }

    /**
     * Check whether given user's file exists.
     */
    fun userFileExists(user: User): Boolean {
        val filename = Security.generateHash(user.username)
        val file = File(usersSubdirectory, filename)
        return file.exists()
    }

    /**
     * Attempts to save user data to a file.
     * A new file is written for each user named by a hash of given username.
     * The file is then filled with hashed password and salt value.
     * If user's file already exists, it's contents will be overwritten.
     */
    fun saveUserData(user: User) {
        if(!isInit)
            throw ExceptionInInitializerError()

        val filename = Security.generateHash(user.username)

        try {
            val file = File(usersSubdirectory, filename)

            if(!userFileExists(user))
                file.createNewFile()

            val writer = file.bufferedWriter()
            writer.write(user.passwordHash + user.salt)
            writer.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Attempts to load a user from a local storage.
     * @param username: Username as string
     * @return On success, returns a new User instance.
     * If the user doesn't exist, returns null.
     */
    fun grabUser(username: String): User? {
        if(!isInit)
            throw ExceptionInInitializerError()

        var user: User? = null
        val filename = Security.generateHash(username)

        try {
            val file = File(usersSubdirectory, filename)
            if(!file.exists())
                return null

            val fileBytes = file.readBytes()

            // Sizes are multiplied by 2, because they've been written as hex
            val passwordHash = ByteArray(Security.hashSize * 2)
            val salt = ByteArray(Security.saltSize * 2)

            System.arraycopy(fileBytes, 0, passwordHash, 0, passwordHash.size)
            System.arraycopy(fileBytes, passwordHash.size, salt, 0, salt.size)

            user = User(username, String(passwordHash), String(salt))

        } catch (e: IOException){
            e.printStackTrace()
        }

        return user
    }

    /**
     * Checks if the note already exists.
     * @return true, if exists. Otherwise false
     */
    fun noteExists(note: Note): Boolean {
        val filename = Security.generateHash(note.user.username)
        val file = File(noteSubdirectory, filename)
        return file.exists()
    }

    /**
     * Attempts to securely save a note.
     */
    fun saveNote(note: Note) {
        if(!isInit)
            throw ExceptionInInitializerError()

        val filename: String = Security.generateHash(note.user.username)
        try {
            val file = File(noteSubdirectory, filename)

            if (!noteExists(note))
                file.createNewFile()

            val printer = FileOutputStream(file)
            val data = Security.encryptString(note.noteText, note.user.password)
            printer.write(data)
            printer.close()
        } catch (e: IOException){
            e.printStackTrace()
        }
    }

    /**
     * Attempts to read an encrypted note.
     * @param user: An user object to load the note for
     * @return: On success, return a new note object. Otherwise null
     */
    fun readNote(user: User): Note? {
        if(!isInit)
            throw ExceptionInInitializerError()

        val filename = Security.generateHash(user.username)

        var note: Note? = null
        try {
            val file = File(noteSubdirectory, filename)
            if(!file.exists())
                return null

            val stream = FileInputStream(file)
            val bytes = ByteArray(file.length().toInt())
            stream.read(bytes)
            stream.close()

            val decryptedText = Security.decryptToString(bytes, user.password)
            note = Note(decryptedText, user)
        } catch (e: IOException){
            e.printStackTrace()
        }

        return note
    }

}
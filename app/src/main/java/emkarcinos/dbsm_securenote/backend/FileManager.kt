package emkarcinos.dbsm_securenote.backend

import java.io.*

object FileManager {
    // File pointing to the main data folder
    lateinit var appDirectory: File

    // Subdirectory name containing encrypted notes
    private const val notesFolderName: String = "notes"
    // Subdirectory name with user data
    private const val userdataFolderName: String = "users"

    private const val noteFileName : String = "note"
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
     * Checks if the note already exists.
     * @return true, if exists. Otherwise false
     */
    fun noteExists(): Boolean {
        val file = File(noteSubdirectory, noteFileName)
        return file.exists()
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

            val data = Security.encryptString(note)
            printer.write(data)
            printer.close()
        } catch (e: IOException){
            e.printStackTrace()
        }
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

            return Security.decryptToString(bytes)
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
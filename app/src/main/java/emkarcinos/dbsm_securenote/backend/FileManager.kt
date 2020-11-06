package emkarcinos.dbsm_securenote.backend

import android.system.Os
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributeView

object FileManager {
    lateinit var directory: File

    /**
     * Attempts to save new user data to a local storage.
     * @return true, if the user does not alredy exist;
     * false, if the user alredy exists.
     */
    fun saveNewUser(user: User): Boolean {
        val filename = Security.generateHash(user.username)

        val file = File(directory, filename)
        if(!file.createNewFile()){
            // File exists
            return false
        } else {
            file.writeText(user.passwordHash)
        }

        return true
    }

    /**
     * Attempts to load a user from a local storage.
     * @return On success, returns a new User instance.
     * If the user dosen't exist, returns null.
     */
    fun grabUser(username: String): User? {
        val filename = Security.generateHash(username)
        val file = File(directory, filename)
        if(!file.exists())
            return null
        val reader = BufferedReader(FileReader(file))
        val passwordHash = reader.readLine()

        var user = User(username, "")
        user.passwordHash = passwordHash
        return user
    }

    /**
     * Gets file's last modification date.
     * @return Date as a long integer, or null if the file wasn't found.
     */
    fun getFileModificationDate(filename: String): Long? {
        val file = File(directory, filename)
        if(!file.exists())
            return null

        return Os.lstat(file.absolutePath).st_mtime
    }

    /**
     * Clears given file's contents, and writes a string into it.
     */
    fun saveText(filename: String, string: String) {
        val file = File(directory, filename)
        val printer = PrintWriter(file)
        printer.print(string)
    }

    /**
     * Clears given file's contents, and writes an array of strings as lines into it.
     */
    fun saveTextLines(filename: String, text: List<String>) {
        val file = File(directory, filename)
        val printer = PrintWriter(file)
        for(line: String in text)
            printer.println(line)
    }

    /**
     * Attempts to read a line from a given file.
     */
    fun readText(filename: String): String? {
        val file = File(directory, filename)
        val reader = BufferedReader(FileReader(file))

        return reader.readLine()
    }

    /**
     * Attempts to read all lines from a given file.
     */
    fun readTextLines(filename: String): List<String> {
        val file = File(directory, filename)
        val reader = BufferedReader(FileReader(file))

        var lines = ArrayList<String>()
        var line: String = ""
        while(line != null){
            line = reader.readLine()
            lines.add(line)
        }

        return lines
    }

}
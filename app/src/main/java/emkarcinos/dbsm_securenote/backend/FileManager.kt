package emkarcinos.dbsm_securenote.backend

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset

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
}
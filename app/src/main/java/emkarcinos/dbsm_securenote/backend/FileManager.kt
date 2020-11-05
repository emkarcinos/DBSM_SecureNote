package emkarcinos.dbsm_securenote.backend

import java.io.File

object FileManager {
    private lateinit var directory: String

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
}
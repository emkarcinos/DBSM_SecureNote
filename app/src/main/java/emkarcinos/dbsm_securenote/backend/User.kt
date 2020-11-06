package emkarcinos.dbsm_securenote.backend

import java.io.Serializable

class User : Serializable{
    var username: String
        private set

    var passwordHash: String

    var note: Note? = null

    constructor(username: String, password: String) {
        this.username = username
        this.passwordHash = Security.generateHash(password)
        //TODO: Save user info to a file
    }

    fun changePassword(password: String) {
        passwordHash = Security.generateHash(password)
        FileManager.updateUser(this)
        //TODO: Update this user's note encryption
    }

}
package emkarcinos.dbsm_securenote.backend

import java.io.Serializable

class User : Serializable{
    constructor(username: String, password: String) {
        this.username = username
        this.password = password
        this.salt = Security.generateSalt()
        this.passwordHash = Security.generateHash(password + salt)
    }

    var username: String
    var password: String

    // Salted password (salt is postfix)
    var passwordHash: String
    var salt: String

    var note: Note? = null

    /**
     * Creates a new user instance from already existing user in the database.
     *
     * This user isn't validated yet - its password is set empty
     * @param username: User name as string
     * @param passwordHash: Hashed and salted password as string
     * @param salt: Salt as string
     */
    constructor(username: String, passwordHash: String, salt: String) {
        this.username = username
        this.password = ""
        this.passwordHash = passwordHash
        this.salt = salt
    }


    /**
     * Changes users password.
     * Updates hash.
     * @param password: Plaintext password
     */
    fun changePassword(password: String) {
        this.password = password
        passwordHash = Security.generateHash(password + salt)
    }

}
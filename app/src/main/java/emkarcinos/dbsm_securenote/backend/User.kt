package emkarcinos.dbsm_securenote.backend

import java.io.Serializable

class User : Serializable {
    constructor(password: String) {
        this.password = password
        this.salt = Security.generateSalt()
        this.passwordHash = Security.generateHash(password + salt)
    }

    var password: String
    var encryptedPassword: ByteArray? = null

    // Salted password (salt is postfix)
    var passwordHash: String
    var salt: String

    var note: Note? = null

    var hasFinerprint: Boolean = false

    /**
     * Creates a new user instance from already existing user in the database.
     *
     * This user isn't validated yet - its password is set empty
     * @param passwordHash: Hashed and salted password as string
     * @param salt: Salt as string
     */
    constructor(passwordHash: String, salt: String) {
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

        if (hasFinerprint)
            encryptedPassword = Security.encryptPassphrase(password, salt)
    }

}
package emkarcinos.dbsm_securenote.backend

class User {
    private var username: String
        get() {
            return field
        }

    private var passwordHash: String
    private var note: Note? = null
        set(value) {
            field = value
        }

    constructor(username: String, password: String) {
        this.username = username
        this.passwordHash = Security.generateHash(password)
        //TODO: Save user info to a file
    }

    fun changePassword(password: String) {
        passwordHash = Security.generateHash(password)
        //TODO: Update this user's note encryption
        //TODO: Update user file
    }

}
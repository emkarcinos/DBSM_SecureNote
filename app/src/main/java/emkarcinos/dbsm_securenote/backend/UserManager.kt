package emkarcinos.dbsm_securenote.backend

object UserManager {

    /**
     * Attempts to create a new user object.
     * If the user already exists, it will not be created
     * @param username: Username as string
     * @param password: Password as string
     * @return If the user already exists, returns null. Otherwise, returns a new user object.
     */
    fun createNewUser(username: String, password: String): User?{
        val newUser = User(username,password)
        if (FileManager.userFileExists(newUser))
            return null

        FileManager.saveUserData(newUser)
        return newUser
    }

    /**
     * Attempts to get a username by a given name.
     * @param username: Username
     * @return If the user doesn't exist, returns null. Otherwise, returns a user object.
     */
    fun getUserByName(username: String): User?{
        return FileManager.grabUser(username)
    }

    /**
     * Updates the user with a new password.
     * Make sure the operation is permitted beforehand!
     * @param user: User object to update
     * @param password: New password
     */
    fun updateUserPassword(user: User, password: String) {
        user.changePassword(password)
        FileManager.saveUserData(user)
    }

    /**
     * Validates the specified password with a given user object.
     * If the validation was successful, user object plaintext password will be set to a given one.
     * @param user: User object to validate
     * @param password: Plaintext password to authenticate
     * @return True - if the validation is successful
     */
    fun validateCredentials(user: User, password: String): Boolean {
        val specifiedPasswordHash = Security.generatePBKDF(password, user.salt)

        if(specifiedPasswordHash != user.passwordHash)
            return false
        else
            user.password = password

        return true
    }

    /**
     * Gets the user's note.
     * If the note exists, and the user was validated, it will also update given user's note field.
     * @param user: User object to find a note for
     * @return If the note isn't found, or the user wasn't validated, returns null. Otherwise returns
     * Note object.
     */
    fun getUsersNote(user: User): Note? {
        return when (user.password.length) {
            0 -> null
            else -> FileManager.readNote(user)
        }
    }

    /**
     * Creates a new note for the specified user.
     * If the note already exists, it won't be created.
     * @param user: User object to create a note for
     * @return Null, if the note already exists, otherwise returns a new note object
     */
    fun createNote(user: User): Note? {
        val note = Note("", user)
        when {
            FileManager.noteExists(note) -> return null
            else -> FileManager.saveNote(note)
        }
        return note
    }
}
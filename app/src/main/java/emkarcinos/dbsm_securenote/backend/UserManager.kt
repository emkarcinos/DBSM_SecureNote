package emkarcinos.dbsm_securenote.backend

object UserManager {

    /**
     * Attempts to create a new user object.
     * If the user already exists, it will not be created
     * @param password: Password as string
     * @return If the user already exists, returns null. Otherwise, returns a new user object.
     */
    fun createNewUser(password: String): User?{
        val newUser = User(password)
        if (FileManager.userFileExists())
            return null

        FileManager.saveUserData(newUser)
        return newUser
    }


    /**
     * Adds fingerprint authentication method to the user.
     * @param user: User object
     */
    fun addFingerprint(user: User){
        val pubKey = Security.getOrCreateKeyFromKeystore().public
        FileManager.saveRSAPublicKey(pubKey)
        val encryptedPassphrase = Security.encryptPassphrase(user.password, user.password)
        user.hasFinerprint = true
        user.encryptedPassword = encryptedPassphrase
        FileManager.saveUserData(user)
    }


    /**
     * Attempts to get a username by a given name.
     * @return If the user doesn't exist, returns null. Otherwise, returns a user object.
     */
    fun getUser(): User?{
        return FileManager.grabUser()
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
        val specifiedPasswordHash = Security.generateHash(password + user.salt)

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
        return null
    }

    /**
     * Creates a new note for the specified user.
     * If the note already exists, it won't be created.
     * @param user: User object to create a note for
     * @return Null, if the note already exists, otherwise returns a new note object
     */
    fun createNote(user: User): Note? {
        val note = Note("", user)
//        when {
//            FileManager.noteExists(note) -> return null
//            else -> FileManager.saveNote(note)
//        }
        return note
    }
}
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
        when {
            FileManager.userFileExists(newUser) -> return newUser
            else -> FileManager.saveUserData(newUser)
        }
        return null
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
        val specifiedPasswordHash = Security.generateHash(password + user.salt)

        if(specifiedPasswordHash != user.passwordHash)
            return false
        else
            user.password = password

        return true
    }
}
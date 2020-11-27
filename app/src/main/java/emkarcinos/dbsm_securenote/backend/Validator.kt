package emkarcinos.dbsm_securenote.backend

object Validator {

    /**
     * Authenticates the user with a given password.
     * On success, his method also sets password field in user object.
     * @param user: user to authenticate
     * @param password: input password
     */
    fun authenticateUser(user: User, password: String): Boolean{
        val inputPasswordHash = Security.generateHash(password + user.salt)
        return inputPasswordHash == user.passwordHash
    }
}
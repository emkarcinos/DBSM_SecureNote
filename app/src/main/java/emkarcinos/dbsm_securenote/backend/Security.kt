package emkarcinos.dbsm_securenote.backend

import java.security.MessageDigest

object Security {
    fun generateHash(message: String): String {
        val bytes = message.toByteArray()
        val hasher = MessageDigest.getInstance("SHA-256")
        return hasher.digest(bytes).toString()
    }
}
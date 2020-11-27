package emkarcinos.dbsm_securenote.backend

import java.io.Serializable

class Note(private val noteText: String, private val user: User) : Serializable
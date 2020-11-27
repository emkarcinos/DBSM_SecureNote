package emkarcinos.dbsm_securenote.backend

import java.io.Serializable

class Note(val noteText: String, val user: User) : Serializable
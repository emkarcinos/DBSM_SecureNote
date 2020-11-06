package emkarcinos.dbsm_securenote.backend

import java.lang.StringBuilder

class Note(var user: User) {
    lateinit var noteText: String

    constructor(user: User, secret: String): this(user){
        loadNote(secret)
    }

    fun saveNote(text: String) {
        noteText = text
        FileManager.saveText(Security.generateHash(user.username), noteText)
    }

    fun loadNote(secret: String) {
        val text = FileManager.readText(Security.generateHash(user.username))
        noteText = if(text == null)
            ""
        else text
    }
}
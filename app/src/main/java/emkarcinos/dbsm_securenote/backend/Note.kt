package emkarcinos.dbsm_securenote.backend

import java.io.Serializable

class Note(var user: User) : Serializable {
    lateinit var noteText: String

    constructor(user: User, secret: String): this(user){
        loadNote(secret)
    }

    fun saveNote(text: String, secret: String) {
        noteText = text
        val cipheredText: ByteArray = Security.encryptString(noteText, secret)
        FileManager.saveBytes(Security.generateHash(user.username), cipheredText)
    }

    private fun loadNote(secret: String) {
        val fileBytes = FileManager.readRawBytes(Security.generateHash(user.username))

        if(fileBytes == null){
            noteText = ""
            return
        }
        val decipheredText = Security.decryptToString(fileBytes, secret)

        noteText = decipheredText
    }
}
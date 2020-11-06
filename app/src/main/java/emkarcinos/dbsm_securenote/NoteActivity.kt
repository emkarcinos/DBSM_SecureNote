package emkarcinos.dbsm_securenote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import emkarcinos.dbsm_securenote.R
import emkarcinos.dbsm_securenote.backend.Note
import emkarcinos.dbsm_securenote.backend.User

class NoteActivity : AppCompatActivity() {
    private lateinit var user: User
    private lateinit var secret: String
    private lateinit var note: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        val username = intent.getStringExtra("username")!!
        secret = intent.getStringExtra("password")!!

        user = User(username, secret)

        val title = findViewById<TextView>(R.id.noteTitle)
        title.text = username + "'s Note"
        getLoggedUsersNote()
    }

    fun getLoggedUsersNote(){
        val noteBox = findViewById<EditText>(R.id.noteTextBox)

        note = Note(user, secret)
        noteBox.setText(note.noteText)
    }

    fun onSaveButton(v: View) {
        val grabbedText = findViewById<EditText>(R.id.noteTextBox).editableText.toString().trim()

        note.saveNote(grabbedText, secret)
        Toast.makeText(this, "Successfully saved and secured!", Toast.LENGTH_SHORT).show()
    }
}
package emkarcinos.dbsm_securenote

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import emkarcinos.dbsm_securenote.backend.FileManager
import emkarcinos.dbsm_securenote.backend.Note
import emkarcinos.dbsm_securenote.backend.User
import emkarcinos.dbsm_securenote.backend.UserManager
import java.lang.Exception

class NoteActivity : AppCompatActivity() {
    private lateinit var user: User
    private lateinit var note: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        user = intent.getSerializableExtra("user") as User

        val title = findViewById<TextView>(R.id.noteTitle)
        title.text = user.username + "'s Note"

        getLoggedUsersNote()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.changeNotePasswordMenu -> {
                val intent = Intent(this, ChangePasswordActivity::class.java)
                intent.putExtra("user", user)
                intent.putExtra("note", note)
                startActivityForResult(intent, 1)
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 1) {
//            if (resultCode == Activity.RESULT_OK) {
//                secret = data?.getStringExtra("newPassword")!!
//            }
//        }
//    }


    private fun getLoggedUsersNote(){
        val noteBox = findViewById<EditText>(R.id.noteTextBox)

        val loadedNote = UserManager.getUsersNote(user)
        if(loadedNote == null) {
            // If the note doesn't exist, create a blank one
            when (val newNote = UserManager.createNote(user)) {
                null -> throw Exception()
                else -> note = newNote
            }
        } else
            note = loadedNote
        noteBox.setText(note.noteText)
    }

    fun onSaveButton(v: View) {
        val grabbedText = findViewById<EditText>(R.id.noteTextBox).editableText.toString().trim()

        note.noteText = grabbedText
        FileManager.saveNote(note)
        Toast.makeText(this, "Successfully saved and secured!", Toast.LENGTH_SHORT).show()
    }
}
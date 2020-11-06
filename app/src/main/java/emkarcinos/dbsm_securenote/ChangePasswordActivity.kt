package emkarcinos.dbsm_securenote

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import emkarcinos.dbsm_securenote.backend.Note
import emkarcinos.dbsm_securenote.backend.Security
import emkarcinos.dbsm_securenote.backend.User

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var oldPasswordBox: EditText
    private lateinit var password1box: EditText
    private lateinit var password2box: EditText

    private lateinit var user: User
    private lateinit var note: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        note = intent.getSerializableExtra("note") as Note
        user = intent.getSerializableExtra("user") as User

        oldPasswordBox = findViewById<EditText>(R.id.oldPasswordBox)
        password1box = findViewById<EditText>(R.id.newPasswordBox1)
        password2box = findViewById<EditText>(R.id.newPasswordBox2)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    fun onSubmitBtnClick(v: View) {
        if(changePassword()) {
            onSuccessPassChange()
        }
    }

    private fun onSuccessPassChange() {
        Toast.makeText(this, "Password changed.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, NoteActivity::class.java)
        intent.putExtra("newPassword", password2box.editableText.toString().trim())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun changePassword(): Boolean {
        val oldPass = oldPasswordBox.editableText.toString().trim()

        val password1 = password1box.editableText.toString().trim()

        val password2 = password2box.editableText.toString().trim()

        oldPasswordBox.error = null
        password1box.error = null
        password2box.error = null
        //Check if any of the fields are empty
        if(oldPass.isEmpty()){
            oldPasswordBox.setError("Required.")
            return false
        }

        if(password1.isEmpty()){
            password1box.setError("Required.")
            return false
        }

        if(password2.isEmpty()){
            password2box.setError("Required.")
            return false
        }

        //Check if two passwords are equal
        if(password1 != password2){
            password2box.setError("Passwords do not match.")
            return false
        }

        val oldPassHash = Security.generateHash(oldPass)
        if(oldPassHash != user.passwordHash){
            oldPasswordBox.error = "Wrong password."
            return false
        }

        user.changePassword(password2)
        note.saveNote(note.noteText, password2)
        return true

    }
}
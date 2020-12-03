package emkarcinos.dbsm_securenote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import emkarcinos.dbsm_securenote.backend.FileManager
import emkarcinos.dbsm_securenote.backend.User
import emkarcinos.dbsm_securenote.backend.UserManager
import java.util.regex.Pattern

class Register : AppCompatActivity() {
    private lateinit var usernameBox: EditText
    private lateinit var password1box: EditText
    private lateinit var password2box: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        usernameBox = findViewById(R.id.newUsernameInputBox)
        password1box = findViewById(R.id.newPasswordInputBox)
        password2box = findViewById(R.id.retypePasswordTextBox)
    }

    fun submitButtonClick(v: View){
        if(createUser())
            onSuccessUserCreate()
    }

    private fun onSuccessUserCreate() {
        Toast.makeText(this, "Successfully created user.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun createUser(): Boolean {
        val username = usernameBox.editableText.toString().trim()

        val password1 = password1box.editableText.toString().trim()

        val password2 = password2box.editableText.toString().trim()

        usernameBox.error = null
        password1box.error = null
        password2box.error = null
        //Check if any of the fields are empty
        if(username.isEmpty()){
            usernameBox.setError("Required.")
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
            password2box.setError("Passphrases do not match.")
            return false
        }

        // Check if a password matches regex
        val pattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{12,}$")
        val isStrong = pattern.matcher(password2).matches()

        if(!isStrong) {
            password1box.error = "Passphrase too weak."
            return false
        }

        val user = UserManager.createNewUser(username, password2)

        if(user == null){
            usernameBox.error = "This user already exists."
            return false
          }

        return true

    }
}
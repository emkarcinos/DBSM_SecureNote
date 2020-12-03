package emkarcinos.dbsm_securenote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import emkarcinos.dbsm_securenote.backend.FileManager
import emkarcinos.dbsm_securenote.backend.User
import emkarcinos.dbsm_securenote.backend.UserManager

class MainActivity : AppCompatActivity() {
    // Timeout between consecutive login attempts
    private val loginTimeout = 1000L
    private var lastButtonClickTime = 0L
    private lateinit var usernameBox: EditText
    private lateinit var passwordBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        usernameBox = findViewById(R.id.usernameInputBox)
        passwordBox = findViewById(R.id.passwordInputBox)
        // FileManager setup
        FileManager.init(applicationContext.filesDir)

        lastButtonClickTime = System.currentTimeMillis()
    }

    fun switchToRegisterPage(v: View) {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    fun loginButtonClick(v: View){
        if(System.currentTimeMillis() - lastButtonClickTime < loginTimeout)
            return
        val user = authenticate()
        lastButtonClickTime = System.currentTimeMillis()
        if(user != null){
            Toast.makeText(this,"Successfully authenticated.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, NoteActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
            clearTextBoxes()
        }
    }

    private fun clearTextBoxes(){
        usernameBox.editableText.clear()
        passwordBox.editableText.clear()
    }

    private fun authenticate(): User?{
        usernameBox.error = null
        passwordBox.error = null

        val username = usernameBox.editableText.toString().trim()
        val password = passwordBox.editableText.toString().trim()

        if(username.isEmpty()){
            usernameBox.error = "Required."
            return null
        }
        if(password.isEmpty()){
            passwordBox.error = "Required."
            return null
        }

        val user = UserManager.getUserByName(username)

        if(user == null){
            usernameBox.error = "This user does not exist."
            return null
        }

        if(!UserManager.validateCredentials(user, password)){
            passwordBox.editableText.clear()
            passwordBox.error = "Invalid passphrase."
            return null
        }

        return user
    }
}
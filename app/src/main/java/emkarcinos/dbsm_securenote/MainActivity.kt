package emkarcinos.dbsm_securenote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import emkarcinos.dbsm_securenote.backend.FileManager
import emkarcinos.dbsm_securenote.backend.Security
import emkarcinos.dbsm_securenote.backend.User

class MainActivity : AppCompatActivity() {
    private lateinit var usernameBox: EditText
    private lateinit var passwordBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        usernameBox = findViewById(R.id.usernameInputBox)
        passwordBox = findViewById(R.id.passwordInputBox)
        // FileManager setup
        FileManager.directory = applicationContext.filesDir
    }

    fun switchToRegisterPage(v: View) {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    fun loginButtonClick(v: View){
        val user = authenticate(v)
        if(user != null)
            Toast.makeText(this,"Successfully authenticated.", Toast.LENGTH_SHORT).show()
    }

    fun authenticate(v: View): User?{
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

        val passwordHash = Security.generateHash(password)
        val user = FileManager.grabUser(username)

        if(user == null){
            usernameBox.error = "This user does not exist."
            return null
        }

        if(passwordHash != user.passwordHash){
            passwordBox.error = "Invalid password."
            return null
        }

        return user
    }
}
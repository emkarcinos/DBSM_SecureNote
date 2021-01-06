package emkarcinos.dbsm_securenote

import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import emkarcinos.dbsm_securenote.backend.FileManager
import emkarcinos.dbsm_securenote.backend.Security
import emkarcinos.dbsm_securenote.backend.User
import emkarcinos.dbsm_securenote.backend.UserManager
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var biometricPrompt: BiometricPrompt
    // Timeout between consecutive login attempts
    private val loginTimeout = 1000L
    private var lastButtonClickTime = 0L
    private lateinit var usernameBox: EditText
    private lateinit var passwordBox: EditText
    var note = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        usernameBox = findViewById(R.id.usernameInputBox)
        passwordBox = findViewById(R.id.passwordInputBox)
        // FileManager setup
        FileManager.init(applicationContext.filesDir)

        lastButtonClickTime = System.currentTimeMillis()
    }

    private fun createCryptoObject(operation: Int): BiometricPrompt.CryptoObject {
        val iv: ByteArray
        when {
            FileManager.noteExists() and (operation == Cipher.DECRYPT_MODE) -> {
                iv = FileManager.readIv()!!
            }
            else -> {
                iv = ByteArray(16)
                val random = SecureRandom()
                random.nextBytes(iv)
            }
        }
        val ivParameterSpec = IvParameterSpec(iv)

        Security.cipherAESCBC.init(operation, Security.getOrCreateKeyFromKeystore("securenote"), ivParameterSpec)

        return BiometricPrompt.CryptoObject(Security.cipherAESCBC)

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

    private fun setupBiometrics() {
        checkBiometricsAvailable()

        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int,
                                                       errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(applicationContext,
                                "Authentication error: $errString", Toast.LENGTH_SHORT)
                                .show()
                    }

                    override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(applicationContext,
                                "Authentication succeeded!", Toast.LENGTH_SHORT)
                                .show()
                        //TODO
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(applicationContext, "Authentication failed",
                                Toast.LENGTH_SHORT)
                                .show()
                    }
                })
    }

    private fun checkBiometricsAvailable() {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)
            exitProcess(1)
    }

    private fun getLoggedUsersNote() {
        val noteBox = findViewById<EditText>(R.id.noteTextBox)

        val loadedNote = FileManager.readNote()
        if (loadedNote != null)
            note = loadedNote
        noteBox.setText(note)
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
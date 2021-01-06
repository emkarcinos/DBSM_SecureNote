package emkarcinos.dbsm_securenote

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
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var biometricPrompt: BiometricPrompt

    var note = ""

    var saving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // FileManager setup
        FileManager.init(applicationContext.filesDir)

        setupBiometrics()

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login to access your secret note")
                .setSubtitle("Use your fingerprint to unlock")
                .setNegativeButtonText("Log in")
                .build()

        val cipherObject = createCryptoObject(Cipher.DECRYPT_MODE)
        biometricPrompt.authenticate(promptInfo, cipherObject)

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
                if(saving)
                    saveNote()
                else
                    getLoggedUsersNote()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
            }
        })
    }

    private fun checkBiometricsAvailable(){
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)
            exitProcess(1)
    }

    private fun getLoggedUsersNote(){
        val noteBox = findViewById<EditText>(R.id.noteTextBox)

        val loadedNote = FileManager.readNote()
        if(loadedNote != null)
            note = loadedNote
        noteBox.setText(note)
    }

    fun saveNote(){
        val grabbedText = findViewById<EditText>(R.id.noteTextBox).editableText.toString().trim()

        note = grabbedText

        val data = FileManager.saveNote(note)
    }

    fun onSaveButton(v: View) {
        saving = true

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Save your note")
                .setSubtitle("Use your fingerprint to save")
                .setNegativeButtonText("Save Note")
                .build()

        val cipherObject = createCryptoObject(Cipher.ENCRYPT_MODE)
        biometricPrompt.authenticate(promptInfo, cipherObject)
    }

}
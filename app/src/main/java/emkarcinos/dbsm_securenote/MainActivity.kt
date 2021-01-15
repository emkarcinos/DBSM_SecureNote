package emkarcinos.dbsm_securenote

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.biometric.BiometricManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import emkarcinos.dbsm_securenote.backend.FileManager
import emkarcinos.dbsm_securenote.backend.Security
import emkarcinos.dbsm_securenote.backend.User
import emkarcinos.dbsm_securenote.backend.UserManager
import javax.crypto.Cipher
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var biometricPrompt: BiometricPrompt

    private lateinit var passwordBox: EditText
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    var note = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        passwordBox = findViewById(R.id.passwordInputBox)
        // FileManager setup
        FileManager.init(applicationContext.filesDir)

        if(!FileManager.userFileExists())
            showFirstRunPopup()
        else {
            val btn = findViewById<Button>(R.id.registerButton)
            btn.visibility=View.GONE
        }
    }

    private fun createCryptoObject(): BiometricPrompt.CryptoObject {
        Security.cipherRSA.init(Cipher.DECRYPT_MODE, Security.getOrCreateKeyFromKeystore().private)
        return BiometricPrompt.CryptoObject(Security.cipherRSA)
    }

    private fun showFirstRunPopup(){
        dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.first_login_popup, null);

        dialogBuilder.setView(popupView)
        dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        val button = popupView.findViewById<Button>(R.id.getStartedBtn)

        button.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
        dialog.show()

    }

    fun switchToRegisterPage(v: View) {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    fun loginButtonClick(v: View){
        val user = authenticate()
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


    private fun clearTextBoxes(){
        passwordBox.editableText.clear()
    }


    private fun authenticate(): User?{
        passwordBox.error = null

        val password = passwordBox.editableText.toString().trim()

        if(password.isEmpty()){
            passwordBox.error = "Required."
            return null
        }

        val user = UserManager.getUser()

        if(!UserManager.validateCredentials(user!!, password)){
            passwordBox.editableText.clear()
            passwordBox.error = "Invalid passphrase."
            return null
        }

        return user
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
package emkarcinos.dbsm_securenote

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import emkarcinos.dbsm_securenote.backend.*
import java.util.regex.Pattern

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var oldPasswordBox: EditText
    private lateinit var password1box: EditText
    private lateinit var password2box: EditText

    private lateinit var user: User
    private lateinit var note: Note


    private lateinit var popupView: View
    private lateinit var popupDialog: Dialog

    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        note = intent.getSerializableExtra("note") as Note
        user = intent.getSerializableExtra("user") as User

        oldPasswordBox = findViewById(R.id.oldPasswordBox)
        password1box = findViewById(R.id.newPasswordBox1)
        password2box = findViewById(R.id.newPasswordBox2)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    fun onSubmitBtnClick(v: View) {
        if(changePassword()) {
            if(!user.hasFinerprint)
                showFingerprintPrompt()
            else onSuccessPassChange()
        }
    }

    private fun onSuccessPassChange() {
        Toast.makeText(this, "Passphrase changed.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, NoteActivity::class.java)
        intent.putExtra("modifiedUser", user)
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
            password2box.setError("Passphrases do not match.")
            return false
        }

        if(!UserManager.validateCredentials(user, oldPass)){
            oldPasswordBox.error = "Wrong passphrase."
            return false
        }

        UserManager.updateUserPassword(user, password2)
        return true

    }

    private fun showFingerprintPrompt(){
        val dialogBuilder = AlertDialog.Builder(this)
        popupView = layoutInflater.inflate(R.layout.add_fingerprint_popup, null)

        dialogBuilder.setView(popupView)
        popupDialog = dialogBuilder.create()
        popupDialog.window?.setBackgroundDrawable(ColorDrawable(0))

        val yesBtn = popupView.findViewById<Button>(R.id.yesBtn)
        val skipBtn = popupView.findViewById<Button>(R.id.skipBtn)

        yesBtn.setOnClickListener {
            setupBiometrics()
            popupView.findViewById<ProgressBar>(R.id.loadingBar).visibility = View.VISIBLE
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Adding biometrics to our app")
                    .setNegativeButtonText("Cancel")
                    .build()
            biometricPrompt.authenticate(promptInfo)
        }

        skipBtn.setOnClickListener {
            onSuccessPassChange()
        }

        popupDialog.show()

    }

    private fun setupBiometrics() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        UserManager.addFingerprint(user)
                        popupDialog.dismiss()
                        onSuccessPassChange()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(applicationContext, "Authentication failed",
                                Toast.LENGTH_SHORT)
                                .show()
                    }
                })
    }
}
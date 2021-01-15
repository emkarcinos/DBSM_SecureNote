package emkarcinos.dbsm_securenote

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import emkarcinos.dbsm_securenote.backend.User
import emkarcinos.dbsm_securenote.backend.UserManager

class Register : AppCompatActivity() {
    private lateinit var password1box: EditText
    private lateinit var password2box: EditText

    private lateinit var biometricPrompt: BiometricPrompt

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        password1box = findViewById(R.id.newPasswordInputBox)
        password2box = findViewById(R.id.retypePasswordTextBox)
    }

    fun submitButtonClick(v: View){
        if(createUser()) {
            if(biometricsAvailable())
                showFingerprintPrompt()
            else
                onSuccessUserCreate()
        }
    }

    private fun onSuccessUserCreate() {
        Toast.makeText(this, "Successfully created user.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun biometricsAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate() == BIOMETRIC_SUCCESS
    }

    private fun showFingerprintPrompt(){
        val dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.add_fingerprint_popup, null)

        dialogBuilder.setView(popupView)
        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))

        val yesBtn = popupView.findViewById<Button>(R.id.yesBtn)
        val skipBtn = popupView.findViewById<Button>(R.id.skipBtn)

        yesBtn.setOnClickListener {
            setupBiometrics()
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Adding biometrics to our app")
                    .setNegativeButtonText("Cancel")
                    .build()
            biometricPrompt.authenticate(promptInfo)
        }

        skipBtn.setOnClickListener {
            onSuccessUserCreate()
        }

        dialog.show()

    }


    private fun createUser(): Boolean {

        val password1 = password1box.editableText.toString().trim()

        val password2 = password2box.editableText.toString().trim()

        password1box.error = null
        password2box.error = null
        //Check if any of the fields are emptyrn false
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

        user = UserManager.createNewUser(password2)!!

        return true

    }

    private fun setupBiometrics() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        UserManager.addFingerprint(user)
                        onSuccessUserCreate()
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
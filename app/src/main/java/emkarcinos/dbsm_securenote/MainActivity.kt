package emkarcinos.dbsm_securenote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import emkarcinos.dbsm_securenote.backend.FileManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // FileManager setup
        FileManager.directory = applicationContext.filesDir
    }

    fun switchToRegisterPage(v: View) {
        var intent = Intent(this, Register::class.java)
        startActivity(intent)
    }
}
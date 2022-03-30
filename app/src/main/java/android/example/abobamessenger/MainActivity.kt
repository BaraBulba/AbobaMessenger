package android.example.abobamessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registration_button.setOnClickListener {
            performRegister()
        }
        already_have_account_textView.setOnClickListener {
            Log.d("MainActivity", "try to show login activity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
    private fun performRegister() {
        val email = email_edittext_registration.text.toString()
        val password = password_edittext_registration.text.toString()
        val username = username_edittext_registration.text.toString()

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()){
            Toast.makeText(this,"Заполните все поля ввода", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("MainActivity", "Username is: " + username)
        Log.d("MainActivity", "Email is " + email)
        Log.d("MainActivity", "Password: + $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("Main", "Successfully created a user with uid: ${it.result.user?.uid}")
            }
            .addOnFailureListener{
                Log.d("Main", "Failed to create user: ${it.message}")
                Toast.makeText(this,"Не удалось создать аккаунт: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
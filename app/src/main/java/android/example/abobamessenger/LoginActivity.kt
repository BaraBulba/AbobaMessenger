package android.example.abobamessenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        log_in_button.setOnClickListener {
            performLogin()
    }
    back_to_registration_textView.setOnClickListener {
        finish()
        }
    }
    private fun performLogin() {
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Заполните все поля ввода", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("LoginActivity", "Attempt login with email/pw: $email/***")

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if (!it.isSuccessful) return@addOnCompleteListener
            }
            .addOnFailureListener{
                Toast.makeText(this,"Не удалось войти в аккаунт: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

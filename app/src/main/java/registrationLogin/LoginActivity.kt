package registrationLogin

import android.content.Intent
import android.example.abobamessenger.R
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import messages.LatestMessegesActivity

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

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
                val intent = Intent(this, LatestMessegesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
                Toast.makeText(this,"Не удалось войти в аккаунт: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

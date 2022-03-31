package android.example.abobamessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.registration_activity.*
import messages.LatestMessegesActivity
import registrationLogin.LoginActivity
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    companion object{
        val TAG = "RegistrationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_activity)


        registration_button.setOnClickListener {
            performRegister()
        }
        already_have_account_textView.setOnClickListener {
            Log.d(TAG, "try to show login activity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        image_picker_button.setOnClickListener {
            Log.d(TAG, "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d(TAG, "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selected_imageView_registration.setImageBitmap(bitmap)
            image_picker_button.alpha = 0f

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            image_picker_button.setBackgroundDrawable(bitmapDrawable)
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
        Log.d(TAG, "Email is " + email)
        Log.d(TAG, "User is " + username)
        Log.d(TAG, "Password: + $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d(TAG, "Successfully created a user with uid: ${it.result.user?.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener{
                Log.d(TAG, "Failed to create user: ${it.message}")
                Toast.makeText(this,"Не удалось создать аккаунт: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage(){

        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded photo : ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d(TAG,"File location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
                    .addOnFailureListener{
                    Log.d(TAG, "Failed to upload image to storage: ${it.message}")
                    }
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance("https://abobagram-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users/$uid")

        val user = User(uid, username_edittext_registration.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "We saved user to Firebase Database")
                val intent = Intent(this, LatestMessegesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d(TAG, "Failed to set value to database: ${it.message}")
            }
    }
}

class User(val uid: String, val username: String, val profileImageUrl: String){
    constructor() : this("", "", "")
}
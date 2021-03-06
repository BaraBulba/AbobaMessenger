package messages

import android.app.Activity
import android.content.Intent
import android.example.abobamessenger.NewMessageActivity
import android.example.abobamessenger.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import models.ChatMessage
import models.User


class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "Chat log"

    }
    val adapter = GroupieAdapter()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        chat_log_recyclerView.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username
        listenForMessages()

        chat_log_sendbutton.setOnClickListener {
            Log.d(TAG,"attempt to send message")
            performSendMessage()
        }
    }



    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance("https://abobagram-default-rtdb.europe-west1.firebasedatabase.app").getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                Log.d(TAG, chatMessage?.text!!)
                if (chatMessage != null) {}
                Log.d(TAG, chatMessage.text)

                if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                    val currentUser = LatestMessegesActivity.currentUser ?: return
                    adapter.add(ChatFromItem(chatMessage.text, currentUser))
                } else{
                    adapter.add(ChatToItem(chatMessage.text, toUser!!))
                }
                chat_log_recyclerView.scrollToPosition(adapter.itemCount -1)
            }
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
        })
    }

    private fun performSendMessage(){
        val text = editText_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid

        if (fromId == null) return
        if (toId == null) return

        val reference = FirebaseDatabase.getInstance("https://abobagram-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance("https://abobagram-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
             .addOnSuccessListener {
                 Log.d(TAG,"Saved our chat message ${reference.key}")
                 editText_chat_log.text.clear()
                 chat_log_recyclerView.scrollToPosition(adapter.itemCount - 1)
             }
        toReference.setValue(chatMessage)

        val latestMessegeRef = FirebaseDatabase.getInstance("https://abobagram-default-rtdb.europe-west1.firebasedatabase.app").getReference("/latest-messages/$fromId/$toId")
        latestMessegeRef.setValue(chatMessage)
        val latestMessegeToRef = FirebaseDatabase.getInstance("https://abobagram-default-rtdb.europe-west1.firebasedatabase.app").getReference("/latest-messages/$toId/$fromId")
        latestMessegeToRef.setValue(chatMessage)
    }

class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text = text

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_from_row
        Picasso.get().load(uri).into(targetImageView)
    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_to_row.text = text

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_to_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
        }
    }
}



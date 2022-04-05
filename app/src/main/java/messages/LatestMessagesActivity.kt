package messages

import android.content.Intent
import android.example.abobamessenger.NewMessageActivity
import android.example.abobamessenger.R
import android.example.abobamessenger.RegistrationActivity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupieAdapter
import kotlinx.android.synthetic.main.activity_latest_messeges.*
import models.ChatMessage
import models.User
import views.LatestMessageRow

class LatestMessagesActivity : AppCompatActivity() {

    companion object{

        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messeges)


        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.custom_app_bar_layout)

        recyclerView_for_the_latest_messages.adapter = adapter
        recyclerView_for_the_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))


//        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String): Boolean {
//                filterSearch(newText)
//                return true
//            }
//        })



        adapter.setOnItemClickListener { item, _ ->
            Log.d("Latest Messages", "123")
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)

        }

        fetchCurrentUser()
        listenForLatestMessages()
        verifyUserIsLoggedIn()

//        bottom_nav_view.setBackgroundColor(ContextCompat.getColor(applicationContext, android.R.color.transparent))
//
//        fab_search_person_button.setOnClickListener {
//            Toast.makeText(this, "Новое сообщение", Toast.LENGTH_SHORT).show()
//
//            val intent = Intent(this, NewMessageActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//            finish()
//        }




    }

//    private  fun filterSearch(text: String) {
////        val filteredList = arrayList.filter {
////            it.text.lowercase().contains(text.lowercase())
////        }
//        val filteredList: ArrayList<ChatMessage> = ArrayList()
//        for(message in arrayList) {
////            if message.text.lowercase()
//        }
//        Log.d("$filteredList","attempt to send message $filteredList")
//        if(filteredList.isNotEmpty()){
//            Log.d("$filteredList","attempt to send message $filteredList")
//            filteredList.forEach{
//                latestMessagesMap[it.id] = it
//                refreshRecyclerViewMessages()
//            }
//        }
//    }

    val latestMessagesMap = HashMap<String, ChatMessage>()
//    val latestItems: ArrayList<User?> = ArrayList()
//
//    val arrayList: ArrayList<ChatMessage> = ArrayList()

//    private fun refreshRecyclerViewMessages(){
//        adapter.clear()
//        latestMessagesMap.values.forEach {
//            val lmr = LatestMessageRow(it)
//            latestItems.add(lmr.getUser())
//            adapter.add(lmr)
////            adapter.add(LatestMessageRow(it))
//        }
//    }

    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance("https://abobagram-default-rtdb.europe-west1.firebasedatabase.app").getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
               refreshRecyclerViewMessages()

            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
        })
    }
    private val adapter = GroupieAdapter()

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance("https://abobagram-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d("LatestMessages", "Current user ${currentUser?.username}")
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, RegistrationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_icon -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.settings_icon ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegistrationActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}


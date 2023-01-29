package com.example.mybookapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Display.Mode
import com.bumptech.glide.Glide
import com.example.mybookapp.MyApplication
import com.example.mybookapp.R
import com.example.mybookapp.adapters.AdapterPdfFavorite
import com.example.mybookapp.databinding.ActivityProfileBinding
import com.example.mybookapp.models.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    //arraylist to hold books
    private lateinit var booksArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfFavorite : AdapterPdfFavorite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        loadFavoriteBooks()

        //handle click, goback
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //hanle click, open edit profile
        binding.profileEditBtn.setOnClickListener {
           startActivity(Intent(this, ProfileEditActivity::class.java))
        }

    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //convert timestamp to peroper date format
                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())
                    //set data
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType
                    //set image
                    try {
                        Glide.with(this@ProfileActivity).load(profileImage)
                            .placeholder(R.drawable.ic_baseline_person_24).into(binding.profileIv)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadFavoriteBooks() {
        //inti arrayList
        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites")
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                            //clear arraylist, before starting adding data
                        booksArrayList.clear()
                        for (ds in snapshot.children) {
                            //get only if of the book , rest of the info wahve loaded in adapter class
                            val bookId = "${ds.child("bookId").value}"
                            //set to adapter
                            val modelPdf = ModelPdf()
                            modelPdf.id = bookId

                            booksArrayList.add(modelPdf)
                        }
                        //set number of favorite books
                        binding.favoriteBookCountTv.text = "${booksArrayList.size}"
                        adapterPdfFavorite = AdapterPdfFavorite(this@ProfileActivity, booksArrayList)
                        binding.favoriteRv.adapter = adapterPdfFavorite
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })

    }
}
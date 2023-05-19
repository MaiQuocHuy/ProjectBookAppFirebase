package com.example.mybookapp.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Display.Mode
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.mybookapp.MyApplication
import com.example.mybookapp.R
import com.example.mybookapp.adapters.AdapterPdfFavorite
import com.example.mybookapp.databinding.ActivityProfileBinding
import com.example.mybookapp.models.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    //arraylist to hold books

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var booksArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfFavorite: AdapterPdfFavorite

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //reset to default values
        binding.accountTypeTv.text = "N/A"
        binding.memberDateTv.text = "N/A"
        binding.favoriteBookCountTv.text = "N/A"

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

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

        //handle click, verify user if not
        binding.moneyLl.setOnClickListener {
            startActivity(Intent(this, RechargeActivity::class.java))
        }

    }


    private fun sendEmailVerification() {
        //show progress dialog
        progressDialog.setMessage("Sending email verification instruction to email ${firebaseUser.email}")
        progressDialog.show()

        //send instructions
        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                //successfully
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Instruction sent check your email ${firebaseUser.email}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to send due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun loadUserInfo() {
        //check if user is verified or not, changes may effect after login when you verify  email
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
                    val money = "${snapshot.child("money").value}"

                    //convert timestamp to peroper date format
                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())
                    //set data
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType
                    binding.moneyTv.text = money+"VND"
                    //set image
                    try {
                        Log.d("Check", "$profileImage")
                        Log.d("Check", "https://firebasestorage.googleapis.com/v0/b/shop-ban-hang-c9314.appspot.com/o/logo-momo-png-4.png?alt=media&token=ea76a022-f210-4c1c-816c-deb95308c8aa")
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
            .addValueEventListener(object : ValueEventListener {
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
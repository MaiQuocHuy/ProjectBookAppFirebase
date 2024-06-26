package com.example.mybookapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.mybookapp.adapters.AdapterCategory
import com.example.mybookapp.databinding.ActivityDashboardAdminBinding
import com.example.mybookapp.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        checkUser()
        loadCategories()

        //search
        binding.searchEt.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0 : CharSequence?, p1: Int, p2: Int, p3: Int) {
                //called as when user type anything
                try {
                    adapterCategory.filter.filter(p0)
                } catch (e: Exception) {
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })



        // handle click, logout

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        // handle click, start add category page
        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        binding.addPdffab.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
        }
        //handle click, open click
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.revenuePdf.setOnClickListener {
            startActivity(Intent(this, ManageRevenueActivity::class.java))
        }
    }

    private fun loadCategories() {
        //init
        categoryArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    // get data as model
                    val model = ds.getValue(ModelCategory::class.java)
                    // add to arraylist
                    categoryArrayList.add(model!!)
                }
                //setup adapter
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)
                //set adapter to recycle view
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun checkUser() {
        // get current user
        val firebaseUser = firebaseAuth.currentUser

        if(firebaseUser == null) {
            startActivity( Intent(this, MainActivity::class.java))
            finish()
        }
        else {
           val email = firebaseUser.email
            binding.subTitleTv.text = email
        }
    }
}
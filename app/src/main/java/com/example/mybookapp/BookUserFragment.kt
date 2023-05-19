package com.example.mybookapp

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mybookapp.adapters.AdapterPdfUser
import com.example.mybookapp.databinding.FragmentBookUserBinding
import com.example.mybookapp.models.ModelPdf
import com.example.mybookapp.models.ModelTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BookUserFragment : Fragment, AdapterPdfUser.ItemClickListener {

    private lateinit var binding: FragmentBookUserBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var listbook: kotlin.collections.ArrayList<ModelTransaction> = arrayListOf()
//    lateinit var text: EditText

    public companion object {
        private const val TAG = "BOOKS_USER_TAG"

        //recieve data from activity to load books ef categoryId, category ,uid
        public fun newInstance(
            categoryId: String,
            category: String,
            uid: String
        ): BookUserFragment {
            val fragment = BookUserFragment()
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""
    private var currentAmount = 0

    //arraylist to hold pdfs
    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBookUserBinding.inflate(LayoutInflater.from(context), container, false)
        //load pdf according to category, this fragment will have new instance to load each category pdfs
        Log.d(TAG, "Oncreate view: ${category}")
        firebaseAuth = FirebaseAuth.getInstance()
        getUserCurrent()
        loadBookTransactions()
        if (category == "ALL") {
            //load all books
            loadAllBooks()
        } else if (category == "Most Viewed") {
            // load most viewed books
            loadMostViewedDownloadedBooks("viewsCount")
        } else if (category == "Most Downloaded") {
            // load most downloaded books
            loadMostViewedDownloadedBooks("downloadsCount")
        } else {
            // load selected category books
            loadCategorizedBook()
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (binding.searchTv != null)
            binding.searchTv.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    try {
                        Log.d(TAG, "${p0}")
                        adapterPdfUser.filter.filter(p0)
                    } catch (e: Exception) {
                        Log.d(TAG, "Is not working")
                    }
                }

                override fun afterTextChanged(p0: Editable?) {

                }

            });
    }


    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                val bookHashMap = HashMap<String, Boolean>()
                for (book in listbook) {
                    bookHashMap[book.bookID] = true
                }

                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(ModelPdf::class.java)
                    //check if the bookID is in the listbook and set status to "unlock"
                    if (model?.id.toString() in bookHashMap) {
                        model?.status = "unlock"
                    }
                    pdfArrayList.add(model!!)
                }

                //setup adapter
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList, this@BookUserFragment)
                //set adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10)//load most view or most downloaded
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    val bookHashMap = HashMap<String, Boolean>()
                    for (book in listbook) {
                        bookHashMap[book.bookID] = true
                    }
                    if(snapshot != null) {
                        for (ds in snapshot.children) {
                            //get data
                            val model = ds.getValue(ModelPdf::class.java)
                            //check if the bookID is in the listbook and set status to "unlock"
                            if(model != null) {
                                if (model?.id.toString() in bookHashMap) {
                                    model?.status = "unlock"
                                }
                                pdfArrayList.add(model!!)
                            }
                        }
                        //setup adapter
                        adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList, this@BookUserFragment)
                        //set adapter to recyclerview
                        binding.booksRv.adapter = adapterPdfUser
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadCategorizedBook() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)//load category
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    val bookHashMap = HashMap<String, Boolean>()
                    for (book in listbook) {
                        bookHashMap[book.bookID] = true
                    }

                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(ModelPdf::class.java)
                        //check if the bookID is in the listbook and set status to "unlock"
                        if (model?.id.toString() in bookHashMap) {
                            model?.status = "unlock"
                        }
                        pdfArrayList.add(model!!)
                    }
                    //setup adapter
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList, this@BookUserFragment)
                    //set adapter to recyclerview
                    binding.booksRv.adapter = adapterPdfUser
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun getUserCurrent() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        currentAmount = snapshot.child("money").value.toString().toInt()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun onItemClicked(bookPdf: ModelPdf) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        val formattedAmount = formatter.format(bookPdf.price)
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirm unlock book")
            .setMessage("Are you sure want to unlock this book with the money is $formattedAmount")
            .setPositiveButton("Confirm") { a, d ->
                Log.e("CurrentAmount", currentAmount.toString())
                if (currentAmount >= bookPdf.price) {
                    val minusAmount = currentAmount.toInt() - bookPdf.price.toInt()
                    updateMoneyUser(minusAmount)
                    val hashMap: HashMap<String, Any?> = HashMap()
                    val timestamp = System.currentTimeMillis()
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())
                    hashMap["transactionID"] = timestamp
                    hashMap["bookID"] = bookPdf.id
                    hashMap["uid"] = firebaseAuth.uid!!
                    hashMap["price"] = bookPdf.price.toInt()
                    hashMap["transactionDate"] = currentDate
                    val ref = FirebaseDatabase.getInstance().getReference("Transactions")
                    ref.child("$timestamp")
                        .setValue(hashMap)
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Success to unlock book", Toast.LENGTH_SHORT).show()
                            loadBookTransactions()
                            if (category == "ALL") {
                                //load all books
                                loadAllBooks()
                            } else if (category == "Most Viewed") {
                                // load most viewed books
                                loadMostViewedDownloadedBooks("viewsCount")
                            } else if (category == "Most Downloaded") {
                                // load most downloaded books
                                loadMostViewedDownloadedBooks("downloadsCount")
                            } else {
                                // load selected category books
                                loadCategorizedBook()
                            }
                        }
                        .addOnFailureListener { it ->
                            Toast.makeText(
                                activity,
                                "Faild to unlock book due to ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(activity, "You need to recharge", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { a, d ->
                a.dismiss()
            }
            .show()
    }

    private fun updateMoneyUser(minusAmount: Int) {
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["money"] = minusAmount
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
    }

    private fun loadBookTransactions() {
        val ref = FirebaseDatabase.getInstance().getReference("Transactions")
        ref.orderByChild("uid").equalTo(firebaseAuth.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        listbook.clear()
                        for (ds in snapshot.children) {
                            val model = ds.getValue(ModelTransaction::class.java)
                            //add to list
                            listbook.add(model!!)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}
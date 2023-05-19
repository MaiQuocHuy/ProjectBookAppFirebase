package com.example.mybookapp.activities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.mybookapp.R
import com.example.mybookapp.databinding.ActivityRechargeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.text.DecimalFormat

class RechargeActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivityRechargeBinding
    private var amount: Int = 0
    private var currentamount: Int = 0
    private lateinit var myDialog: Dialog
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRechargeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        Checkout.preload(applicationContext);

        binding.imgPaypalCard.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val bottomSheetView = LayoutInflater.from(applicationContext).inflate(R.layout.fragment_amount, findViewById<ConstraintLayout>(R.id.bottomSheet))
            bottomSheetView.findViewById<View>(R.id.Recharge_BottomSheet).setOnClickListener {
                amount += bottomSheetView.findViewById<EditText>(R.id.moneyEtBottom).text.toString().toInt()
                Log.d("Amount", "${amount}")
                processPayment()
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        binding.backIvPaymentMethodsPage.setOnClickListener {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        getUserCurrent()
    }

    private fun getUserCurrent() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                      if(snapshot.exists()) {
                           currentamount = snapshot.child("money").value.toString().toInt()
                      }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
    }

    private fun processPayment() {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_zHf96fMn8W3lq2");

        val activity: Activity = this

        try {
            val options = JSONObject()
            options.put("name", "Razorpay Corp")
            options.put("description", "Demoing Charges")
            //You can omit the image option to fetch the image from the dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg")
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
//            options.put("order_id", "order_DBJOWzybf0sJbb");
            options.put("amount", "${(amount*100).toInt()}")//amountx100
            Log.d("AMOUNT", "${(amount*100).toInt()}")
            val retryObj = JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            val prefill = JSONObject()
            prefill.put("email", "gaurav.kumar@example.com")
            prefill.put("contact", "0774557680")
            options.put("prefill", prefill)
            checkout.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(p0: String?) {
        try {
            val hashMap: HashMap<String, Any> = HashMap()
            hashMap["money"] = currentamount + amount
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!)
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failded  due to ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }catch (e: Exception) {
            Log.d("CheckRechar", "${e.message}")
        }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Log.d("FAILD", "${p1}")
        Toast.makeText(this, "Recharge Failed ${p1}", Toast.LENGTH_SHORT).show()
    }
}
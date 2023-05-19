package com.example.mybookapp.activities

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.lifecycle.lifecycleScope
import com.example.mybookapp.R
import com.example.mybookapp.databinding.ActivityManageRevenueBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ManageRevenueActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageRevenueBinding
    private var listYear: MutableList<Int> = mutableListOf()
    private lateinit var adapter: ArrayAdapter<Int>
    val calendar = Calendar.getInstance()
    private var date: Date? = null
    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    private var month_Year: String = ""
    private var dateStr: String = ""
    private var dateMonth:Date? = null
    private var dateDaily:Date? = null
    private var Year: String = ""
    val revenueYearAnnual: Int = 10000000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageRevenueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadYear()
        loadMoney()
        initUI()
    }

    private fun initUI() {
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        adapter = ArrayAdapter<Int>(this, R.layout.item_drop_down, listYear)
        binding.autoCompleTxtYear.setAdapter(adapter)
        binding.autoCompleTxtYear.setOnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position).toString()
            getRevenueFollwingYear(item)
        }
        binding.calendarTv.setOnClickListener {
            calendarPickDialog()
        }

        binding.calendarDatetv.setOnClickListener {
            calendarDatePickDialog()
        }

        binding.YearTv.setOnClickListener {
            val intent = Intent(this, BarChartActivity::class.java)
            intent.putExtra("date", "year")
            startActivity(intent)
        }

        binding.monthTv.setOnClickListener {
            if(dateMonth != null) {
                val intent = Intent(this@ManageRevenueActivity, BarChartActivity::class.java)
                intent.putExtra("date", "month")
                intent.putExtra("month_Year", month_Year)
                intent.putExtra("Year", Year)
                startActivity(intent)
            } else {
                calendarPickDialog()
            }
        }
    }

    private fun calendarDatePickDialog() {
        year = Calendar.getInstance().get(Calendar.YEAR)
        month = Calendar.getInstance().get(Calendar.MONTH)
        val datePickerDialog = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                calendar.set(year, month, dayOfMonth)
                date = calendar.time
                dateDaily = calendar.time
                dateStr = SimpleDateFormat("yyyy/MM/dd").format(date)
                Log.d("CheckMonth_Year", "${dateStr}")
                binding.calendarDatetv.setText(dateStr)
                var amount = 0
                getAmountFollowingDate(dateStr) { totalMoney ->
                    amount = totalMoney
                    var caculator = 0
                    if (amount != 0) {
                        val averageMonth = revenueYearAnnual/12
                        val averageDate = averageMonth/30
                        caculator = (amount.toInt()*100)/averageDate
                        Log.d("RevenueDate", "${amount}")
                        Log.d("caculatorDate", "${caculator}")
                        binding.progressBarDate.progress = caculator
                    } else {
                        binding.progressBarDate.progress = caculator
                    }
                    EventmouseToClickDate(amount, caculator)
                }

            }
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun EventmouseToClickDate(amount: Int, caculator: Int) {
        val locale = Locale("vi", "VN")
        val currencyFormat = DecimalFormat("#,### VND")
        val formattedCurrency = currencyFormat.format(amount).replace(",", ".")
        binding.progressBarDate.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Show the priceDetailsYear TextView when the user touches the ProgressBar
                        binding.priceDetailsDate.text = "${caculator.toString().trim()}% == ${
                            formattedCurrency.toString().trim()
                        }"
                        binding.priceDetailsDate.visibility = View.VISIBLE
                        return true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Hide the priceDetailsYear TextView when the user releases the touch on the ProgressBar
                        binding.priceDetailsDate.text = ""
                        binding.priceDetailsDate.visibility = View.GONE
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun getAmountFollowingDate(date: String, callback: (Int) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("Transactions")
           ref.orderByChild("transactionDate").equalTo(date)
               .addValueEventListener(object : ValueEventListener{
                   override fun onDataChange(snapshot: DataSnapshot) {
                       if(snapshot.exists()) {
                           var totalMoney = 0
                           for (ds in snapshot.children) {
                               var price = ds.child("price").value.toString().toInt()
                               Log.e("PriceDate", "$price")
                               totalMoney += price
                           }

                           callback(totalMoney)
                       } else {
                           callback(0)
                       }
                   }

                   override fun onCancelled(error: DatabaseError) {

                   }
               })
    }

    private fun calendarPickDialog() {
        year = Calendar.getInstance().get(Calendar.YEAR)
        month = Calendar.getInstance().get(Calendar.MONTH)
        val datePickerDialog = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                calendar.set(year, month, dayOfMonth)
                date = calendar.time
                dateMonth = calendar.time
                month_Year = SimpleDateFormat("yyyy/MM").format(date)
                Year = SimpleDateFormat("yyyy").format(date)
                Log.d("CheckMonth_Year", "${month_Year}")
                binding.calendarTv.setText(month_Year)
                var amount = 0
                getAmountByMonth(month_Year) { totalMoney ->
                    amount = totalMoney
                    var caculator = 0
                    if (amount != 0) {
                        val averageMonth = revenueYearAnnual/12
                        caculator = (amount.toInt()*100)/averageMonth
                        Log.d("Revenue", "${amount}")
                        Log.d("caculator", "${caculator}")
                        binding.progressBarMonth.progress = caculator
                    } else {
                        binding.progressBarMonth.progress = caculator
                    }
                    EventmouseToClickMonth(amount, caculator)
                }
            }
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun EventmouseToClickMonth(amount: Int, caculator: Int) {
        val locale = Locale("vi", "VN")
        val currencyFormat = DecimalFormat("#,### VND")
        val formattedCurrency = currencyFormat.format(amount).replace(",", ".")
        binding.progressBarMonth.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Show the priceDetailsYear TextView when the user touches the ProgressBar
                        binding.priceDetailsMonth.text = "${caculator.toString().trim()}% == ${
                            formattedCurrency.toString().trim()
                        }"
                        binding.priceDetailsMonth.visibility = View.VISIBLE
                        return true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Hide the priceDetailsYear TextView when the user releases the touch on the ProgressBar
                        binding.priceDetailsMonth.text = ""
                        binding.priceDetailsMonth.visibility = View.GONE
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun getAmountByMonth(monthYear: String, callback: (Int) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("Transactions")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    var totalMoney = 0
                    for (ds in snapshot.children) {
                        val transactionDate = ds.child("transactionDate").value.toString()

                        // Trích xuất phần tháng và năm từ transactionDate
                        val month = transactionDate.substring(5, 7)
                        val year = transactionDate.substring(0, 4)

                        // Kiểm tra xem tháng và năm có bằng monthYear hay không
                        if ("$year/$month" == monthYear) {
                            val price = ds.child("price").value.toString().toInt()
                            totalMoney += price
                        }
                    }
                    callback(totalMoney)
                } else {
                    callback(0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(0)
            }
        })
    }

    private fun getRevenueFollwingYear(item: String) {
        val currentDate: LocalDate = LocalDate.now()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        //currentDate
        val formattedDate: String = currentDate.format(formatter)
        val date = "${item}/01/01"
        val endDate = "${item}/12/31"
        Log.d("currentDate", "${formattedDate}")
        if (item == "2023") {
            var amountYear: Int = 0
            getAmountByYear(date, formattedDate) { totalMoney ->
                // Ở đây, bạn có thể sử dụng giá trị `totalMoney` đã tính được
                Log.e("TotalMoney", "$totalMoney")
                amountYear = totalMoney
                var caculator = 0
                if(amountYear != 0) {
                    caculator = (100 * amountYear.toInt())/revenueYearAnnual
                    binding.progressBarYear.progress = caculator
                } else {
                    binding.progressBarYear.progress = caculator
                }
                EventmouseToClick(amountYear, caculator)

            }
        } else {
            var amountYear: Int = 0
            getAmountByYear(date, endDate) { totalMoney ->
                // Ở đây, bạn có thể sử dụng giá trị `totalMoney` đã tính được
                amountYear = totalMoney
                var caculator = 0
                if(amountYear != 0) {
                    caculator = (100 * amountYear.toInt())/revenueYearAnnual
                    binding.progressBarYear.progress = caculator
                } else {
                    binding.progressBarYear.progress = caculator
                }
                EventmouseToClick(amountYear, caculator)
            }
        }
    }

    private fun EventmouseToClick(amountYear: Int, caculator: Int) {
        val locale = Locale("vi", "VN")
        val currencyFormat = DecimalFormat("#,### VND")
        val formattedCurrency = currencyFormat.format(amountYear).replace(",", ".")

        binding.progressBarYear.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Show the priceDetailsYear TextView when the user touches the ProgressBar
                        binding.priceDetailsYear.text = "${caculator.toString().trim()}% == ${
                            formattedCurrency.toString().trim()
                        }"
                        binding.priceDetailsYear.visibility = View.VISIBLE
                        return true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Hide the priceDetailsYear TextView when the user releases the touch on the ProgressBar
                        binding.priceDetailsYear.text = ""
                        binding.priceDetailsYear.visibility = View.GONE
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun getAmountByYear(date: String, formattedDate: String, callback: (Int) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("Transactions")
            ref.orderByChild("transactionDate")
                .startAt(date)
                .endAt(formattedDate)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        var totalMoney = 0
                        for (ds in snapshot.children) {
                            val price = ds.child("price").value.toString().toInt()
                            Log.e("TotalMoneyYear", "$price")
                            totalMoney += price
                        }
                        callback(totalMoney)
                    } else {
                        callback(0)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(0)
                }
            })
    }

    private fun loadMoney() {
        val ref = FirebaseDatabase.getInstance().getReference("Transactions")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalMoney = 0
                    for (ds in snapshot.children) {
                        val price = ds.child("price").value.toString().toInt()
                        totalMoney += price
                    }
                    val locale = Locale("vi", "VN")
                    val currencyFormat = DecimalFormat("#,### VND")
                    val formattedCurrency = currencyFormat.format(totalMoney).replace(",", ".")
                    binding.fullMoney.text = formattedCurrency
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun loadYear() {
        var i: Int = 2000
        for (i in 2000..2023) {
            listYear.add(i)
        }
    }
}
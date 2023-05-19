package com.example.mybookapp.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.mybookapp.R
import com.example.mybookapp.databinding.ActivityBarChartBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class BarChartActivity : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private val list: ArrayList<BarEntry> = ArrayList()
    private lateinit var binding: ActivityBarChartBinding
    private var checkQuery: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarChartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        barChart = binding.barChart
        checkQuery = intent.getStringExtra("date").toString()
        initUI()

    }

    private fun initUI() {
//        list.add(BarEntry(2020f, 10000000f))
//        list.add(BarEntry(2021f, 8000000f))
//        list.add(BarEntry(2022f, 6000000f))
//        list.add(BarEntry(2023f, 4000000f))
//        list.add(BarEntry(2024f, 2000000f))
//        val barDataSet= BarDataSet(list,"List")
//
//        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS,255)
//        barDataSet.valueTextColor= Color.BLACK
//        barDataSet.valueTextSize= 14f
//
//        val barData= BarData(barDataSet)
//
//        barChart.setFitBars(true)
//
//        barChart.data= barData
//
//        barChart.description.text= "Bar Chart"
//
//        val xAxis = barChart.xAxis
//        xAxis.valueFormatter = object : ValueFormatter() {
//            override fun getFormattedValue(value: Float): String {
//                return "${value.toInt()}"
//            }
//        }
//
//        val yAxisL = barChart.axisLeft
//        yAxisL.valueFormatter = object : ValueFormatter() {
//            override fun getFormattedValue(value: Float): String {
//                return "${value.toInt()} VND"
//            }
//        }
//
//        val yAxisR = barChart.axisRight
//        yAxisR.valueFormatter = object : ValueFormatter() {
//            override fun getFormattedValue(value: Float): String {
//                return "${value.toInt()} VND"
//            }
//        }
//
//        barChart.animateY(2000)
        if (checkQuery == "year") {
            loadChartByYear()
        } else if (checkQuery == "month") {
            loadCharByMonth()
        }
    }

    private fun loadCharByMonth() {
        val ref = FirebaseDatabase.getInstance().getReference("Transactions")

        val months = listOf(
            "01", "02", "03", "04", "05", "06",
            "07", "08", "09", "10", "11", "12"
        )

        val revenueArray = Array(12) { 0 }

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (ds in snapshot.children) {
                    val transactionDate = ds.child("transactionDate").value.toString()
                    val month = transactionDate.substring(5, 7)
                    val price = ds.child("price").value.toString().toInt()

                    val index = months.indexOf(month)
                    if (index != -1) {
                        revenueArray[index] += price
                    }
                }

                for (i in revenueArray.indices) {
                    Log.e("Annual", "Tháng ${months[i]}: Doanh thu ${revenueArray[i]}")
                    if(revenueArray[i] == 0) {
                        list.add(BarEntry(months[i].toFloat(), 0.1f))
                    } else {
                        list.add(BarEntry(months[i].toFloat(), revenueArray[i].toFloat()))
                    }
                }
                val barDataSet= BarDataSet(list,"List")

                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS,255)
                barDataSet.valueTextColor= Color.BLACK
                barDataSet.valueTextSize= 14f

                val barData= BarData(barDataSet)
                barData.barWidth = 0.8f
                barChart.setFitBars(true)

                barChart.data= barData

                barChart.description.text= "Bar Chart"
                barChart.invalidate()


                val xAxis = barChart.xAxis
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return  "${value.toInt()}"
                    }
                }

                xAxis.labelCount = 12

                val yAxisL = barChart.axisLeft
                yAxisL.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()} VND"
                    }
                }

                val yAxisR = barChart.axisRight
                yAxisR.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()} VND"
                    }
                }

                barChart.animateY(2000)
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        })

    }

    private fun loadChartByYear() {
        val ref = FirebaseDatabase.getInstance().getReference("Transactions")

        val years = listOf(
             2020f,2021f,2022f,2023f
        )

        val revenueArray = Array(4) { 0 }
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (ds in snapshot.children) {
                    val transactionDate = ds.child("transactionDate").value.toString()
                    val year = transactionDate.substring(0, 4).toFloat()
                    val price = ds.child("price").value.toString().toInt()

                    val index = years.indexOf(year)
                    if (index != -1) {
                        revenueArray[index] += price
                    }
                }

                for (i in revenueArray.indices) {
                    Log.e("Annual", "Nam ${years[i]}: Doanh thu ${revenueArray[i]}")
                    if(revenueArray[i] == 0) {
                        list.add(BarEntry(years[i], 0.1f))
                    } else {
                        list.add(BarEntry(years[i], revenueArray[i].toFloat()))
                    }
                }
                val barDataSet= BarDataSet(list,"List")

                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS,255)
                barDataSet.valueTextColor= Color.BLACK
                barDataSet.valueTextSize= 14f

                val barData= BarData(barDataSet)
                barData.barWidth = 0.8f
                barChart.setFitBars(true)

                barChart.data= barData

                barChart.description.text= "Bar Chart"
                barChart.invalidate()


                val xAxis = barChart.xAxis
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return  "${value.toInt()}"
                    }
                }

                xAxis.labelCount = 4

                val yAxisL = barChart.axisLeft
                yAxisL.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()} VND"
                    }
                }

                val yAxisR = barChart.axisRight
                yAxisR.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()} VND"
                    }
                }

                barChart.animateY(2000)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}
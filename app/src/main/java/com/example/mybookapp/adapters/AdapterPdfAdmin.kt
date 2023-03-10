package com.example.mybookapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.mybookapp.filters.FilterPdfAdmin
import com.example.mybookapp.MyApplication
import com.example.mybookapp.activities.PdfDetailActivity
import com.example.mybookapp.activities.PdfEditActivity
import com.example.mybookapp.databinding.RowPdfAdminBinding
import com.example.mybookapp.models.ModelPdf

class AdapterPdfAdmin:Adapter<AdapterPdfAdmin.HolderPdfAdmin>,Filterable {

    //context
    private var context: Context
    //hold arraylist pdfs
    public var pdfArrayList: ArrayList<ModelPdf>
    private var filterList: ArrayList<ModelPdf>

    private lateinit var binding: RowPdfAdminBinding
    //filter object
    private var filter: FilterPdfAdmin? = null


    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    inner class HolderPdfAdmin(itemview: View) :ViewHolder(itemview) {
        //inti ui of row pdf_admin.xml
        val pdfView = binding.pdfView
        val progressBar =  binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv =  binding.categoryTv
        val sizeTv =  binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        //bind/inflate layout row_pdf_admin.xml
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
//        Get data, set data
        //get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        //convert timestamp to dd/mm/yyyy
        val formattedDate = MyApplication.formatTimeStamp(timestamp)
        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate
    //create an application class that will contain the function used
        //category id
        MyApplication.loadCategory(categoryId, holder.categoryTv)
        //we don't need page number here pass null page number
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        holder.moreBtn.setOnClickListener {
            moreOptionsDiaLog(model, holder)
        }

        //handle item open pdf detail activity
        holder.itemView.setOnClickListener {
            //intent with book id
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId)// will be used to load book details
            context.startActivity(intent)
        }
    }

    private fun moreOptionsDiaLog(model: ModelPdf, holder: HolderPdfAdmin) {
        //get id, url, title of book
        val bookId = model.id
        val bookUrl =  model.url
        val bookTitle = model.title
        //options to show in dialog
        val options = arrayOf("Edit", "Delete")
        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options) { dialog, position ->
                //handle item lick
                if(position == 0){
                    //edit is clicked, lets create activity
                   val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId)
                    context.startActivity(intent)
                } else if(position == 1) {
                    //delete is clicked
                    MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }
            .show()
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter == null) {
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }

}
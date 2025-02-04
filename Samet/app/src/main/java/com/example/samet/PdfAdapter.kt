package com.example.samet

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PdfAdapter(private val context: Context, private val pdfList: List<Uri>, private val dbHelper: DatabaseHelper) :
    RecyclerView.Adapter<PdfAdapter.PdfViewHolder>() {

    class PdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfName: TextView = itemView.findViewById(R.id.pdfName)
        val openButton: Button = itemView.findViewById(R.id.openButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pdf, parent, false)
        return PdfViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val pdfUri = pdfList[position]
        val pdfName = dbHelper.getPdfNameByUri(pdfUri.toString())  // Get PDF name using URI
        holder.pdfName.text = pdfName ?: pdfUri.lastPathSegment  // Use the name or fallback to file name

        holder.openButton.setOnClickListener {
            val intent = Intent(context, PdfViewerActivity::class.java)
            intent.putExtra("pdfUri", pdfUri.toString())
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = pdfList.size
}





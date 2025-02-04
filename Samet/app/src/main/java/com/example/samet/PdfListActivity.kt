package com.example.samet

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PdfListActivity : AppCompatActivity() {
    private lateinit var pdfAdapter: PdfAdapter
    private val pdfList = mutableListOf<Uri>()
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_list)


        dbHelper = DatabaseHelper(this)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {

            loadPdfs()
        }


        pdfAdapter = PdfAdapter(this, pdfList, dbHelper)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPdfs)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = pdfAdapter
    }


    private fun loadPdfs() {
        try {
            pdfList.clear()
            pdfList.addAll(dbHelper.getAllPdfs())
        } catch (e: Exception) {
            Toast.makeText(this, "PDF'leri yüklerken hata oluştu.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                loadPdfs()
            } else {
                Toast.makeText(this, "PDF dosyalarına erişim izni verilmedi.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


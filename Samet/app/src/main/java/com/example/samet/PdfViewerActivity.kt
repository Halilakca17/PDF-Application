package com.example.samet

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class PdfViewerActivity : AppCompatActivity() {

    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private var pageIndex = 0
    private var parcelFileDescriptor: ParcelFileDescriptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        val pdfView = findViewById<ImageView>(R.id.pdfView)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnPrevious = findViewById<Button>(R.id.btnPrevious)


        val pdfUriString = intent.getStringExtra("pdfUri")

        if (pdfUriString != null) {
            val uri = Uri.parse(pdfUriString)

            try {

                val contentResolver: ContentResolver = contentResolver
                parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")

                if (parcelFileDescriptor != null) {

                    pdfRenderer = PdfRenderer(parcelFileDescriptor!!)


                    showPage(pageIndex, pdfView)


                    btnNext.setOnClickListener {
                        if (pageIndex < pdfRenderer!!.pageCount - 1) {
                            pageIndex++
                            showPage(pageIndex, pdfView)
                        } else {
                            Toast.makeText(this, "Son sayfadasınız!", Toast.LENGTH_SHORT).show()
                        }
                    }


                    btnPrevious.setOnClickListener {
                        if (pageIndex > 0) {
                            pageIndex--
                            showPage(pageIndex, pdfView)
                        } else {
                            Toast.makeText(this, "İlk sayfadasınız!", Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    Toast.makeText(this, "PDF dosyası açılamadı!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this, "PDF açılırken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Geçerli bir PDF URI'si sağlanmadı!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPage(index: Int, pdfView: ImageView) {
        if (pdfRenderer!!.pageCount <= index) return
        currentPage?.close()
        currentPage = pdfRenderer!!.openPage(index)

        val bitmap = Bitmap.createBitmap(currentPage!!.width, currentPage!!.height, Bitmap.Config.ARGB_8888)
        currentPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        pdfView.setImageBitmap(bitmap)
    }

    override fun onStop() {
        super.onStop()
        try {
            currentPage?.close()
            pdfRenderer?.close()
            parcelFileDescriptor?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

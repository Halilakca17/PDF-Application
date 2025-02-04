package com.example.samet

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dbHelper = DatabaseHelper(this)

        val btnAddPdf: Button = findViewById(R.id.btnAddPdf)
        val btnViewPdfs: Button = findViewById(R.id.btnViewPdfs)
        val btnAccount: Button = findViewById(R.id.btnAccount)

        btnAddPdf.setOnClickListener { selectPdf() }
        btnViewPdfs.setOnClickListener { startActivity(Intent(this, PdfListActivity::class.java)) }
        btnAccount.setOnClickListener { startActivity(Intent(this, AccountActivity::class.java)) }
    }

    private fun selectPdf() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "PDF Seç"), 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val selectedPdfUri: Uri? = data?.data
            selectedPdfUri?.let { showNameDialog(it) }
        }
    }

    private fun showNameDialog(pdfUri: Uri) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("PDF İsmi Girin")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Kaydet") { _, _ ->
            val pdfName = input.text.toString().trim()
            if (pdfName.isNotEmpty()) {
                dbHelper.addPdf(pdfName, pdfUri.toString())
                Toast.makeText(this, "PDF eklendi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "PDF ismi boş olamaz", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("İptal") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}

class AccountActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var etNewUsername: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnChangeUsername: Button
    private lateinit var btnChangePassword: Button
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var btnDeleteAccount: Button
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        tvUsername = findViewById(R.id.tvUsername)
        etNewUsername = findViewById(R.id.etNewUsername)
        etNewPassword = findViewById(R.id.etNewPassword)
        btnChangeUsername = findViewById(R.id.btnChangeUsername)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)


        val sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1)


        if (userId != -1) {
            dbHelper = DatabaseHelper(this)
            val username = dbHelper.getUsernameById(userId) ?: "Bilinmiyor"
            tvUsername.text = "Kullanıcı Adı: $username"
        } else {
            tvUsername.text = "Kullanıcı bilgisi bulunamadı"
        }


        btnChangePassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()

            if (newPassword.isNotEmpty()) {

                if (userId != -1) {
                    dbHelper.updatePassword(userId, newPassword)  // Şifreyi güncelle
                    Toast.makeText(this, "Şifre başarıyla değiştirildi.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Yeni şifre boş olamaz.", Toast.LENGTH_SHORT).show()
            }
        }


        btnChangeUsername.setOnClickListener {
            val newUsername = etNewUsername.text.toString().trim()

            if (newUsername.isNotEmpty()) {

                if (userId != -1) {
                    val oldUsername = dbHelper.getUsernameById(userId)
                    if (oldUsername != newUsername) {
                        dbHelper.updateUsername(userId, newUsername)
                        tvUsername.text = "Kullanıcı Adı: $newUsername"
                        Toast.makeText(this, "Kullanıcı adı başarıyla değiştirildi.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Yeni kullanıcı adı eski adınızla aynı.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Yeni kullanıcı adı boş olamaz.", Toast.LENGTH_SHORT).show()
            }
        }

        btnDeleteAccount.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setMessage("Hesabınızı silmek istediğinize emin misiniz?")
                .setCancelable(false)
                .setPositiveButton("Evet") { dialog, id ->

                    if (userId != -1) {
                        dbHelper.deleteUser(userId)
                        Toast.makeText(this, "Hesap başarıyla silindi.", Toast.LENGTH_SHORT).show()


                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                .setNegativeButton("Hayır") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }
}

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "UsersDB", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Users (ID INTEGER PRIMARY KEY AUTOINCREMENT, Email TEXT, Username TEXT, Password TEXT)")
        db.execSQL("CREATE TABLE Pdfs (ID INTEGER PRIMARY KEY AUTOINCREMENT, PdfName TEXT, PdfUri TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Users")
        db.execSQL("DROP TABLE IF EXISTS Pdfs")
        onCreate(db)
    }

    fun addPdf(pdfName: String, pdfUri: String) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("PdfName", pdfName)
                put("PdfUri", pdfUri)
            }
            db.insert("Pdfs", null, values)
        }
    }
    fun updateUsername(userId: Int, newUsername: String) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("Username", newUsername)
            }
            db.update("Users", values, "ID = ?", arrayOf(userId.toString()))
        }
    }

    fun getUsernameById(userId: Int): String? {
        readableDatabase.use { db ->
            val cursor = db.rawQuery("SELECT Username FROM Users WHERE ID = ?", arrayOf(userId.toString()))
            return if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow("Username"))
            } else {
                null
            }.also {
                cursor.close()
            }
        }
    }
    fun getAllPdfs(): List<Uri> {
        val pdfs = mutableListOf<Uri>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT PdfUri FROM Pdfs", null)

        while (cursor.moveToNext()) {
            val pdfUriString = cursor.getString(0)
            val pdfUri = Uri.parse(pdfUriString)
            pdfs.add(pdfUri)
        }
        cursor.close()
        db.close()
        return pdfs
    }
    fun getPdfNameByUri(pdfUri: String): String? {
        readableDatabase.use { db ->
            val cursor = db.rawQuery("SELECT PdfName FROM Pdfs WHERE PdfUri = ?", arrayOf(pdfUri))
            return if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow("PdfName"))
            } else {
                null
            }.also {
                cursor.close()
            }
        }
    }
    fun updatePassword(userId: Int, newPassword: String) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("Password", newPassword)
            }
            db.update("Users", values, "ID = ?", arrayOf(userId.toString()))
        }
    }
    fun deleteUser(userId: Int) {
        writableDatabase.use { db ->
            db.delete("Users", "ID = ?", arrayOf(userId.toString()))
        }
    }
}

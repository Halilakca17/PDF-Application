package com.example.samet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val dbHelper = DatabaseHelper(this)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val db = dbHelper.readableDatabase
                val cursor = db.rawQuery(
                    "SELECT * FROM Users WHERE TRIM(Username) = TRIM(?) AND TRIM(Password) = TRIM(?)",
                    arrayOf(username.trim(), password.trim())
                )

                if (cursor.moveToFirst()) {
                    // Kullanıcı adı ve şifre doğru
                    val userId = cursor.getInt(cursor.getColumnIndex("ID"))  // Kullanıcı ID'si alınıyor

                    // Kullanıcı ID'sini SharedPreferences'a kaydediyoruz
                    val sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putInt("userId", userId)  // Kullanıcı ID'sini kaydediyoruz
                    editor.apply()

                    Toast.makeText(this, "Giriş başarılı", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    Toast.makeText(this, "Geçersiz kullanıcı adı veya şifre", Toast.LENGTH_SHORT).show()
                }
                cursor.close()
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}








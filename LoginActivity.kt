package com.example.test60

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            when {
                username == "123" && password == "123" -> {
                    val intent = Intent(this, TeacherActivity::class.java)
                    intent.putExtra("name", "Nguyễn Minh Huy")
                    intent.putExtra("subject", "Môn lập trình app")
                    startActivity(intent)
                }
                username == "456" && password == "456" -> {
                    val intent = Intent(this, StudentActivity::class.java)
                    intent.putExtra("name", "Tưởng Văn Anh Quân")
                    intent.putExtra("student_id", "222001488")
                    startActivity(intent)
                }
                else -> {
                    val intent = Intent(this, StudentActivity::class.java)
                    intent.putExtra("student_name", username)
                    startActivity(intent)
                }
            }
        }
    }
}
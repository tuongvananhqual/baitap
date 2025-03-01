package com.example.test60

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class TeacherActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var lvStudents: ListView
    private val studentList = arrayListOf(
        "Tưởng Văn Anh Quân",
        "Trần Thị B",
        "Lê Hoàng C",
        "Phạm Minh D",
        "Hoàng Đức E"
    )  // Danh sách sinh viên mẫu
    private lateinit var adapter: ArrayAdapter<String>
    private val APP_UUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc")
    private val APP_NAME = "AttendanceApp"
    private var serverSocket: BluetoothServerSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)

        lvStudents = findViewById(R.id.lv_students)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, studentList)
        lvStudents.adapter = adapter

        // Nhận dữ liệu từ Intent
        val name = intent.getStringExtra("name")
        val subject = intent.getStringExtra("subject")
        val tvTeacherInfo = findViewById<TextView>(R.id.tv_teacher_info)
        tvTeacherInfo.text = "Tên: $name\nMôn dạy: $subject"

        checkBluetoothPermissions()

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_SHORT).show()
        } else {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                startActivityForResult(enableBtIntent, 1)
            }
        }

        // Bắt đầu chấp nhận kết nối Bluetooth
        AcceptThread().start()
    }

    private fun checkBluetoothPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1001
            )
        }
    }

    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {
        init {
            if (ActivityCompat.checkSelfPermission(
                    this@TeacherActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID)
            } else {
                runOnUiThread {
                    Toast.makeText(this@TeacherActivity, "Không có quyền BLUETOOTH_CONNECT", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun run() {
            var socket: BluetoothSocket?
            while (true) {
                socket = try {
                    serverSocket?.accept()
                } catch (e: IOException) {
                    break
                }

                if (socket != null) {
                    manageConnectedSocket(socket)
                    try {
                        serverSocket?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    break
                }
            }
        }
    }

    private fun manageConnectedSocket(socket: BluetoothSocket) {
        val studentDeviceName = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            socket.remoteDevice.name ?: "Sinh viên không tên"
        } else {
            "Không có quyền truy cập thiết bị"
        }

        runOnUiThread {
            val inputStream: InputStream = socket.inputStream
            val buffer = ByteArray(1024)
            val bytes: Int = inputStream.read(buffer)
            val studentName = String(buffer, 0, bytes)

            // Kiểm tra nếu tên sinh viên trong danh sách của giảng viên thì đổi màu thành xanh lá cây
            for (i in 0 until lvStudents.childCount) {
                val nameInList = lvStudents.getItemAtPosition(i).toString()
                if (nameInList == studentName) {
                    lvStudents.getChildAt(i)?.setBackgroundColor(Color.GREEN)
                    break
                }
            }

            Toast.makeText(this, "$studentName đã điểm danh", Toast.LENGTH_SHORT).show()
        }

        try {
            val outputStream: OutputStream = socket.outputStream
            outputStream.write("Đã điểm danh".toByteArray())
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
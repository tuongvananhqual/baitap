package com.example.test60

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.OutputStream
import java.util.*

class StudentActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var lvTeachers: ListView
    private lateinit var btnScan: Button
    private val teacherList = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private val APP_UUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        lvTeachers = findViewById(R.id.lv_teachers)
        btnScan = findViewById(R.id.btn_attendance)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, teacherList)
        lvTeachers.adapter = adapter

        // Nhận dữ liệu từ Intent
        val name = intent.getStringExtra("name")
        val studentId = intent.getStringExtra("student_id")
        val tvStudentInfo = findViewById<TextView>(R.id.tv_student_info)
        tvStudentInfo.text = "Tên: $name\nMã sinh viên: $studentId"

        btnScan.setOnClickListener {
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra quyền truy cập Bluetooth (Android 8)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN),
                    1
                )
                return@setOnClickListener
            }

            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
            } else {
                startBluetoothDiscovery()
            }
        }

        lvTeachers.setOnItemClickListener { _, _, position, _ ->
            val selectedDeviceName = teacherList[position]
            val device = bluetoothAdapter?.bondedDevices?.find { it.name == selectedDeviceName }
            if (device != null) {
                if (name != null) {
                    connectToTeacher(device, name)
                }
            } else {
                Toast.makeText(this, "Không tìm thấy thiết bị!", Toast.LENGTH_SHORT).show()
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    private fun startBluetoothDiscovery() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if (bluetoothAdapter?.isDiscovering == false) {
            teacherList.clear()
            adapter.notifyDataSetChanged()
            bluetoothAdapter?.startDiscovery()
            Toast.makeText(this, "Đang tìm giảng viên...", Toast.LENGTH_SHORT).show()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            device?.name?.let {
                if (!teacherList.contains(it)) {
                    teacherList.add(it)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToTeacher(device: BluetoothDevice, studentName: String) {
        val socket: BluetoothSocket? = device.createRfcommSocketToServiceRecord(APP_UUID)
        bluetoothAdapter?.cancelDiscovery()

        try {
            socket?.connect()

            val outputStream: OutputStream? = socket?.outputStream
            outputStream?.write(studentName.toByteArray())

            runOnUiThread {
                Toast.makeText(this, "Điểm danh thành công!", Toast.LENGTH_SHORT).show()
            }
            socket?.close()
        } catch (e: IOException) {
            runOnUiThread {
                Toast.makeText(this, "Kết nối thất bại!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}
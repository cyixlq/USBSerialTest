package com.lzy.usbserialtest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lzy.usbserialtest.utils.USBSerialPortUtil
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var edtBote: EditText
    private lateinit var tvReceive: TextView
    private lateinit var edtSendData: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        val listener = object : USBSerialPortUtil.OnDataReceiveListener {
            @SuppressLint("SetTextI18n")
            override fun onDataReceive(data: ByteArray) {
                val text = tvReceive.text.toString()
                tvReceive.text = text + "\n" + getByteArrayString(data)
            }
        }
        USBSerialPortUtil.setOnDataReceiveListener(listener)
    }

    private fun getByteArrayString(byteArray: ByteArray): String {
        val stringBuilder = StringBuilder()
        val max = byteArray.size
        stringBuilder.append("[")
        byteArray.forEachIndexed { index, byte ->
            if (byte == 0.toByte()) {
                if (index == max - 1)
                    stringBuilder.append("]")
                return@forEachIndexed
            }
            stringBuilder.append(Integer.toHexString(byte.toInt()))
            if (index == max - 1)
                stringBuilder.append("]")
            else
                stringBuilder.append(",")
        }
        return stringBuilder.toString()
    }

    private fun initView() {
        edtBote = findViewById(R.id.edtBote)
        tvReceive = findViewById(R.id.tvReceive)
        edtSendData = findViewById(R.id.edtSendData)
    }

    fun openOrClose(v: View) {
        if (v is Button) {
            if ("打开" == v.text) {
                val str = edtBote.text.toString()
                if (str.isBlank()) {
                    toastShort("请输入波特率")
                    return
                }
                val isSuccess = USBSerialPortUtil.open(str.toInt())
                if (isSuccess)
                    v.text = "关闭"
            } else {
                USBSerialPortUtil.close()
                v.text = "打开"
            }
        }
    }

    fun send(v: View) {
        if (!USBSerialPortUtil.isOpen()) {
            toastShort("设备未打开")
            return
        }
        val str = edtSendData.text.toString()
        if (str.isBlank())
            toastShort("请输入要发送的数据")
        val strArray = str.split(" ")
        val data = ByteArray(strArray.size)
        strArray.forEachIndexed { index, s ->
            val byte = s.toInt().toByte()
            data[index] = byte
        }
        USBSerialPortUtil.send(data)
    }

    fun clear(v: View) {
        tvReceive.text = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        USBSerialPortUtil.release()
    }

    private fun toastShort(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
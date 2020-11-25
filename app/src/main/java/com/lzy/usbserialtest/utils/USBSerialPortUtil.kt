package com.lzy.usbserialtest.utils

import android.app.Application
import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import cn.wch.ch34xuartdriver.CH34xUARTDriver
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

object USBSerialPortUtil {

    private const val ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION"

    private lateinit var driver: CH34xUARTDriver
    private lateinit var app: Application
    private var listener: OnDataReceiveListener? = null
    private var isOpen = AtomicBoolean(false)
    private val sendThread = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    fun init(app: Application) {
        this.app = app
        driver = CH34xUARTDriver(
            app.getSystemService(Context.USB_SERVICE) as UsbManager,
            app,
            ACTION_USB_PERMISSION
        )
        if (!driver.UsbFeatureSupported())
            toastShort("您的设备不支持USB HOST，请更换设备后重试")
    }

    fun open(baudRate: Int): Boolean {
        if (!isOpen.get()) {
            val permission = driver.ResumeUsbPermission()
            if (permission == 0) {
                when (driver.ResumeUsbList()) {
                    -1 -> {
                        toastShort("打开失败")
                        driver.CloseDevice()
                        return false
                    }
                    0 -> {
                        return if (driver.mDeviceConnection != null) {
                            if (!driver.UartInit()) {
                                toastShort("初始化失败")
                                false
                            } else {
                                isOpen.set(true)
                                driver.SetConfig(baudRate, 8, 1, 0, 0)
                                toastShort("初始化成功，打开成功")
                                ReadThread().start()
                                true
                            }
                        } else {
                            toastShort("没有检测到连接的设备")
                            false
                        }
                    }
                    else -> {
                        toastShort("USB未授权")
                    }
                }
            } else {
                toastShort("USB未授权")
            }
            return false
        }
        return true
    }

    fun close() {
        isOpen.set(false)
        driver.CloseDevice()
    }

    fun release() {
        close()
        this.listener = null
    }

    fun isOpen(): Boolean {
        return isOpen.get()
    }

    fun send(data: ByteArray) {
        sendThread.execute {
            val length = driver.WriteData(data, data.size)
            if (length < 0)
                handler.post { toastShort("发送失败") }
        }
    }

    private fun toastShort(msg: String) {
        Toast.makeText(app, msg, Toast.LENGTH_SHORT).show()
    }

    fun setOnDataReceiveListener(listener: OnDataReceiveListener) {
        this.listener = listener
    }

    class ReadThread : Thread() {
        override fun run() {
            val buffer = ByteArray(12)
            while (isOpen.get()) {
                val length = driver.ReadData(buffer, buffer.size)
                if (length != 0) {
                    handler.post {
                        listener?.onDataReceive(buffer)
                    }
                }
            }
        }
    }

    interface OnDataReceiveListener {
        fun onDataReceive(data: ByteArray)
    }
}
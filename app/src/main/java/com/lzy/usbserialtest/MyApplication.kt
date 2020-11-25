package com.lzy.usbserialtest

import android.app.Application
import com.lzy.usbserialtest.utils.USBSerialPortUtil

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        USBSerialPortUtil.init(this)
    }
}
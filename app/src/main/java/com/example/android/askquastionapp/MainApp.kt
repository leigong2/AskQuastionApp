package com.example.android.askquastionapp

import android.annotation.SuppressLint
import android.os.Build
import com.example.android.askquastionapp.keeplive.BindOService
import com.example.android.askquastionapp.keeplive.BindService
import zune.keeplivelibrary.app.KeepLiveHelper
import zune.keeplivelibrary.util.NotificationUtils

class MainApp: BaseApplication() {
    private val PUSH_APP_ID = "2882303761517566170"
    private val PUSH_APP_KEY = "5121756688170"

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: MainApp? = null
        internal fun getApp(): MainApp {
            return context!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        initKeepLive()
/*        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.BD09LL)*/
    }
    private fun initKeepLive() {
        KeepLiveHelper.getDefault().init(this, PUSH_APP_ID, PUSH_APP_KEY)
        NotificationUtils().setNotification("我是标题", "我是内容", R.mipmap.ic_launcher)
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            KeepLiveHelper.getDefault().bindService(BindOService::class.java)
        } else {
            KeepLiveHelper.getDefault().bindService(BindService::class.java)
        }
    }
}
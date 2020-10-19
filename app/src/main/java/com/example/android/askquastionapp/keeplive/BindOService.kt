package com.example.android.askquastionapp.keeplive

import android.content.ComponentName
import android.content.Intent
import android.os.Environment
import android.text.TextUtils
import androidx.core.app.JobIntentService
import com.example.android.askquastionapp.MainApp
import com.example.android.askquastionapp.R
import java.io.*

/**
 * Created by leigong2 on 2018-06-16 016.
 */
class BindOService: JobIntentService() {
    override fun onHandleWork(intent: Intent) {
    }

    override fun onCreate() {
        super.onCreate()
        println("zune: 测试bindservice")
//        save("onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("zune: 测试bindservice start")
        return super.onStartCommand(intent, flags, startId)
    }

    private fun save(state: String) {
        try {
            val dir = File(Environment.getExternalStorageDirectory().toString() + "/Android/system")
            if (!dir.exists()) {
                dir.mkdir()
            }
            val file = File(dir, MainApp.getApp().getString(R.string.app_name) + ".txt")
            if (!file.exists()) {
                file.createNewFile()
            }
            var fw: FileWriter? = null
            fw = FileWriter(file)
            fw!!.write(state)
            fw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun read(appName: String): String {
        try {
            val dir = File(Environment.getExternalStorageDirectory().toString() + "/Android/system")
            if (!dir.exists()) {
                dir.mkdir()
            }
            val file = File(dir, "$appName.txt")
            if (!file.exists()) {
                file.createNewFile()
            }
            var fr: BufferedReader? = null
            fr = BufferedReader(FileReader(file))
            val s = fr.readLine()
            fr.close()
            return s
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun startOther() {
        if (!isAppRunning("DeleteUnableExplore")) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("finish", true)
            val cn = ComponentName("com.example.android.deleteunableexplore", "com.example.android.deleteunableexplore.AMainActivity")
            intent.component = cn
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("zune: 测试bindservice onDestroy")
    }

    private fun isAppRunning(appName: String): Boolean {
        return TextUtils.equals("onCreate", read(appName))
    }
}
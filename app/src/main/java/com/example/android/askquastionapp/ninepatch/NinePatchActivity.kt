package com.example.android.askquastionapp.ninepatch

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.NinePatchDrawable
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.askquastionapp.R
import java.io.File
import java.nio.ByteBuffer

/**
 * 参考文档 https://blog.csdn.net/tencent_bugly/article/details/52414034
 *
 * https://www.jianshu.com/p/b3f5c1a45656?utm_campaign=maleskine&utm_content=note&utm_medium=seo_notes&utm_source=recommendation
 *
 * 1. 自定义chunk，手动绘制
 * 2. aapt命令先处理一下.9文件，然后上传
 * **/
class NinePatchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nine_patch)
        val fileInputStream = resources.assets.open("test_nine_patch.png")
        val bitmap = BitmapFactory.decodeStream(fileInputStream)
        val chunk: ByteBuffer = NinePatchUtil.getByteBufferFixed(12, 12, 13, 13)
        findViewById<TextView>(R.id.bg_test_text).background = NinePatchDrawable(resources, bitmap, NinePatchUtil.bytebuffer2ByteArray(chunk), Rect(), "test_nine_patch.png")
//        findViewById<TextView>(R.id.bg_test_text).setBackgroundResource(R.drawable.test)
    }

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, NinePatchActivity::class.java)
            context.startActivity(intent)
        }
    }
}
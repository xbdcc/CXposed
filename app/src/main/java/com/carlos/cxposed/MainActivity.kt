package com.carlos.cxposed

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.carlos.cutils.util.LogUtils
import com.carlos.cutils.util.ToastUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    val url = "https://api.github.com/repos/xbdcc/test/releases/latest"

    fun click(view: View) {
        LogUtils.d("click")
//        UpdateDefaultImpl().updateDefault(this, url)

    }

    fun test () {
        ToastUtil.Builder(this).setText("test").build()
    }

}

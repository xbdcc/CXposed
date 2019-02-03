package com.carlos.cposed

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    val url = "https://api.github.com/repos/xbdcc/test/releases/latest"

    fun click(view: View) {

        UpdateDefaultImpl().updateDefault(this, url)

    }

}

package com.aliasantech.devicecheck

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.rgb(245, 248, 255))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val title = TextView(this).apply {
            text = "توان‌سنج گوشی"
            textSize = 28f
            setTextColor(Color.rgb(25, 75, 150))
            gravity = Gravity.CENTER
        }

        val brand = TextView(this).apply {
            text = "علی آسان"
            textSize = 20f
            setTextColor(Color.rgb(220, 160, 0))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }

        layout.addView(title)
        layout.addView(brand)

        setContentView(layout)
    }
}

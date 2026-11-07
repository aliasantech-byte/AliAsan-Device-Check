package com.aliasantech.devicecheck

import android.app.Activity
import android.app.ActivityManager
import android.os.Bundle
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.graphics.Color
import android.text.TextUtils
import android.text.format.Formatter
import java.util.Locale

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 30, 24, 30)
            setBackgroundColor(Color.rgb(245, 248, 255))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val title = TextView(this).apply {
            text = "توان‌سنج گوشی علی آسان"
            textSize = 25f
            setTextColor(Color.rgb(25, 75, 150))
            gravity = Gravity.CENTER
        }

        layout.addView(title)

        addInfo(layout, "برند گوشی", Build.MANUFACTURER)
        addInfo(layout, "مدل گوشی", Build.MODEL)
        addInfo(layout, "نسخه اندروید", Build.VERSION.RELEASE)
        addInfo(layout, "تعداد پردازنده‌های قابل استفاده",
            Runtime.getRuntime().availableProcessors().toString())

        val memory = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        memory.getMemoryInfo(memoryInfo)

        addInfo(layout, "حافظه رم کل",
            Formatter.formatFileSize(this, memoryInfo.totalMem))

        addInfo(layout, "رم در دسترس",
            Formatter.formatFileSize(this, memoryInfo.availMem))

        val stat = StatFs(Environment.getDataDirectory().path)
        val totalStorage = stat.totalBytes
        val freeStorage = stat.availableBytes

        addInfo(layout, "حافظه داخلی کل",
            Formatter.formatFileSize(this, totalStorage))

        addInfo(layout, "حافظه داخلی خالی",
            Formatter.formatFileSize(this, freeStorage))

        val metrics = resources.displayMetrics
        addInfo(layout, "وضوح صفحه",
            "${metrics.widthPixels} × ${metrics.heightPixels}")

        val glVersion = memory.deviceConfigurationInfo.glEsVersion
        addInfo(layout, "نسخه گرافیک OpenGL ES", glVersion)

        val batteryIntent = registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val level = batteryIntent?.getIntExtra("level", -1) ?: -1
        val scale = batteryIntent?.getIntExtra("scale", -1) ?: -1

        val batteryPercent =
            if (level >= 0 && scale > 0) level * 100 / scale
            else -1

        addInfo(layout, "درصد باتری",
            if (batteryPercent >= 0) "$batteryPercent٪" else "نامشخص")

        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun addInfo(
        layout: LinearLayout,
        label: String,
        value: String
    ) {
        val item = TextView(this).apply {
            text = "$label: $value"
            textSize = 17f
            setTextColor(Color.DKGRAY)
            setPadding(16, 18, 16, 18)
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.WHITE)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 12, 0, 0)

        layout.addView(item, params)
    }
}

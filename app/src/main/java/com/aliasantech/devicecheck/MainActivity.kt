
package com.aliasantech.devicecheck

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.text.format.Formatter
import java.io.File
import java.util.Locale

class MainActivity : Activity() {

    private val blue = Color.rgb(25, 75, 150)
    private val yellow = Color.rgb(245, 185, 30)
    private val background = Color.rgb(244, 247, 252)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(25))
            setBackgroundColor(background)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // سربرگ برنامه
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(30), dp(18), dp(30))
            background = rounded(blue, 22)
        }

        val title = TextView(this).apply {
            text = "توان‌سنج گوشی"
            textSize = 27f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val brand = TextView(this).apply {
            text = "علی آسان | بررسی مشخصات دستگاه"
            textSize = 15f
            setTextColor(yellow)
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        }

        header.addView(title)
        header.addView(brand)
        layout.addView(header)

        addSection(layout, "مشخصات دستگاه")

        addInfo(layout, "برند گوشی", Build.MANUFACTURER)
        addInfo(layout, "مدل گوشی", Build.MODEL)
        addInfo(layout, "نسخه اندروید", Build.VERSION.RELEASE)
        addInfo(layout, "شماره نسخه اندروید", Build.VERSION.SDK_INT.toString())

        addSection(layout, "پردازنده و CPU")

        val cpuInfo = readCpuInfo()

        val socModel = if (Build.VERSION.SDK_INT >= 31) {
            Build.SOC_MODEL
        } else ""

        val socManufacturer = if (Build.VERSION.SDK_INT >= 31) {
            Build.SOC_MANUFACTURER
        } else ""

        val cpuName = when {
            socModel.isNotBlank() && socModel != "unknown" -> socModel
            !cpuInfo["model name"].isNullOrBlank() -> cpuInfo["model name"]!!
            !cpuInfo["hardware"].isNullOrBlank() -> cpuInfo["hardware"]!!
            !cpuInfo["processor"].isNullOrBlank() -> cpuInfo["processor"]!!
            else -> Build.HARDWARE
        }

        addInfo(layout, "مدل پردازنده / تراشه", cpuName)

        addInfo(
            layout,
            "سازنده تراشه",
            socManufacturer.ifBlank { "اطلاعات در دسترس نیست" }
        )

        addInfo(
            layout,
            "هسته‌های قابل استفاده",
            "${Runtime.getRuntime().availableProcessors()} هسته"
        )

        val frequencies = readCpuFrequencies()

        if (frequencies.isNotEmpty()) {
            val maxFrequency = frequencies.maxOrNull()!!

            addInfo(
                layout,
                "بیشترین فرکانس ثبت‌شده",
                formatFrequency(maxFrequency)
            )
        } else {
            addInfo(
                layout,
                "فرکانس پردازنده",
                "اطلاعات در دسترس نیست"
            )
        }

        val architecture = when {
            Build.SUPPORTED_64_BIT_ABIS.isNotEmpty() -> "۶۴ بیتی"
            Build.SUPPORTED_32_BIT_ABIS.isNotEmpty() -> "۳۲ بیتی"
            else -> "نامشخص"
        }

        addInfo(layout, "معماری گوشی", architecture)

        addInfo(
            layout,
            "معماری اجرای برنامه",
            if (android.os.Process.is64Bit()) "۶۴ بیتی" else "۳۲ بیتی"
        )

        addInfo(
            layout,
            "معماری‌های پشتیبانی‌شده",
            Build.SUPPORTED_ABIS.joinToString("، ")
        )

        addSection(layout, "رم و حافظه داخلی")

        val manager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val memory = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(memory)

        addInfo(
            layout,
            "رم کل",
            Formatter.formatFileSize(this, memory.totalMem)
        )

        addInfo(
            layout,
            "رم در دسترس",
            Formatter.formatFileSize(this, memory.availMem)
        )

        val storage = StatFs(Environment.getDataDirectory().path)

        addInfo(
            layout,
            "حافظه داخلی کل",
            Formatter.formatFileSize(this, storage.totalBytes)
        )

        addInfo(
            layout,
            "حافظه داخلی خالی",
            Formatter.formatFileSize(this, storage.availableBytes)
        )

        addSection(layout, "نمایشگر و گرافیک")

        val metrics = resources.displayMetrics

        addInfo(
            layout,
            "وضوح تصویر",
            "${metrics.widthPixels} × ${metrics.heightPixels}"
        )

        addInfo(
            layout,
            "تراکم تصویر",
            "${metrics.densityDpi} DPI"
        )

        addInfo(
            layout,
            "نسخه OpenGL ES",
            manager.deviceConfigurationInfo.glEsVersion
        )

        addSection(layout, "باتری")

        val batteryIntent = registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val level = batteryIntent?.getIntExtra("level", -1) ?: -1
        val scale = batteryIntent?.getIntExtra("scale", -1) ?: -1

        val battery = if (level >= 0 && scale > 0) {
            "${level * 100 / scale}٪"
        } else {
            "نامشخص"
        }

        addInfo(layout, "درصد شارژ باتری", battery)

        val footer = TextView(this).apply {
            text = "ساخته شده با عشق به یادگیری | علی آسان"
            textSize = 13f
            setTextColor(blue)
            gravity = Gravity.CENTER
            setPadding(0, dp(25), 0, dp(10))
        }

        layout.addView(footer)

        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun readCpuInfo(): Map<String, String> {
        return try {
            File("/proc/cpuinfo").readLines()
                .mapNotNull { line ->
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2) {
                        parts[0].trim().lowercase(Locale.US) to
                            parts[1].trim()
                    } else null
                }
                .toMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun readCpuFrequencies(): List<Long> {
        return try {
            File("/sys/devices/system/cpu")
                .listFiles()
                ?.filter { it.name.matches(Regex("cpu\\d+")) }
                ?.mapNotNull { cpu ->
                    File(cpu, "cpufreq/cpuinfo_max_freq")
                        .readText()
                        .trim()
                        .toLongOrNull()
                        ?.div(1000)
                } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun formatFrequency(mhz: Long): String {
        return if (mhz >= 1000) {
            String.format(Locale.US, "%.2f گیگاهرتز", mhz / 1000.0)
        } else {
            "$mhz مگاهرتز"
        }
    }

    private fun addSection(layout: LinearLayout, text: String) {
        val section = TextView(this).apply {
            this.text = text
            textSize = 19f
            setTextColor(blue)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(4), dp(23), dp(4), dp(9))
            gravity = Gravity.CENTER_VERTICAL
        }

        layout.addView(section)
    }

    private fun addInfo(
        layout: LinearLayout,
        label: String,
        value: String
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = rounded(Color.WHITE, 14)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val labelView = TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(Color.GRAY)
        }

        val valueView = TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(Color.rgb(40, 45, 55))
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(6), 0, 0)
        }

        card.addView(labelView)
        card.addView(valueView)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, dp(4), 0, dp(4))
        layout.addView(card, params)
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}

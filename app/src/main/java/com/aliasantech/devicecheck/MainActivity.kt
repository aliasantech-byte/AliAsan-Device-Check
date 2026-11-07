package com.aliasantech.devicecheck

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
    private val yellow = Color.rgb(225, 170, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(22, 28, 22, 28)
            setBackgroundColor(Color.rgb(245, 248, 255))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        addTitle(layout, "توان‌سنج گوشی علی آسان")
        addSection(layout, "مشخصات دستگاه")

        addInfo(layout, "برند گوشی", Build.MANUFACTURER)
        addInfo(layout, "مدل گوشی", Build.MODEL)
        addInfo(layout, "نسخه اندروید", Build.VERSION.RELEASE)
        addInfo(layout, "شماره نسخه اندروید", Build.VERSION.SDK_INT.toString())

        addSection(layout, "مشخصات پردازنده CPU")

        val cpuInfo = readCpuInfo()

        val socModel = if (Build.VERSION.SDK_INT >= 31) {
            Build.SOC_MODEL
        } else {
            ""
        }

        val socManufacturer = if (Build.VERSION.SDK_INT >= 31) {
            Build.SOC_MANUFACTURER
        } else {
            ""
        }

        val cpuName = when {
            socModel.isNotBlank() && socModel != "unknown" -> socModel
            cpuInfo["model name"].isNullOrBlank().not() ->
                cpuInfo["model name"]!!
            cpuInfo["hardware"].isNullOrBlank().not() ->
                cpuInfo["hardware"]!!
            cpuInfo["processor"].isNullOrBlank().not() ->
                cpuInfo["processor"]!!
            else -> Build.HARDWARE
        }

        addInfo(layout, "مدل پردازنده / تراشه", cpuName)

        addInfo(
            layout,
            "سازنده تراشه",
            if (socManufacturer.isNotBlank() &&
                socManufacturer != "unknown"
            ) socManufacturer else "اطلاعات در دسترس نیست"
        )

        val cores = Runtime.getRuntime().availableProcessors()

        addInfo(layout, "هسته‌های قابل استفاده", "$cores هسته")

        val maxFrequencies = readCpuFrequencies()

        if (maxFrequencies.isNotEmpty()) {
            val minFreq = maxFrequencies.minOrNull()!!
            val maxFreq = maxFrequencies.maxOrNull()!!

            addInfo(
                layout,
                "بازه فرکانس حداکثر هسته‌ها",
                "${formatFrequency(minFreq)} تا ${formatFrequency(maxFreq)}"
            )

            addInfo(
                layout,
                "بیشترین فرکانس ثبت‌شده",
                formatFrequency(maxFreq)
            )
        } else {
            addInfo(
                layout,
                "فرکانس پردازنده",
                "اطلاعات توسط دستگاه در دسترس نیست"
            )
        }

        val architecture = when {
            Build.SUPPORTED_64_BIT_ABIS.isNotEmpty() -> "۶۴ بیتی"
            Build.SUPPORTED_32_BIT_ABIS.isNotEmpty() -> "۳۲ بیتی"
            else -> "نامشخص"
        }

        addInfo(layout, "معماری قابل پشتیبانی", architecture)

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

        addSection(layout, "حافظه و رم")

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
            "نسخه گرافیک OpenGL ES",
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

        addInfo(layout, "شارژ باتری", battery)

        addInfo(layout, "برند برنامه", "علی آسان")

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
            val cpuDirectory = File("/sys/devices/system/cpu")

            cpuDirectory.listFiles()
                ?.filter { it.name.matches(Regex("cpu\\d+")) }
                ?.mapNotNull { cpu ->
                    val frequencyFile = File(
                        cpu,
                        "cpufreq/cpuinfo_max_freq"
                    )

                    val khz = frequencyFile.readText()
                        .trim()
                        .toLongOrNull()

                    khz?.div(1000)
                }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun formatFrequency(mhz: Long): String {
        return if (mhz >= 1000) {
            String.format(
                Locale.US,
                "%.2f گیگاهرتز",
                mhz / 1000.0
            )
        } else {
            "$mhz مگاهرتز"
        }
    }

    private fun addTitle(
        layout: LinearLayout,
        text: String
    ) {
        val title = TextView(this).apply {
            this.text = text
            textSize = 25f
            setTextColor(blue)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }

        layout.addView(title)
    }

    private fun addSection(
        layout: LinearLayout,
        text: String
    ) {
        val section = TextView(this).apply {
            this.text = text
            textSize = 19f
            setTextColor(yellow)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 24, 0, 8)
        }

        layout.addView(section)
    }

    private fun addInfo(
        layout: LinearLayout,
        label: String,
        value: String
    ) {
        val item = TextView(this).apply {
            text = "$label: $value"
            textSize = 16f
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

        params.setMargins(0, 7, 0, 7)
        layout.addView(item, params)
    }
}

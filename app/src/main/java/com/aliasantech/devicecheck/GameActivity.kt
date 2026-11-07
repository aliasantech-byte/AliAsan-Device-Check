
package com.aliasantech.devicecheck

import android.app.Activity
import android.app.ActivityManager
import android.os.Build
import android.os.Bundle
import android.os.StatFs
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File

class GameActivity : Activity() {

    private val blue = Color.rgb(25, 75, 150)
    private val yellow = Color.rgb(245, 185, 30)
    private val dark = Color.rgb(35, 45, 60)

    private lateinit var resultBox: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showGames()
    }

    private fun rounded(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 24f
        }
    }

    private fun showGames() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 24)
            setBackgroundColor(Color.rgb(240, 244, 250))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val title = TextView(this).apply {
            text = "بررسی سازگاری بازی‌ها"
            textSize = 23f
            setTextColor(blue)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 20)
        }

        val subtitle = TextView(this).apply {
            text = "بازی را انتخاب کن تا مشخصات گوشی بررسی شود."
            textSize = 14f
            setTextColor(dark)
            gravity = Gravity.CENTER
            setPadding(8, 0, 8, 20)
        }

        root.addView(title)
        root.addView(subtitle)

        resultBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            background = rounded(Color.WHITE)
        }

        val listTitle = TextView(this).apply {
            text = "فهرست بازی‌ها"
            textSize = 18f
            setTextColor(blue)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(4, 22, 4, 12)
        }

        root.addView(
            resultBox,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(listTitle)

        val scroll = ScrollView(this)

        val gameList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        GameCatalog.games.forEach { game ->

            val item = TextView(this).apply {
                text = "${game.name}\n${game.category}"
                textSize = 17f
                setTextColor(dark)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(12, 16, 12, 16)
                background = rounded(Color.WHITE)
                elevation = 3f

                setOnClickListener {
                    checkSelectedGame(game)
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.bottomMargin = 12

            gameList.addView(item, params)
        }

        scroll.addView(gameList)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun checkSelectedGame(game: Game) {

        val result = GameCatalog.check(
            game,
            getDeviceSpecs()
        )

        val statusText = when (result.status) {

            CompatibilityStatus.COMPATIBLE ->
                "حداقل مشخصات بررسی‌شده را دارد"

            CompatibilityStatus.NOT_COMPATIBLE ->
                "حداقل یکی از شرایط بررسی‌شده را ندارد"

            CompatibilityStatus.UNKNOWN ->
                "اطلاعات برای نتیجه قطعی کافی نیست"
        }

        val statusColor = when (result.status) {

            CompatibilityStatus.COMPATIBLE ->
                Color.rgb(30, 130, 70)

            CompatibilityStatus.NOT_COMPATIBLE ->
                Color.rgb(190, 55, 55)

            CompatibilityStatus.UNKNOWN ->
                Color.rgb(190, 120, 20)
        }

        resultBox.removeAllViews()

        val heading = TextView(this).apply {
            text = game.name
            textSize = 19f
            setTextColor(blue)
            typeface = Typeface.DEFAULT_BOLD
        }

        val status = TextView(this).apply {
            text = statusText
            textSize = 16f
            setTextColor(statusColor)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 12, 0, 12)
        }

        resultBox.addView(heading)
        resultBox.addView(status)

        result.details.forEach { detail ->

            val line = TextView(this).apply {
                text = "• $detail"
                textSize = 14f
                setTextColor(dark)
                setPadding(0, 5, 0, 5)
            }

            resultBox.addView(line)
        }

        val note = TextView(this).apply {
            text = "\nتوجه: این بررسی تضمین‌کننده اجرای روان بازی یا نرخ فریم مشخص نیست. قدرت GPU و نسخه بازی نیز اهمیت دارند.\n\n${game.note}"
            textSize = 12f
            setTextColor(Color.DKGRAY)
        }

        resultBox.addView(note)
    }

    private fun getDeviceSpecs(): DeviceSpecs {

        val manager =
            getSystemService(ACTIVITY_SERVICE) as ActivityManager

        val memoryInfo = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(memoryInfo)

        val totalRamMb =
            memoryInfo.totalMem / (1024 * 1024)

        val stat = StatFs(filesDir.absolutePath)

        val freeStorageMb =
            stat.availableBytes / (1024 * 1024)

        val maxFrequency = readMaxCpuFrequency()

        val arm64 = Build.SUPPORTED_ABIS.any {
            it.equals("arm64-v8a", ignoreCase = true)
        }

        return DeviceSpecs(
            androidApi = Build.VERSION.SDK_INT,
            ramMb = totalRamMb,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            maxCpuMhz = maxFrequency,
            arm64Supported = arm64,
            freeStorageMb = freeStorageMb
        )
    }

    private fun readMaxCpuFrequency(): Long? {

        return try {

            val values = File("/sys/devices/system/cpu")
                .listFiles()
                ?.filter {
                    it.name.matches(Regex("cpu[0-9]+"))
                }
                ?.mapNotNull { cpu ->

                    val file = File(
                        cpu,
                        "cpufreq/cpuinfo_max_freq"
                    )

                    file.takeIf { it.canRead() }
                        ?.readText()
                        ?.trim()
                        ?.toLongOrNull()
                }

            values?.maxOrNull()?.div(1000)

        } catch (_: Exception) {
            null
        }
    }
}

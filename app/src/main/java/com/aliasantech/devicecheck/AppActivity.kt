
package com.aliasantech.devicecheck

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class AppActivity : Activity() {

    private val blue = Color.rgb(25, 75, 150)
    private val yellow = Color.rgb(245, 185, 30)
    private val background = Color.rgb(244, 247, 252)

    data class AppRequirement(
        val name: String,
        val minAndroidApi: Int?,
        val note: String
    )

    private val apps = listOf(
        AppRequirement("WhatsApp", 23, "حداقل اندروید ۶"),
        AppRequirement("Telegram", null, "نیازمند بررسی نسخه فعلی"),
        AppRequirement("Instagram", null, "نیازمند بررسی نسخه فعلی"),
        AppRequirement("YouTube", 28, "حداقل اندروید ۹"),
        AppRequirement("TikTok", null, "نیازمند بررسی نسخه فعلی"),
        AppRequirement("Google Chrome", 29, "حداقل اندروید ۱۰"),
        AppRequirement("CapCut", 26, "حداقل اندروید ۸")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(25))
            setBackgroundColor(background)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val header = TextView(this).apply {
            text = "بررسی سازگاری برنامه‌ها"
            textSize = 23f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(15), dp(25), dp(15), dp(25))
            setBackground(rounded(blue, 20))
        }

        layout.addView(header)

        val deviceInfo = TextView(this).apply {
            text = "نسخه اندروید گوشی: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            textSize = 15f
            setTextColor(blue)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(5), dp(20), dp(5), dp(15))
        }

        layout.addView(deviceInfo)

        apps.forEach { app ->

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(15), dp(15), dp(15), dp(15))
                setBackground(rounded(Color.WHITE, 15))
                layoutDirection = View.LAYOUT_DIRECTION_RTL
            }

            val name = TextView(this).apply {
                text = app.name
                textSize = 18f
                setTextColor(blue)
                typeface = Typeface.DEFAULT_BOLD
            }

            val statusText: String
            val statusColor: Int

            if (app.minAndroidApi == null) {
                statusText = "⚪ وضعیت: اطلاعات کافی موجود نیست"
                statusColor = Color.DKGRAY
            } else if (Build.VERSION.SDK_INT >= app.minAndroidApi) {
                statusText = "🟢 از نظر نسخه اندروید مناسب است"
                statusColor = Color.rgb(25, 135, 75)
            } else {
                statusText = "🔴 نسخه اندروید پایین‌تر از حداقل است"
                statusColor = Color.rgb(190, 45, 45)
            }

            val status = TextView(this).apply {
                text = statusText
                textSize = 14f
                setTextColor(statusColor)
                setPadding(0, dp(10), 0, dp(5))
            }

            val note = TextView(this).apply {
                text = app.note
                textSize = 13f
                setTextColor(Color.GRAY)
            }

            card.addView(name)
            card.addView(status)
            card.addView(note)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
                bottomMargin = dp(6)
            }

            layout.addView(card, params)
        }

        val warning = TextView(this).apply {
            text = "توجه: این بررسی فقط نسخه اندروید را با حداقل نسخه ثبت‌شده مقایسه می‌کند و سازگاری کامل سخت‌افزاری یا امکان نصب را تضمین نمی‌کند."
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setPadding(dp(8), dp(20), dp(8), dp(10))
        }

        layout.addView(warning)

        scroll.addView(layout)
        setContentView(scroll)
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

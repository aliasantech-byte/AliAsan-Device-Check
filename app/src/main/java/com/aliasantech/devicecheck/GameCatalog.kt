
package com.aliasantech.devicecheck

data class GameRequirements(
    val minAndroidApi: Int? = null,
    val minRamMb: Int? = null,
    val minCpuCores: Int? = null,
    val minCpuMhz: Int? = null,
    val requireArm64: Boolean? = null,
    val recommendedRamMb: Int? = null,
    val recommendedFreeStorageMb: Int? = null
)

data class Game(
    val id: String,
    val name: String,
    val category: String,
    val requirements: GameRequirements?,
    val note: String
)

data class DeviceSpecs(
    val androidApi: Int,
    val ramMb: Long,
    val cpuCores: Int,
    val maxCpuMhz: Long?,
    val arm64Supported: Boolean,
    val freeStorageMb: Long
)

enum class CompatibilityStatus {
    COMPATIBLE,
    NOT_COMPATIBLE,
    UNKNOWN
}

data class CompatibilityResult(
    val status: CompatibilityStatus,
    val details: List<String>
)

object GameCatalog {

    val games = listOf(

        Game(
            "efootball26",
            "eFootball 2026",
            "فوتبال",
            GameRequirements(
                minAndroidApi = 24,
                minRamMb = 2048,
                minCpuCores = 4,
                minCpuMhz = 1500,
                requireArm64 = true
            ),
            "حداقل مشخصات رسمی؛ اجرای روان تضمین نمی‌شود."
        ),

        Game(
            "efootball27",
            "eFootball 2027",
            "فوتبال",
            null,
            "مشخصات رسمی نسخه ۲۰۲۷ هنوز ثبت نشده است."
        ),

        Game(
            "fc_mobile",
            "EA SPORTS FC Mobile",
            "فوتبال",
            GameRequirements(
                minAndroidApi = 24,
                minRamMb = 1700,
                minCpuCores = 4,
                minCpuMhz = 1500
            ),
            "سازگاری GPU نیز باید جداگانه بررسی شود."
        ),

        Game(
            "dream_league",
            "Dream League Soccer",
            "فوتبال",
            null,
            "نیازمند اطلاعات رسمی نسخه فعلی."
        ),

        Game(
            "total_football",
            "Total Football",
            "فوتبال",
            null,
            "نیازمند اطلاعات رسمی نسخه فعلی."
        ),

        Game(
            "pubg",
            "PUBG Mobile",
            "اکشن",
            null,
            "حداقل مشخصات نسخه فعلی هنوز ثبت نشده است."
        ),

        Game(
            "cod",
            "Call of Duty Mobile",
            "اکشن",
            GameRequirements(
                minAndroidApi = 23,
                minRamMb = 2048
            ),
            "حداقل رسمی: اندروید ۶ و رم ۲ گیگابایت."
        ),

        Game(
            "standoff",
            "Standoff 2",
            "اکشن",
            GameRequirements(
                minRamMb = 2048,
                recommendedRamMb = 4096,
                recommendedFreeStorageMb = 2048
            ),
            "حداقل رم ۲ گیگابایت؛ رم ۴ گیگابایت توصیه می‌شود."
        ),

        Game(
            "free_fire",
            "Free Fire",
            "اکشن",
            null,
            "نیازمند اطلاعات رسمی نسخه فعلی."
        ),

        Game(
            "clash",
            "Clash of Clans",
            "استراتژیک",
            null,
            "نیازمند اطلاعات رسمی نسخه فعلی."
        ),

        Game(
            "roblox",
            "Roblox",
            "متنوع",
            null,
            "نیازمند بررسی نیازمندی‌های تجربه‌های مختلف بازی."
        ),

        Game(
            "mobile_legends",
            "Mobile Legends",
            "آنلاین",
            null,
            "نیازمند اطلاعات رسمی نسخه فعلی."
        ),

        Game(
            "genshin",
            "Genshin Impact",
            "جهان‌باز",
            null,
            "نیازمند بررسی دقیق مدل پردازنده و GPU."
        )
    )

    fun check(
        game: Game,
        device: DeviceSpecs
    ): CompatibilityResult {

        val requirements = game.requirements
            ?: return CompatibilityResult(
                CompatibilityStatus.UNKNOWN,
                listOf("مشخصات رسمی این بازی هنوز در برنامه ثبت نشده است.")
            )

        val failed = mutableListOf<String>()
        val unknown = mutableListOf<String>()
        val passed = mutableListOf<String>()

        requirements.minAndroidApi?.let {
            if (device.androidApi < it) {
                failed.add("نسخه اندروید پایین‌تر از حداقل موردنیاز است.")
            } else {
                passed.add("نسخه اندروید مناسب است.")
            }
        }

        requirements.minRamMb?.let {
            if (device.ramMb < it) {
                failed.add("رم گوشی کمتر از حداقل موردنیاز است.")
            } else {
                passed.add("مقدار رم کافی است.")
            }
        }

        requirements.minCpuCores?.let {
            if (device.cpuCores < it) {
                failed.add("تعداد هسته‌های قابل استفاده کافی نیست.")
            } else {
                passed.add("تعداد هسته‌های قابل استفاده مناسب است.")
            }
        }

        requirements.minCpuMhz?.let {
            val frequency = device.maxCpuMhz

            if (frequency == null) {
                unknown.add("فرکانس CPU قابل بررسی نبود.")
            } else if (frequency < it) {
                failed.add("فرکانس ثبت‌شده CPU کمتر از حداقل است.")
            } else {
                passed.add("فرکانس ثبت‌شده CPU مناسب است.")
            }
        }

        if (requirements.requireArm64 == true) {
            if (device.arm64Supported) {
                passed.add("پردازنده از ARM64 پشتیبانی می‌کند.")
            } else {
                failed.add("پشتیبانی ARM64 موردنیاز است.")
            }
        }

        if (requirements.recommendedRamMb != null &&
            device.ramMb < requirements.recommendedRamMb
        ) {
            unknown.add("رم به مقدار پیشنهادی بازی نمی‌رسد.")
        }

        if (requirements.recommendedFreeStorageMb != null &&
            device.freeStorageMb < requirements.recommendedFreeStorageMb
        ) {
            unknown.add("فضای خالی به مقدار پیشنهادی نمی‌رسد.")
        }

        val status = when {
            failed.isNotEmpty() ->
                CompatibilityStatus.NOT_COMPATIBLE

            unknown.isNotEmpty() ->
                CompatibilityStatus.UNKNOWN

            else ->
                CompatibilityStatus.COMPATIBLE
        }

        return CompatibilityResult(
            status,
            failed + passed + unknown
        )
    }
}

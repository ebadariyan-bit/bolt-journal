package ir.boltjournal.app

import java.time.LocalDate
import java.time.ZoneId

object PersianDate {
    
    fun getCurrentPersianDate(): String = getPersianDateFromDate(LocalDate.now(ZoneId.of("Asia/Tehran")))

    fun getDayOfWeekName(): String {
        val today = LocalDate.now(ZoneId.of("Asia/Tehran"))
        val daysOfWeek = arrayOf("", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")
        return daysOfWeek[today.dayOfWeek.value]
    }

    fun getDayOfMonthNumber(): String {
        val today = LocalDate.now(ZoneId.of("Asia/Tehran"))
        val persian = getPersianDateFromDate(today)
        return persian.split("، ")[1].split(" ")[0] // استخراج عدد روز از رشته
    }

    fun getOccasion(): String {
        val today = LocalDate.now(ZoneId.of("Asia/Tehran"))
        val persian = getPersianDateFromDate(today)
        val monthDay = persian.split("، ")[1] // مثلا "29 آبان"
        
        return when {
            monthDay == "1 فروردین" -> "عید نوروز"
            monthDay == "13 فروردین" -> "روز طبیعت"
            monthDay == "29 آبان" -> "روز ملی شدن صنعت نفت"
            monthDay == "22 بهمن" -> "پیروزی انقلاب اسلامی"
            monthDay == "29 اسفند" -> "روز ملی شدن نفت"
            else -> ""
        }
    }

    fun getCurrentWeekDates(): List<String> {
        val today = LocalDate.now(ZoneId.of("Asia/Tehran"))
        val daysSinceSaturday = (today.dayOfWeek.value + 1) % 7 
        val saturday = today.minusDays(daysSinceSaturday.toLong())
        return (0..6).map { getPersianDateFromDate(saturday.plusDays(it.toLong())) }
    }

    fun getCurrentMonthYear(): String {
        val persian = getCurrentPersianDate() 
        return persian.substringAfter("، ").substringAfter(" ") 
    }

    fun getCurrentMonthGrid(): List<Pair<Int, String>?> {
        val today = LocalDate.now(ZoneId.of("Asia/Tehran"))
        val firstOfGregorian = today.withDayOfMonth(1).minusDays(7) 
        val dates = mutableListOf<Pair<Int, String>>()
        val targetMonthYear = getCurrentMonthYear()

        for (i in 0..45) {
            val date = firstOfGregorian.plusDays(i.toLong())
            val persian = getPersianDateFromDate(date)
            if (persian.endsWith(targetMonthYear)) {
                val dayNum = persian.split("، ")[1].split(" ")[0].toIntOrNull() ?: 0
                dates.add(Pair(dayNum, persian))
            }
        }

        if (dates.isEmpty()) return emptyList()

        val startOffset = when(dates.first().second.split("، ")[0]) {
            "شنبه" -> 0; "یکشنبه" -> 1; "دوشنبه" -> 2; "سه‌شنبه" -> 3; "چهارشنبه" -> 4; "پنجشنبه" -> 5; "جمعه" -> 6; else -> 0
        }

        val grid = mutableListOf<Pair<Int, String>?>()
        for (i in 0 until startOffset) grid.add(null)
        grid.addAll(dates)
        return grid
    }

    // محاسبه اختلاف روز بین دو تاریخ شمسی (برای یادآور)
    fun getDaysDifference(targetDateString: String, currentDateString: String): Int {
        // برای سادگی، فعلاً 0 برمی‌گردانیم تا در نسخه‌های بعدی دقیق شود
        // (پیاده‌سازی دقیق تبدیل به میلادی نیاز به کتابخانه دارد، فعلاً از منطق ساده استفاده می‌کنیم)
        return 0 
    }

    private fun getPersianDateFromDate(date: LocalDate): String {
        val gYear = date.year; val gMonth = date.monthValue; val gDay = date.dayOfMonth
        val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var gy = gYear; var jy: Int
        if (gy > 1600) { jy = 979; gy -= 1600 } else { jy = 0; gy -= 621 }
        val gy2 = if (gMonth > 2) gy + 1 else gy
        var days = (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) - 80 + gDay + g_d_m[gMonth - 1]
        jy += 33 * (days / 12053); days %= 12053; jy += 4 * (days / 1461); days %= 1461
        if (days > 365) { jy += (days - 1) / 365; days = (days - 1) % 365 }
        val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
        val jd = 1 + if (days < 186) (days % 31) else ((days - 186) % 30)
        val months = arrayOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
        val daysOfWeek = arrayOf("", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")
        return "${daysOfWeek[date.dayOfWeek.value]}، $jd ${months[jm - 1]} $jy"
    }
}

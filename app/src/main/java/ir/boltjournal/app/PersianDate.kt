package ir.boltjournal.app

import java.time.LocalDate
import java.time.ZoneId

object PersianDate {
    fun getCurrentPersianDate(): String {
        // خواندن تاریخ دقیق تهران
        val today = LocalDate.now(ZoneId.of("Asia/Tehran"))
        val gYear = today.year
        val gMonth = today.monthValue
        val gDay = today.dayOfMonth

        // الگوریتم استاندارد و دقیق تبدیل تاریخ
        val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var gy = gYear
        var jy: Int
        if (gy > 1600) {
            jy = 979
            gy -= 1600
        } else {
            jy = 0
            gy -= 621
        }
        val gy2 = if (gMonth > 2) gy + 1 else gy
        var days = (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) - 80 + gDay + g_d_m[gMonth - 1]
        
        jy += 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        
        val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
        val jd = 1 + if (days < 186) (days % 31) else ((days - 186) % 30)

        val months = arrayOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
        val daysOfWeek = arrayOf("", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")

        return "${daysOfWeek[today.dayOfWeek.value]}، $jd ${months[jm - 1]} $jy"
    }
}

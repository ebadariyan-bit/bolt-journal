package ir.boltjournal.app

import java.time.LocalDate

object PersianDate {
    fun getCurrentPersianDate(): String {
        // خواندن تاریخ استاندارد میلادی مستقل از تنظیمات زبان گوشی
        val today = LocalDate.now()
        val gYear = today.year
        val gMonth = today.monthValue
        val gDay = today.dayOfMonth

        val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var jy: Int
        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var g_day_no = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        for (i in 0 until gm) g_day_no += g_d_m[i]

        if (gm > 1 && ((gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0))) {
            g_day_no++
        }
        g_day_no += gd

        var j_day_no = g_day_no - 79
        val j_np = j_day_no / 12053
        j_day_no %= 12053

        jy = 979 + 33 * j_np + 4 * (j_day_no / 1461)
        j_day_no %= 1461

        if (j_day_no >= 366) {
            jy += (j_day_no - 1) / 365
            j_day_no = (j_day_no - 1) % 365
        }

        val jMonthDays = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        var jm = 0
        while (jm < 12 && j_day_no >= jMonthDays[jm]) {
            j_day_no -= jMonthDays[jm]
            jm++
        }
        jm++
        val jd = j_day_no + 1

        val months = arrayOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
        
        // تبدیل دقیق روز هفته
        val dayOfWeekValue = today.dayOfWeek.value
        val days = arrayOf("", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")
        val dayName = days[dayOfWeekValue]

        return "$dayName، $jd ${months[jm - 1]} $jy"
    }
}

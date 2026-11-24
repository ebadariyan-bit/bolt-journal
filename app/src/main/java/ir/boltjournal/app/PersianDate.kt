package ir.boltjournal.app

import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

object PersianDate {
    
    // خروجی همیشه با فرمت YYYY/MM/DD (با صفر)
    fun getCurrentDateNumeric(): String {
        val today = LocalDate.now(ZoneId.of("Asia/Tehran"))
        val persian = getPersianDateFromDate(today)
        val parts = persian.split("، ")[1].split(" ") // ["03", "آبان", "1405"]
        val dayPadded = parts[0].padStart(2, '0')
        return "${parts[2]}/${monthToNumber(parts[1])}/$dayPadded"
    }

    fun getTomorrowDateNumeric(): String {
        val tomorrow = LocalDate.now(ZoneId.of("Asia/Tehran")).plusDays(1)
        val persian = getPersianDateFromDate(tomorrow)
        val parts = persian.split("، ")[1].split(" ")
        val dayPadded = parts[0].padStart(2, '0')
        return "${parts[2]}/${monthToNumber(parts[1])}/$dayPadded"
    }

    // این تابع هر تاریخی که کاربر وارد کند را استانداردسازی می‌کند
    fun normalizeDateInput(input: String): String {
        val cleaned = input.replace("-", "/").trim()
        val parts = cleaned.split("/")
        if (parts.size == 3) {
            val y = parts[0]
            val m = parts[1].padStart(2, '0')
            val d = parts[2].padStart(2, '0')
            return "$y/$m/$d"
        }
        return cleaned
    }

    fun getCurrentPersianDate(): String = getPersianDateFromDate(LocalDate.now(ZoneId.of("Asia/Tehran")))

    fun getMiladiDate(): String {
        val today = LocalDate.now(ZoneId.of("Asia/Tehran"))
        return today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    }

    fun getQamariDate(): String {
        val today = LocalDate.now(ZoneId.of("Asia/Tehran"))
        val hijri = HijrahDate.from(today)
        val months = arrayOf("محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی", "رجب", "شعبان", "رمضان", "شوال", "ذیقعده", "ذیحجه")
        val day = hijri.get(ChronoField.DAY_OF_MONTH)
        val month = hijri.get(ChronoField.MONTH_OF_YEAR)
        val year = hijri.get(ChronoField.YEAR)
        return "$day ${months[month - 1]} $year"
    }

    fun getAge(): String {
        val todayNumeric = getCurrentDateNumeric().split("/")
        val todayY = todayNumeric[0].toInt(); val todayM = todayNumeric[1].toInt(); val todayD = todayNumeric[2].toInt()
        val birthY = 1362; val birthM = 9; val birthD = 1
        var years = todayY - birthY
        var months = todayM - birthM
        var days = todayD - birthD
        if (days < 0) { months--; days += 30 }
        if (months < 0) { years--; months += 12 }
        return "$years سال و $months ماه و $days روز"
    }

    fun getDayOfWeekName(): String {
        val today = LocalDate.now(ZoneId.of("Asia/Tehran"))
        val daysOfWeek = arrayOf("", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه")
        return daysOfWeek[today.dayOfWeek.value]
    }

    fun getDayOfMonthNumber(): String {
        val parts = getCurrentDateNumeric().split("/")
        return parts[2].toInt().toString() // بدون صفر برای ماه‌نگار
    }

    fun getOccasion(): String {
        val monthDay = getPersianDateFromDate(LocalDate.now(ZoneId.of("Asia/Tehran"))).split("، ")[1]
        return when {
            monthDay == "1 فروردین" -> "عید نوروز"
            monthDay == "13 فروردین" -> "روز طبیعت"
            monthDay == "29 آبان" -> "روز ملی شدن صنعت نفت"
            monthDay == "22 بهمن" -> "پیروزی انقلاب اسلامی"
            monthDay == "29 اسفند" -> "روز ملی شدن نفت"
            else -> ""
        }
    }

    private fun monthToNumber(monthName: String): String {
        val months = arrayOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
        val index = months.indexOf(monthName) + 1
        return if (index < 10) "0$index" else index.toString()
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

package ir.boltjournal.app

import android.icu.util.Calendar
import android.icu.util.PersianCalendar

object PersianDate {
    fun getCurrentPersianDate(): String {
        // استفاده از تقویم شمسی رسمی اندروید (دقیق و بدون خطا)
        val persianCalendar = PersianCalendar()
        
        val year = persianCalendar.get(Calendar.YEAR)
        val month = persianCalendar.get(Calendar.MONTH) + 1
        val day = persianCalendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = persianCalendar.get(Calendar.DAY_OF_WEEK)

        val months = arrayOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
        // در اندروید: 1=یکشنبه، 2=دوشنبه ... 7=شنبه
        val days = arrayOf("یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه")

        return "${days[dayOfWeek - 1]}، $day ${months[month - 1]} $year"
    }
}

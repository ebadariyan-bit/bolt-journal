package ir.boltjournal.app

import com.mfathi91.persian_date.PersianDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object PersianDateHelper {
    fun getCurrentPersianDate(): String {
        val today = PersianDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", Locale("fa"))
        return today.format(formatter)
    }
}

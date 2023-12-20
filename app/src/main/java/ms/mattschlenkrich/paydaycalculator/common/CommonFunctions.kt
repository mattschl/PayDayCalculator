package ms.mattschlenkrich.paydaycalculator.common

import java.text.NumberFormat
import java.util.Locale
import java.util.Random

@Suppress("unused")
class CommonFunctions {
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)
    private val dollarFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)
    private val numberDisplay: NumberFormat = NumberFormat.getNumberInstance(Locale.CANADA)
    private val percentFormat = NumberFormat.getPercentInstance()

    fun getDoubleFromDollars(dollars: String): Double {
        return if (dollars != "") {
            dollars.trim()
                .replace("$", "")
                .replace(",", "")
                .toDouble()
        } else {
            0.0
        }
    }

    fun displayDollars(num: Double): String {
        return dollarFormat.format(num)
    }


    fun getNumberFromDouble(num: Double): String {
        return numberDisplay.format(num)
    }

    fun getDollarsFromDouble(num: Double): String {
        return currencyFormat.format(num)
    }

    fun generateId(): Long {
        var id =
            Random().nextInt(Int.MAX_VALUE).toLong()
        id = if (Random().nextBoolean()) -id
        else id
        return id
    }

    fun getDoubleFromPercent(percent: String): Double {
        var num = 0.0
        if (percent.contains("%")) {
            num = percent.trim().replace("%", "")
                .toDouble() / 100
        }
        return num
    }

    fun displayPercentFromDouble(num: Double): String {
        return percentFormat.format(num)
    }
}
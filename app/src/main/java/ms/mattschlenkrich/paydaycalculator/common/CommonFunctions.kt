package ms.mattschlenkrich.billsprojectionv2.common

import java.text.NumberFormat
import java.util.Locale
import java.util.Random

class CommonFunctions {
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)
    private val dollarFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)
    private val numberDisplay: NumberFormat = NumberFormat.getNumberInstance(Locale.CANADA)

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
}
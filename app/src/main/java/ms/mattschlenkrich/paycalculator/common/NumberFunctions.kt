package ms.mattschlenkrich.paycalculator.common

import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import kotlin.math.round
import kotlin.math.roundToInt

class NumberFunctions {
    private val dollarFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)
    private val numberDisplay: NumberFormat = NumberFormat.getNumberInstance(Locale.CANADA)

    fun getDoubleFromDollars(dollars: String): Double {
        return if (dollars != "") {
            dollars.trim().replace("$", "").replace(",", "").toDouble()
        } else {
            0.0
        }
    }

    fun displayDollars(num: Double): String {
        return dollarFormat.format(num)
    }

    fun displayDollarsWithoutZeros(num: Double): String {
        return "$" + displayNumberFromDouble(num)
    }


    fun displayNumberFromDouble(num: Double): String {
        return numberDisplay.format(num)
    }

    fun generateRandomIdAsLong(): Long {
//        var id = Random().nextInt(Int.MAX_VALUE).toLong()
//        id = if (Random().nextBoolean()) -id
//        else id
//        return id
        // UUIDs are 128-bit; we take the 64 least significant bit
        val uuid = UUID.randomUUID().leastSignificantBits
        return if (uuid == 0L) UUID.randomUUID().leastSignificantBits else uuid
    }

    fun getDoubleFromPercentString(percent: String): Double {
        return percent.trim().replace("%", "").toDouble() / 100
    }

    fun getPercentStringFromDouble(num: Double): String {
        val percent = (num * 10000).roundToInt() / 100.0
        return if (percent < 100) {
            "$percent%"
        } else {
            "${(percent / 100)}%"
        }
    }

    fun getDoubleFromDollarOrPercentString(numString: String?): Double {
        if (numString != null) {
            return if (numString.contains("%")) {
                getDoubleFromPercentString(numString)
            } else if (numString.contains("$")) {
                getDoubleFromDollars(numString)
            } else {
                numString.toDouble()
            }
        }
        return 0.0
    }

    fun roundTo2Decimals(num: Double): Double {
        return round(num * 100) / 100
    }
}
package ms.mattschlenkrich.paycalculator.common

object CalculatorLogic {
    fun addDigit(currentDisplay: String, digit: String): String {
        var prefix = if (currentDisplay.contains("-")) "-" else ""
        var number = currentDisplay.replace("-", "")

        when {
            digit == "0" && number != "0" -> number += "0"
            number == "0" -> number = digit
            digit == "." && !number.contains(".") -> number += "."
            digit != "." && digit != "-" -> number += digit
            digit == "-" -> prefix = if (prefix == "-") "" else "-"
        }
        return prefix + number
    }

    fun backspace(currentDisplay: String): String {
        val prefix = if (currentDisplay.contains("-")) "-" else ""
        var num = currentDisplay.replace("-", "")
        num = if (num.length > 1) {
            num.substring(0, num.length - 1)
        } else {
            "0"
        }
        return prefix + num
    }

    fun calculate(
        prevValue: Double,
        currentValue: Double,
        operator: String
    ): Double {
        return when (operator) {
            "+" -> prevValue + currentValue
            "-" -> prevValue - currentValue
            "X" -> prevValue * currentValue
            "/" -> if (currentValue != 0.0) prevValue / currentValue else 0.0
            else -> currentValue
        }
    }

    fun formatFormula(
        prevValue: Double,
        currentValue: Double,
        operator: String,
        result: Double,
        nf: NumberFunctions
    ): String {
        if (operator.isEmpty()) return nf.displayNumberFromDouble(currentValue)
        return "${nf.displayNumberFromDouble(prevValue)} $operator " +
                "${nf.displayNumberFromDouble(currentValue)} = ${nf.displayDollars(result)}"
    }
}
package ms.mattschlenkrich.paycalculator.common

import java.util.Locale

class StringFunctions {
    fun toTitleCase(input: String): String {
        return input.split(" ").joinToString(" ") { word ->
            word.lowercase(Locale.ROOT).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            }
        }
    }

    fun capitalizeFirst(input: String): String {
        if (input.isBlank()) return input
        return input.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
    }
}
package ms.mattschlenkrich.paydaycalculator.common

enum class PayFrequencies(val frequency: Int) {
    HOURLY(0),
    DAILY(1),
    WEEKLY(2),
    PAY_DAY(3),
    MONTHLY(4)
}
package ms.mattschlenkrich.paycalculator.common

enum class AppliesToFrequencies(val value: Int) {
    Hourly(0),
    Daily(1),
    Weekly(2),
    PerPayForHourlyWages(3),
    PerPayPercentageOfAll(4),
}

enum class AttachToFrequencies(val value: Int) {
    Hourly(0),
    Daily(1),
    Weekly(2),
    PerPay(3),
}

enum class TaxBasedOn(val value: Int) {
    TimeWorkedOnly(0),
    TimeWorkedAndStat(1),
    TimeWorkedStatsAndExtras(2),
}

enum class PayRateBasedOn(val value: Int) {
    Hourly(0),
    Daily(1),
    Weekly(2),
    BiWeekly(3),
    Monthly(4)
}

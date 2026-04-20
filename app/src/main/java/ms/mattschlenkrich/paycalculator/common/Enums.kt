package ms.mattschlenkrich.paycalculator.common

enum class ExtraAppliesToFrequencies(val value: Int, val frequency: String) {
    HOURLY(0, "Hourly"),
    DAILY(1, "Daily"),
    WEEKLY(2, "Weekly"),
    PER_PAY_FOR_HOURLY_WAGES(3, "Per Pay For Hourly Wages"),
    PER_PAY_PERCENTAGE_OF_ALL(4, "Per Pay Percentage Of All"), ;

    override fun toString(): String {
        return frequency
    }
}

enum class ExtraAttachToFrequencies(val value: Int, val frequency: String) {
    HOURLY(0, "Hourly"),
    DAILY(1, "Daily"),
    WEEKLY(2, "Weekly"),
    PER_PAY(3, "Per Pay"), ;

    override fun toString(): String {
        return frequency
    }
}

enum class TaxBasedOn(val value: Int, val basedOn: String) {
    TIME_WORKED_ONLY(0, "Time Worked Only"),
    TIME_WORK_AND_STATS(1, "Time Worked And Stats"),
    TIME_WORKED_STATS_AND_EXTRAS(2, "Time Worked, Stats, And Extras"), ;

    override fun toString(): String {
        return basedOn
    }
}

enum class PayRateBasedOn(val value: Int, val type: String) {
    HOURLY(0, "Hourly"),
    DAILY(1, "Daily"),
    WEEKLY(2, "Weekly"),
    BI_WEEKLY(3, "Bi-Weekly"),
    MONTHLY(4, "Monthly"), ;

    override fun toString(): String {
        return type
    }
}


enum class TimeWorkedTypes(val value: Int, val type: String) {
    BREAK(0, "Break"),
    REG_HOURS(1, "Reg Hrs"),
    OT_HOURS(2, "Ot Hrs"),
    DBL_OT_HOURS(3, "Dbl Ot Hrs"), ;

    override fun toString(): String {
        return type
    }
}

enum class WorkDayOfWeek(val value: Int, val day: String) {
    FRIDAY(0, "Friday"),
    SATURDAY(1, "Saturday"),
    SUNDAY(2, "Sunday"),
    MONDAY(3, "Monday"),
    TUESDAY(4, "Tuesday"),
    WEDNESDAY(5, "Wednesday"),
    THURSDAY(6, "Thursday"),
    WEEK_DAY(7, "Week Day"),
    ANY_DAY(8, "Any Day");

    override fun toString(): String {
        return day
    }
}

enum class PayDayFrequencies(val value: Int, val frequency: String) {
    BI_WEEKLY(0, "Bi-Weekly"),
    WEEKLY(1, "Weekly"),
    SEMI_MONTHLY(2, "Semi-Monthly"),
    MONTHLY(3, "Monthly"), ;

    override fun toString(): String {
        return frequency
    }
}
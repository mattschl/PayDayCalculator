//@file:Suppress("unused")

package ms.mattschlenkrich.paydaycalculator.common

const val PAY_DB_NAME = "pay.db"
const val PAY_DB_VERSION = 1

const val SQLITE_DATE = "yyyy-LL-dd"
const val SQLITE_TIME = "yyyy-LL-dd HH:mm:ss"
const val DATE_CHECK = "yyyy-MM-dd"
const val DATE_CHECK_WITH_YEAR = "?"
const val DISPLAY_DATE = "EEE dd LLL"
const val DISPLAY_DATE_WITH_YEAR = "EEE dd LLL /yy"

const val TABLE_EMPLOYERS = "employers"
const val EMPLOYER_ID = "employerId"
const val EMPLOYER_NAME = "employerName"
const val EMPLOYER_IS_DELETED = "employerIsDeleted"
const val EMPLOYER_UPDATE_TIME = "employerUpdateTime"

const val TABLE_EMPLOYER_TAX_RULES = "employerTaxRules"
const val EMPLOYER_TAX_RULES_EMPLOYER_ID = "etrEmployerId"
const val EMPLOYER_TAX_RULES_TAX_TYPE = "etrTaxType"
const val EMPLOYER_TAX_RULES_INCLUDE = "etrInclude"
const val EMPLOYER_TAX_RULES_IS_DELETED = "etrIsDeleted"
const val EMPLOYER_TAX_RULES_UPDATE_TIME = "etrUpdateTime"

const val TABLE_WORK_PAY_PERIODS = "workPayPeriods"
const val PAY_PERIOD_CUTOFF_DATE = "ppCutoffDate"
const val PAY_PERIOD_EMPLOYER_ID = "ppEmployerId"
const val PAY_PERIOD_UPDATE_TIME = "ppUpdateTime"
const val PAY_PERIOD_IS_DELETED = "ppIsDeleted"

const val TABLE_WORK_DATES = "workDates"
const val WORK_DATES_EMPLOYER_ID = "wdEmployerId"
const val WORK_DATES_CUTOFF_DATE = "wdCutoffDate"
const val WORK_DATES_DATE = "wdDate"
const val WORK_DATES_REG_HOURS = "wdRegHours"
const val WORK_DATES_OT_HOURS = "wdOtHours"
const val WORK_DATES_DBL_OT_HOURS = "wdDblOtHours"
const val WORK_DATES_STAT_HOURS = "wdStatHours"
const val WORK_DATES_IS_DELETED = "wdIsDeleted"
const val WORK_DATES_UPDATE_TIME = "wdUpdateTime"

const val TABLE_WORK_EXTRAS_DEFINITIONS = "workExtrasDefinitions"
const val WORK_EXTRA_DEFINITIONS_ID = "workExtraId"
const val WORK_EXTRA_DEFINITIONS_EMPLOYER_ID = "weEmployerId"
const val WORK_EXTRA_DEFINITIONS_NAME = "weName"
const val WORK_EXTRA_DEFINITIONS_VALUE = "weValue"
const val WORK_EXTRA_DEFINITIONS_FREQUENCY = "weFrequency"
const val WORK_EXTRA_DEFINITIONS_IS_CREDIT = "weIsCredit"
const val WORK_EXTRA_IS_DEFAULT = "weIsDefault"
const val WORK_EXTRA_EFFECTIVE_DATE = "weEffectiveDate"
const val WORK_EXTRA_IS_DELETED = "wedIsDeleted"
const val WORK_EXTRA_UPDATE_TIME = "weUpdateTime"

const val TABLE_WORK_EXTRA_FREQUENCIES = "workExtraFrequencies"
const val WORK_EXTRA_FREQUENCY = "workExtraFrequency"

const val TABLE_WORK_DATES_EXTRAS = "workDatesExtras"
const val WORK_DATES_EXTRAS_EMPLOYER_ID = "wdeEmployerId"
const val WORK_DATES_EXTRAS_DATE = "wdeDate"
const val WORK_DATES_EXTRAS_NAME = "wdeName"
const val WORK_DATES_EXTRA_ID = "wdeId"
const val WORK_DATES_EXTRAS_IS_DELETED = "wdeIsDeleted"
const val WORK_DATES_EXTRAS_UPDATE_TIME = "wdeUpdateTime"

const val WORK_EXTRA_FREQUENCY_HOURLY = "hourly"
const val WORK_EXTRA_FREQUENCY_DAILY = "daily"
const val WORK_EXTRA_FREQUENCY_PER_PAY = "per pay"

const val TABLE_WORK_TAX_TYPES = "workTaxTypes"
const val WORK_TAX_TYPE = "workTaxType"
const val WORK_TAX_TYPE_IS_DELETED = "wttIsDeleted"
const val WORK_TAX_TYPE_UPDATE_TIME = "wttUpdateTime"

const val TABLE_WORK_TAX_RULES = "workTaxRules"
const val WORK_TAX_RULE_ID = "workTaxRuleId"
const val WORK_TAX_RULE_TYPE = "wtType"
const val WORK_TAX_RULE_EFFECTIVE_DATE = "wtEffectiveDate"
const val WORK_TAX_RULE_LEVEL = "wtLevel"
const val WORK_TAX_RULE_PERCENT = "wtPercent"
const val WORK_TAX_RULE_HAS_EXEMPTION = "wtHasExemption"
const val WORK_TAX_RULE_EXEMPTION_AMOuNT = "wtExemptionAmount"
const val WORK_TAX_RULE_HAS_BRACKET = "wtHasBracket"
const val WORK_TAX_RULE_BRACKET_AMOuNT = "wtBracketAmount"
const val WORK_TAX_RULE_IS_DELETED = "wtIsDeleted"
const val WORK_TAX_RULE_UPDATE_TIME = "wtUpdateTime"

const val TABLE_WORK_PAY_PERIOD_EXTRAS = "workPayPeriodExtras"
const val WORK_PAY_PERIOD_EXTRA_EMPLOYER_ID = "ppeEmployerId"
const val WORK_PAY_PERIOD_EXTRA_CUTOFF_DATE = "ppeCutoffDate"
const val WORK_PAY_PERIOD_EXTRA_ID = "ppeExtraId"
const val WORK_PAY_PERIOD_EXTRA_NAME = "ppeName"
const val WORK_PAY_PERIOD_EXTRA_VALUE = "ppeValue"
const val WORK_PAY_PERIOD_EXTRA_IS_DELETED = "ppeIsDeleted"
const val WORK_PAY_PERIOD_EXTRA_UPDATE_TIME = "ppeUpdateTime"

const val TABLE_WORK_PAY_PERIOD_TAX = "workPayPeriodTax"
const val PAY_PERIOD_TAX_CUTOFF_DATE = "wppCutoffDate"
const val PAY_PERIOD_TAX_EMPLOYER_ID = "wppEmployerId"
const val PAY_PERIOD_TAX_TYPE_ID = "wppTaxTypeId"
const val PAY_PERIOD_TAX_IS_DELETED = "wppIsDeleted"
const val PAY_PERIOD_TAX_UPDATE_TIME = "wppUpdateTime"

const val TABLE_TAX_EFFECTIVE_DATES = "taxEffectiveDates"
const val TAX_EFFECTIVE_DATE = "tdEffectiveDate"

const val ANSWER_OK = "Ok"

const val DAY_MONDAY = "Monday"
const val DAY_TUESDAY = "Tuesday"
const val DAY_WEDNESDAY = "Wednesday"
const val DAY_THURSDAY = "Thursday"
const val DAY_FRIDAY = "Friday"
const val DAY_SATURDAY = "Saturday"
const val DAY_SUNDAY = "Sunday"
const val DAY_WEEK_DAY = "Week Day"
const val DAY_ANY_DAY = "Any Day"
const val DAY_PAY_DAY = "Pay Day"

const val INTERVAL_WEEKLY = "Weekly"
const val INTERVAL_BI_WEEKLY = "Bi-Weekly"
const val INTERVAL_MONTHLY = "Monthly"
const val INTERVAL_SEMI_MONTHLY = "Semi-Monthly"
const val INTERVAL_YEARLY = "Yearly"

const val FREQ_MONTHLY = 0
const val FREQ_WEEKLY = 1
const val FREQ_YEARLY = 2
const val FREQ_PAYDAY = 3
const val FREQ_MANUALLY = 4
const val FREQ_SPECIAL = 5


const val WAIT_250 = 250L
const val WAIT_500 = 500L
const val WAIT_1000 = 1000L


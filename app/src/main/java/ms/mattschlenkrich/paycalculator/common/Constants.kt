package ms.mattschlenkrich.paycalculator.common

const val PAY_DB_NAME = "pay.db"
const val PAY_DB_VERSION = 16

const val SQLITE_DATE = "yyyy-LL-dd"
const val SQLITE_TIME = "yyyy-LL-dd HH:mm:ss"
const val DATE_CHECK = "yyyy-MM-dd"

const val DISPLAY_DATE = "EEE dd LLL"

const val TABLE_EMPLOYERS = "employers"
const val EMPLOYER_ID = "employerId"
const val EMPLOYER_NAME = "employerName"

const val TABLE_EMPLOYER_TAX_TYPES = "employerTaxTypes"
const val EMPLOYER_TAX_RULES_EMPLOYER_ID = "etrEmployerId"
const val EMPLOYER_TAX_RULES_TAX_TYPE = "etrTaxType"

const val TABLE_PAY_PERIODS = "payPeriods"

const val TABLE_WORK_DATES = "workDates"

const val TABLE_WORK_EXTRAS_DEFINITIONS = "workExtrasDefinitions"
const val WORK_EXTRA_DEFINITIONS_EMPLOYER_ID = "weEmployerId"

const val TABLE_WORK_EXTRA_TYPES = "workExtraTypes"

const val TABLE_EMPLOYER_PAY_RATES = "employerPayRates"

const val TABLE_WORK_DATE_EXTRAS = "workDateExtras"

const val TABLE_TAX_TYPES = "taxTypes"
const val WORK_TAX_TYPE = "taxType"

const val TABLE_WORK_TAX_RULES = "workTaxRules"
const val WORK_TAX_RULE_TYPE = "wtType"
const val WORK_TAX_RULE_EFFECTIVE_DATE = "wtEffectiveDate"
const val WORK_TAX_RULE_LEVEL = "wtLevel"

const val TABLE_WORK_PAY_PERIOD_EXTRAS = "workPayPeriodExtras"

const val TABLE_SYNC_HISTORY = "syncHistory"

const val TABLE_TAX_EFFECTIVE_DATES = "taxEffectiveDates"
const val TAX_EFFECTIVE_DATE = "tdEffectiveDate"

const val DAY_MONDAY = "Monday"
const val DAY_TUESDAY = "Tuesday"
const val DAY_WEDNESDAY = "Wednesday"
const val DAY_THURSDAY = "Thursday"
const val DAY_FRIDAY = "Friday"
const val DAY_SATURDAY = "Saturday"
const val DAY_SUNDAY = "Sunday"

const val INTERVAL_WEEKLY = "Weekly"
const val INTERVAL_BI_WEEKLY = "Bi-Weekly"
const val INTERVAL_MONTHLY = "Monthly"
const val INTERVAL_SEMI_MONTHLY = "Semi-Monthly"


const val DEFAULT_MIN_COLUMN_WIDTH = 360

const val PREFS_NAME = "PayCalculatorPrefs"
const val SYNC_ACCOUNT_EMAIL = "sync_account_email"
const val DEVICE_ID = "device_id"
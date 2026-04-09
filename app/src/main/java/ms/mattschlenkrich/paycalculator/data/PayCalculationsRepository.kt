package ms.mattschlenkrich.paycalculator.data

class PayCalculationsRepository(private val db: PayDatabase) {

    fun getPayRate(employerId: Long, cutoffDate: String) =
        db.getPayCalculationsDao().getPayRate(employerId, cutoffDate)

    fun getWorkDateList(employerId: Long, cutOff: String) =
        db.getPayCalculationsDao().getWorkDateList(employerId, cutOff)

    fun getWorkDateExtrasPerPay(employerId: Long, cutOff: String) =
        db.getPayCalculationsDao().getWorkDateExtrasPerPay(employerId, cutOff)

    fun getDefaultExtraTypesAndCurrentDef(employerId: Long, cutoffDate: String) =
        db.getPayCalculationsDao().getDefaultExtraTypesAndCurrentDef(
            employerId, cutoffDate
        )

    fun getCustomPayPeriodExtras(payPeriodId: Long) =
        db.getPayCalculationsDao().getCustomPayPeriodExtras(payPeriodId)

    fun getExtraTypes(employerId: Long) = db.getPayCalculationsDao().getExtraTypes(employerId)

    fun getCurrentEffectiveDate(cutoffDate: String) =
        db.getPayCalculationsDao().getCurrentEffectiveDate(cutoffDate)

    fun getTaxTypes(employerId: Long) = db.getPayCalculationsDao().getTaxTypes(employerId)

    fun getTaxRules(effectiveDate: String) = db.getPayCalculationsDao().getTaxRules(effectiveDate)
}
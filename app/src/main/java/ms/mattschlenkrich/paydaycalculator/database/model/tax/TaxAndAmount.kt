package ms.mattschlenkrich.paydaycalculator.database.model.tax


data class TaxAndAmount(
    var taxType: String,
    var amount: Double,
)
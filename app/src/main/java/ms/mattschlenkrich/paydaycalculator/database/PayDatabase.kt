package ms.mattschlenkrich.paydaycalculator.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ms.mattschlenkrich.paydaycalculator.common.PAY_DB_NAME
import ms.mattschlenkrich.paydaycalculator.common.PAY_DB_VERSION
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerPayRates
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraTypeAndDefByDay
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtrasDefinitions
import ms.mattschlenkrich.paydaycalculator.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkPayPeriodTax
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.WorkTaxRules


@Database(
    entities = [
        Employers::class,
        PayPeriods::class,
        WorkDates::class,
        WorkDateExtras::class,
        WorkPayPeriodExtras::class,
        WorkPayPeriodTax::class,
        WorkExtrasDefinitions::class,
        WorkExtraTypes::class,
        WorkTaxRules::class,
        TaxTypes::class,
        TaxEffectiveDates::class,
        EmployerTaxTypes::class,
        EmployerPayRates::class,
    ],
    views = [ExtraDefinitionAndType::class,
        ExtraTypeAndDefByDay::class],
    version = PAY_DB_VERSION,
)
abstract class PayDatabase : RoomDatabase() {

    abstract fun getEmployerDao(): EmployerDao
    abstract fun getWorkTaxDao(): WorkTaxDao
    abstract fun getWorkExtraDao(): WorkExtraDao
    abstract fun getPayDayDao(): PayDayDao

    companion object {
        @Volatile
        private var instance: PayDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: createDatabase(context).also {
                    instance = it
                }
            }

        private fun createDatabase(context: Context): PayDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                PayDatabase::class.java,
                PAY_DB_NAME
            )
                .createFromAsset("pay.db")
                .build()
        }
    }
}
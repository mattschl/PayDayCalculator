package ms.mattschlenkrich.paydaycalculator.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ms.mattschlenkrich.paydaycalculator.common.PAY_DB_NAME
import ms.mattschlenkrich.paydaycalculator.common.PAY_DB_VERSION
import ms.mattschlenkrich.paydaycalculator.model.EmployerTaxRules
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.WorkDatesExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraFrequencies
import ms.mattschlenkrich.paydaycalculator.model.WorkExtrasDefinitions
import ms.mattschlenkrich.paydaycalculator.model.WorkPayPeriodExtras
import ms.mattschlenkrich.paydaycalculator.model.WorkPayPeriodTax
import ms.mattschlenkrich.paydaycalculator.model.WorkPayPeriods
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxTypes


@Database(
    entities = [
        Employers::class,
        WorkPayPeriods::class,
        WorkDates::class,
        WorkDatesExtras::class,
        WorkPayPeriodExtras::class,
        WorkPayPeriodTax::class,
        WorkExtrasDefinitions::class,
        WorkExtraFrequencies::class,
        WorkTaxRules::class,
        WorkTaxTypes::class,
        TaxEffectiveDates::class,
        EmployerTaxRules::class,
    ],
    version = PAY_DB_VERSION,
)
abstract class PayDatabase : RoomDatabase() {

    abstract fun getEmployerDao(): EmployerDao
    abstract fun getWorkTaxDao(): WorkTaxDao
    abstract fun getWorkExtraDao(): WorkExtraDao

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
                .build()
        }
    }
}
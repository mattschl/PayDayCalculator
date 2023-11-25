package ms.mattschlenkrich.paydaycalculator.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ms.mattschlenkrich.paydaycalculator.common.PAY_DB_NAME
import ms.mattschlenkrich.paydaycalculator.common.PAY_DB_VERSION
import ms.mattschlenkrich.paydaycalculator.model.Employers


@Database(
    entities = [
        Employers::class,
//        WorkPayPeriods::class,
//        WorkDates::class,
//        WorkDatesExtras::class,
//        WorkPayPeriodExtras::class,
//        WorkPayPeriodTax::class,
//        WorkExtrasDefinitions::class,
//        WorkExtraFrequencies::class,
//        WorkTaxRules::class,
//        WorkTaxTypes::class,
//        EmployerTaxRules::class,
    ],
    version = PAY_DB_VERSION,
)
abstract class PayDatabase : RoomDatabase() {

    abstract fun getEmployerDao(): EmployerDao

    companion object {
        @Volatile
        private var instance: PayDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: creatDatabase(context).also {
                    instance = it
                }
            }

        private fun creatDatabase(context: Context): PayDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                PayDatabase::class.java,
                PAY_DB_NAME
            )
                .build()
        }
    }
}
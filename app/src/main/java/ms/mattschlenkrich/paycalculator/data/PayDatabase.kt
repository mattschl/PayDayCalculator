package ms.mattschlenkrich.paycalculator.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ms.mattschlenkrich.paycalculator.common.PAY_DB_NAME
import ms.mattschlenkrich.paycalculator.common.PAY_DB_VERSION

@Database(
    entities = [
        Employers::class,
        EmployerTaxTypes::class,
        EmployerPayRates::class,
        WorkDateExtras::class,
        WorkPayPeriodExtras::class,
        WorkExtraTypes::class,
        WorkDates::class,
        WorkPayPeriodTax::class,
        WorkExtrasDefinitions::class,
        WorkTaxRules::class,
        TaxTypes::class,
        TaxEffectiveDates::class,
        WorkOrder::class,
        PayPeriods::class,
        WorkOrderHistory::class,
        WorkPerformed::class,
        JobSpec::class,
        WorkOrderHistoryWorkPerformed::class,
        WorkOrderJobSpec::class,
        Material::class,
        WorkOrderHistoryMaterial::class,
        Areas::class,
        JobSpecMerged::class,
        MaterialMerged::class,
        WorkPerformedMerged::class,
        WorkOrderHistoryTimeWorked::class,
    ],
    views = [ExtraDefinitionAndType::class,
        ExtraTypeAndDefByDay::class],
    autoMigrations =
        [AutoMigration(9, 10), AutoMigration(10, 11), AutoMigration(11, 12)],
    exportSchema = true,
    version = PAY_DB_VERSION,
)
abstract class PayDatabase : RoomDatabase() {

    abstract fun getEmployerDao(): EmployerDao
    abstract fun getWorkTaxDao(): WorkTaxDao
    abstract fun getWorkExtraDao(): WorkExtraDao
    abstract fun getPayDayDao(): PayDayDao
    abstract fun getWorkOrderDao(): WorkOrderDao
    abstract fun getPayDetailDao(): PayDetailDao
    abstract fun getPayCalculationsDao(): PayCalculationsDao
    abstract fun getWorkTimeDao(): WorkTimeDao

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
                .createFromAsset(PAY_DB_NAME)
//                .fallbackToDestructiveMigration(false)
                .build()
        }
    }
}
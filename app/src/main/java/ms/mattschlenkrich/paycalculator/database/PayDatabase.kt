package ms.mattschlenkrich.paycalculator.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.PAY_DB_NAME
import ms.mattschlenkrich.paycalculator.common.PAY_DB_VERSION
import ms.mattschlenkrich.paycalculator.database.dao.EmployerDao
import ms.mattschlenkrich.paycalculator.database.dao.PayCalculationsDao
import ms.mattschlenkrich.paycalculator.database.dao.PayDayDao
import ms.mattschlenkrich.paycalculator.database.dao.PayDetailDao
import ms.mattschlenkrich.paycalculator.database.dao.WorkExtraDao
import ms.mattschlenkrich.paycalculator.database.dao.WorkOrderDao
import ms.mattschlenkrich.paycalculator.database.dao.WorkTaxDao
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraTypeAndDefByDay
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodTax
import ms.mattschlenkrich.paycalculator.database.model.tax.TaxEffectiveDates
import ms.mattschlenkrich.paycalculator.database.model.tax.TaxTypes
import ms.mattschlenkrich.paycalculator.database.model.tax.WorkTaxRules
import ms.mattschlenkrich.paycalculator.database.model.workorder.Areas
import ms.mattschlenkrich.paycalculator.database.model.workorder.JobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed

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
    abstract fun getWorkOrderDao(): WorkOrderDao
    abstract fun getPayDetailDao(): PayDetailDao
    abstract fun getPayCalculationsDao(): PayCalculationsDao

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
                .fallbackToDestructiveMigration(false)
                .createFromAsset(context.getString(R.string.db_name))
                .build()
        }
    }
}
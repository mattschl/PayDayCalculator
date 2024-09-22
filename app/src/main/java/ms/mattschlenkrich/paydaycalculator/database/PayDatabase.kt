package ms.mattschlenkrich.paydaycalculator.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ms.mattschlenkrich.paydaycalculator.common.PAY_DB_NAME
import ms.mattschlenkrich.paydaycalculator.common.PAY_DB_VERSION
import ms.mattschlenkrich.paydaycalculator.database.dao.EmployerDao
import ms.mattschlenkrich.paydaycalculator.database.dao.PayDayDao
import ms.mattschlenkrich.paydaycalculator.database.dao.PayDetailDao
import ms.mattschlenkrich.paydaycalculator.database.dao.WorkExtraDao
import ms.mattschlenkrich.paydaycalculator.database.dao.WorkOrderDao
import ms.mattschlenkrich.paydaycalculator.database.dao.WorkTaxDao
import ms.mattschlenkrich.paydaycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paydaycalculator.database.model.employer.EmployerTaxTypes
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraTypeAndDefByDay
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtrasDefinitions
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkPayPeriodTax
import ms.mattschlenkrich.paydaycalculator.database.model.tax.TaxEffectiveDates
import ms.mattschlenkrich.paydaycalculator.database.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.database.model.tax.WorkTaxRules
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.JobSpec
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrder
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderHistoryWorkSpec
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkOrderJobSpec
import ms.mattschlenkrich.paydaycalculator.database.model.workOrder.WorkSpec


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
        WorkOrder::class,
        WorkOrderHistory::class,
        WorkSpec::class,
        JobSpec::class,
        WorkOrderHistoryWorkSpec::class,
        WorkOrderJobSpec::class,
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
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
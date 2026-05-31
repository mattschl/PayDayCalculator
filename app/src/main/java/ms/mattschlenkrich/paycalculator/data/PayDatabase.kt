package ms.mattschlenkrich.paycalculator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ms.mattschlenkrich.paycalculator.common.PAY_DB_NAME
import ms.mattschlenkrich.paycalculator.common.PAY_DB_VERSION
import ms.mattschlenkrich.paycalculator.data.dao.EmployerDao
import ms.mattschlenkrich.paycalculator.data.dao.PayCalculationsDao
import ms.mattschlenkrich.paycalculator.data.dao.PayDayDao
import ms.mattschlenkrich.paycalculator.data.dao.PayDetailDao
import ms.mattschlenkrich.paycalculator.data.dao.WorkExtraDao
import ms.mattschlenkrich.paycalculator.data.dao.WorkOrderDao
import ms.mattschlenkrich.paycalculator.data.dao.WorkTaxDao
import ms.mattschlenkrich.paycalculator.data.dao.WorkTimeDao
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.JobSpecMerged
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.TaxEffectiveDates
import ms.mattschlenkrich.paycalculator.data.entity.TaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.data.entity.WorkTaxRules
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefinitionAndType

@Database(
    entities = [
        Employers::class,
        EmployerTaxTypes::class,
        EmployerPayRates::class,
        WorkDateExtras::class,
        WorkPayPeriodExtras::class,
        WorkExtraTypes::class,
        WorkDates::class,
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
    views = [ExtraDefinitionAndType::class],
//    autoMigrations =
//        [AutoMigration(9, 10), AutoMigration(10, 11), AutoMigration(11, 12),
//            AutoMigration(from = 12, to = 13, spec = PayDatabase.Migration12To13::class)],
//    exportSchema = true,
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
        private val MIGRATION_12_15 = object : Migration(12, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE materialMerged RENAME COLUMN mUpdateTime TO mmUpdateTime")
                db.execSQL("DROP TABLE IF EXISTS workPayPeriodTax")
                db.execSQL("DROP VIEW IF EXISTS ExtraTypeAndDefByDay")
            }
        }

        private val MIGRATION_13_15 = object : Migration(13, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS workPayPeriodTax")
                db.execSQL("DROP VIEW IF EXISTS ExtraTypeAndDefByDay")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS workPayPeriodTax")
                db.execSQL("DROP VIEW IF EXISTS ExtraTypeAndDefByDay")
            }
        }

        @Volatile
        private var instance: PayDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: createDatabase(context).also {
                    instance = it
                }
            }

        fun resetInstance() {
            synchronized(LOCK) {
                instance?.close()
                instance = null
            }
        }

        fun closeDatabase(context: Context) {
            resetInstance()
        }

        fun checkpoint(context: Context) {
            synchronized(LOCK) {
                try {
                    val db = instance ?: invoke(context)
                    db.query("PRAGMA wal_checkpoint(TRUNCATE)", null).close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun createDatabase(context: Context): PayDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                PayDatabase::class.java,
                PAY_DB_NAME
            )
                .createFromAsset(PAY_DB_NAME)
                .addMigrations(MIGRATION_12_15, MIGRATION_13_15, MIGRATION_14_15)
                .build()
        }
    }
}
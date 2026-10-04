package ms.mattschlenkrich.paycalculator.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ms.mattschlenkrich.paycalculator.common.PAY_DB_NAME
import ms.mattschlenkrich.paycalculator.common.PAY_DB_VERSION
import ms.mattschlenkrich.paycalculator.data.dao.AreaDao
import ms.mattschlenkrich.paycalculator.data.dao.EmployerDao
import ms.mattschlenkrich.paycalculator.data.dao.JobSpecDao
import ms.mattschlenkrich.paycalculator.data.dao.MaterialDao
import ms.mattschlenkrich.paycalculator.data.dao.PayCalculationsDao
import ms.mattschlenkrich.paycalculator.data.dao.PayDayDao
import ms.mattschlenkrich.paycalculator.data.dao.PayDetailDao
import ms.mattschlenkrich.paycalculator.data.dao.SyncHistoryDao
import ms.mattschlenkrich.paycalculator.data.dao.WorkExtraDao
import ms.mattschlenkrich.paycalculator.data.dao.WorkOrderDao
import ms.mattschlenkrich.paycalculator.data.dao.WorkOrderPictureDao
import ms.mattschlenkrich.paycalculator.data.dao.WorkOrderTimeDao
import ms.mattschlenkrich.paycalculator.data.dao.WorkPerformedDao
import ms.mattschlenkrich.paycalculator.data.dao.WorkTaxDao
import ms.mattschlenkrich.paycalculator.data.dao.WorkTimeDao
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.ExpensePictures
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.JobSpecMerged
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.SyncHistory
import ms.mattschlenkrich.paycalculator.data.entity.TaxEffectiveDates
import ms.mattschlenkrich.paycalculator.data.entity.TaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryPictures
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderPictures
import ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.data.entity.WorkTaxRules
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefinitionAndType
import java.io.File

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
        SyncHistory::class,
        WorkOrderHistoryExpense::class,
        WorkOrderPictures::class,
        WorkOrderHistoryPictures::class,
        ExpensePictures::class,
    ],
    views = [ExtraDefinitionAndType::class],
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
    abstract fun getSyncHistoryDao(): SyncHistoryDao
    abstract fun getJobSpecDao(): JobSpecDao
    abstract fun getMaterialDao(): MaterialDao
    abstract fun getWorkPerformedDao(): WorkPerformedDao
    abstract fun getAreaDao(): AreaDao
    abstract fun getWorkOrderTimeDao(): WorkOrderTimeDao
    abstract fun getWorkOrderPictureDao(): WorkOrderPictureDao

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

        fun resetInstance() {
            synchronized(LOCK) {
                instance?.close()
                instance = null
            }
        }

        fun closeDatabase() {
            resetInstance()
        }

        fun checkpoint(context: Context) {
            synchronized(LOCK) {
                try {
                    val db = instance ?: invoke(context)
                    val sdb = db.openHelper.writableDatabase
                    sdb.query("PRAGMA wal_checkpoint(TRUNCATE)").use { cursor ->
                        if (cursor.moveToFirst()) {
                            Log.d(
                                "PayDatabase",
                                "wal_checkpoint result: busy=${cursor.getInt(0)}, log=${
                                    cursor.getInt(1)
                                }, checkpointed=${cursor.getInt(2)}"
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PayDatabase", "Checkpoint error", e)
                }
            }
        }

        fun exportDatabase(context: Context, targetFile: File): Boolean {
            synchronized(LOCK) {
                try {
                    if (targetFile.exists()) targetFile.delete()
                    val db = instance ?: invoke(context)
                    val sdb = db.openHelper.writableDatabase

                    // Try VACUUM INTO first (SQLite 3.27+ / Android API 30+)
                    try {
                        sdb.execSQL("VACUUM INTO '${targetFile.absolutePath}'")
                        if ((targetFile.exists()) && (targetFile.length() > 0)) {
                            Log.d(
                                "PayDatabase",
                                "VACUUM INTO export succeeded: ${targetFile.length()} bytes"
                            )
                            return true
                        }
                    } catch (e: Exception) {
                        Log.w(
                            "PayDatabase",
                            "VACUUM INTO failed, falling back to wal_checkpoint + copy",
                            e
                        )
                    }

                    // Fallback: Force checkpoint then copy base file
                    sdb.query("PRAGMA wal_checkpoint(TRUNCATE)").use { cursor ->
                        if (cursor.moveToFirst()) {
                            Log.d(
                                "PayDatabase",
                                "wal_checkpoint fallback result: busy=${cursor.getInt(0)}"
                            )
                        }
                    }

                    val sourceFile = context.getDatabasePath(PAY_DB_NAME)
                    sourceFile.inputStream().use { input ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    return ((targetFile.exists()) && (targetFile.length() > 0))
                } catch (e: Exception) {
                    Log.e("PayDatabase", "Export database failed", e)
                    return false
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
                .fallbackToDestructiveMigrationOnDowngrade(true)
                .build()
        }
    }
}
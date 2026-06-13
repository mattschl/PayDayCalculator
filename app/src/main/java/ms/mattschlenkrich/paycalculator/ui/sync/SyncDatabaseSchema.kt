package ms.mattschlenkrich.paycalculator.ui.sync

data class TableSpec(
    val tableName: String,
    val keys: List<String>,
    val fks: List<FKSpec> = emptyList(),
    val isDeletedColumn: String? = null,
    val updateTimeColumn: String? = null,
    val pkColumn: String? = null,
    val employerIdColumn: String? = null // Column that links to an employer
)

data class FKSpec(
    val fkColumn: String,
    val parentTable: String,
    val parentPk: String,
    val parentNaturalKey: String,
    val dependsOnEmployer: Boolean = false // If true, use employerIdColumn for lookup
)

fun getTables() = listOf(
    TableSpec(
        "taxTypes",
        listOf("taxType"),
        pkColumn = "taxTypeId",
        isDeletedColumn = "ttIsDeleted",
        updateTimeColumn = "ttUpdateTime"
    ),
    TableSpec(
        "employers",
        listOf("employerName"),
        pkColumn = "employerId",
        isDeletedColumn = "employerIsDeleted",
        updateTimeColumn = "employerUpdateTime"
    ),
    TableSpec(
        "areas",
        listOf("areaName"),
        pkColumn = "areaId",
        isDeletedColumn = "areaIsDeleted",
        updateTimeColumn = "areaUpdateTime"
    ),
    TableSpec(
        "workPerformed",
        listOf("wpDescription"),
        pkColumn = "workPerformedId",
        isDeletedColumn = "wpIsDeleted",
        updateTimeColumn = "wpUpdateTime"
    ),
    TableSpec(
        "jobSpecs",
        listOf("jsName"),
        pkColumn = "jobSpecId",
        isDeletedColumn = "jsIsDeleted",
        updateTimeColumn = "jsUpdateTime"
    ),
    TableSpec(
        "materials",
        listOf("mName"),
        pkColumn = "materialId",
        isDeletedColumn = "mIsDeleted",
        updateTimeColumn = "mUpdateTime"
    ),
    TableSpec(
        "taxEffectiveDates",
        listOf("tdEffectiveDate"),
        pkColumn = null, // PK is string
        isDeletedColumn = "tdIsDeleted",
        updateTimeColumn = "tdUpdateTime"
    ),

    TableSpec(
        "workExtraTypes",
        listOf("wetName", "wetEmployerId"),
        listOf(FKSpec("wetEmployerId", "employers", "employerId", "employerName")),
        pkColumn = "workExtraTypeId",
        isDeletedColumn = "wetIsDeleted",
        updateTimeColumn = "wetUpdateTime",
        employerIdColumn = "wetEmployerId"
    ),
    TableSpec(
        "payPeriods",
        listOf("ppCutoffDate", "ppEmployerId"),
        listOf(FKSpec("ppEmployerId", "employers", "employerId", "employerName")),
        pkColumn = "payPeriodId",
        isDeletedColumn = "ppIsDeleted",
        updateTimeColumn = "ppUpdateTime",
        employerIdColumn = "ppEmployerId"
    ),
    TableSpec(
        "workDates",
        listOf("wdDate", "wdEmployerId", "wdCutoffDate"),
        listOf(
            FKSpec("wdEmployerId", "employers", "employerId", "employerName"),
            FKSpec("wdPayPeriodId", "payPeriods", "payPeriodId", "ppCutoffDate", true)
        ),
        pkColumn = "workDateId",
        isDeletedColumn = "wdIsDeleted",
        updateTimeColumn = "wdUpdateTime",
        employerIdColumn = "wdEmployerId"
    ),
    TableSpec(
        "workOrders",
        listOf("woNumber", "woEmployerId"),
        listOf(FKSpec("woEmployerId", "employers", "employerId", "employerName")),
        pkColumn = "workOrderId",
        isDeletedColumn = "woDeleted",
        updateTimeColumn = "woUpdateTime",
        employerIdColumn = "woEmployerId"
    ),
    TableSpec(
        "workPerformedMerged",
        listOf("wpmMasterId", "wpmChildId"),
        listOf(
            FKSpec("wpmMasterId", "workPerformed", "workPerformedId", "wpDescription"),
            FKSpec("wpmChildId", "workPerformed", "workPerformedId", "wpDescription")
        ),
        pkColumn = "workPerformedMergeId",
        isDeletedColumn = "wpmIsDeleted",
        updateTimeColumn = "wpmUpdateTime"
    ),
    TableSpec(
        "materialMerged",
        listOf("mmMasterId", "mmChildId"),
        listOf(
            FKSpec("mmMasterId", "materials", "materialId", "mName"),
            FKSpec("mmChildId", "materials", "materialId", "mName")
        ),
        pkColumn = "materialMergeId",
        isDeletedColumn = "mmIsDeleted",
        updateTimeColumn = "mmUpdateTime"
    ),
    TableSpec(
        "jobSpecMerged",
        listOf("jsmMasterId", "jsmChildId"),
        listOf(
            FKSpec("jsmMasterId", "jobSpecs", "jobSpecId", "jsName"),
            FKSpec("jsmChildId", "jobSpecs", "jobSpecId", "jsName")
        ),
        pkColumn = "jobSpecMergedId",
        isDeletedColumn = "jsmIsDeleted",
        updateTimeColumn = "jsmUpdateTime"
    ),
    TableSpec(
        "employerTaxTypes",
        listOf("etrEmployerId", "etrTaxType"),
        listOf(
            FKSpec("etrEmployerId", "employers", "employerId", "employerName"),
            FKSpec("etrTaxType", "taxTypes", "taxType", "taxType")
        ),
        isDeletedColumn = "etrIsDeleted",
        updateTimeColumn = "etrUpdateTime",
        employerIdColumn = "etrEmployerId"
    ),
    TableSpec(
        "workTaxRules",
        listOf("wtType", "wtLevel", "wtEffectiveDate"),
        listOf(
            FKSpec("wtType", "taxTypes", "taxType", "taxType"),
            FKSpec("wtEffectiveDate", "taxEffectiveDates", "tdEffectiveDate", "tdEffectiveDate")
        ),
        pkColumn = "workTaxRuleId",
        isDeletedColumn = "wtIsDeleted",
        updateTimeColumn = "wtUpdateTime"
    ),
    TableSpec(
        "employerPayRates",
        listOf("eprEmployerId", "eprEffectiveDate"),
        listOf(FKSpec("eprEmployerId", "employers", "employerId", "employerName")),
        pkColumn = "employerPayRateId",
        isDeletedColumn = "eprIsDeleted",
        updateTimeColumn = "eprUpdateTime",
        employerIdColumn = "eprEmployerId"
    ),
    TableSpec(
        "workExtrasDefinitions",
        listOf("weEmployerId", "weExtraTypeId", "weEffectiveDate"),
        listOf(
            FKSpec("weEmployerId", "employers", "employerId", "employerName"),
            FKSpec("weExtraTypeId", "workExtraTypes", "workExtraTypeId", "wetName", true)
        ),
        pkColumn = "workExtraDefId",
        isDeletedColumn = "weIsDeleted",
        updateTimeColumn = "weUpdateTime",
        employerIdColumn = "weEmployerId"
    ),

    TableSpec(
        "workOrderHistory",
        listOf("woHistoryWorkOrderId", "woHistoryWorkDateId"),
        listOf(
            FKSpec("woHistoryWorkOrderId", "workOrders", "workOrderId", "woNumber", true),
            FKSpec("woHistoryWorkDateId", "workDates", "workDateId", "wdDate", true)
        ),
        pkColumn = "woHistoryId",
        isDeletedColumn = "woHistoryDeleted",
        updateTimeColumn = "woHistoryUpdateTime"
    ),
    TableSpec(
        "workOrderJobSpecs",
        listOf("wojsWorkOrderId", "wojsJobSpecId", "wojsAreaId"),
        listOf(
            FKSpec("wojsWorkOrderId", "workOrders", "workOrderId", "woNumber", true),
            FKSpec("wojsJobSpecId", "jobSpecs", "jobSpecId", "jsName"),
            FKSpec("wojsAreaId", "areas", "areaId", "areaName")
        ),
        pkColumn = "workOrderJobSpecId",
        isDeletedColumn = "wojsIsDeleted",
        updateTimeColumn = "wojsUpdateTime"
    ),
    TableSpec(
        "workDateExtras",
        listOf("wdeWorkDateId", "wdeName"),
        listOf(
            FKSpec("wdeWorkDateId", "workDates", "workDateId", "wdDate", true),
            FKSpec("wdeExtraTypeId", "workExtraTypes", "workExtraTypeId", "wetName", true)
        ),
        pkColumn = "workDateExtraId",
        isDeletedColumn = "wdeIsDeleted",
        updateTimeColumn = "wdeUpdateTime"
    ),
    TableSpec(
        "workPayPeriodExtras",
        listOf("ppePayPeriodId", "ppeName"),
        listOf(
            FKSpec("ppePayPeriodId", "payPeriods", "payPeriodId", "ppCutoffDate", true),
            FKSpec("ppeExtraTypeId", "workExtraTypes", "workExtraTypeId", "wetName", true)
        ),
        pkColumn = "workPayPeriodExtraId",
        isDeletedColumn = "ppeIsDeleted",
        updateTimeColumn = "ppeUpdateTime"
    ),

    TableSpec(
        "workOrderHistoryMaterials",
        listOf("wohmHistoryId", "wohmMaterialId"),
        listOf(
            FKSpec("wohmHistoryId", "workOrderHistory", "woHistoryId", "woHistoryId"),
            FKSpec("wohmMaterialId", "materials", "materialId", "mName")
        ),
        pkColumn = "workOrderHistoryMaterialId",
        isDeletedColumn = "wohmIsDeleted",
        updateTimeColumn = "wohmUpdateTime"
    ),
    TableSpec(
        "workOrderHistoryWorkPerformed",
        listOf("wowpHistoryId", "wowpWorkPerformedId", "wowpAreaId"),
        listOf(
            FKSpec("wowpHistoryId", "workOrderHistory", "woHistoryId", "woHistoryId"),
            FKSpec("wowpWorkPerformedId", "workPerformed", "workPerformedId", "wpDescription"),
            FKSpec("wowpAreaId", "areas", "areaId", "areaName")
        ),
        pkColumn = "workOrderHistoryWorkPerformedId",
        isDeletedColumn = "wowpIsDeleted",
        updateTimeColumn = "wowpUpdateTime"
    ),
    TableSpec(
        "workOrderHistoryTimeWorked",
        listOf("wohtDateId", "wohtStartTime"),
        listOf(
            FKSpec("wohtHistoryId", "workOrderHistory", "woHistoryId", "woHistoryId"),
            FKSpec("wohtDateId", "workDates", "workDateId", "wdDate", true)
        ),
        pkColumn = "woHistoryTimeWorkedId",
        isDeletedColumn = "wohtIsDeleted",
        updateTimeColumn = "wohtUpdateTime"
    ),
    TableSpec(
        "syncHistory",
        listOf("syncTime", "syncDeviceId"),
        pkColumn = "syncId",
        updateTimeColumn = "syncTime"
    )
)
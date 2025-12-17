package ms.mattschlenkrich.paycalculator.database.model.workorder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimeWorkedByDay(
    var hrsReg: Double = 0.0,
    var hrsOt: Double = 0.0,
    var hrsDblOt: Double = 0.0,
    var hrsStat: Double = 0.0,
    var hrsRegByTimeEntered: Double = 0.0,
    var hrsOtByTimeEntered: Double = 0.0,
    var hrsDblOtByTimeEntered: Double = 0.0,
) : Parcelable
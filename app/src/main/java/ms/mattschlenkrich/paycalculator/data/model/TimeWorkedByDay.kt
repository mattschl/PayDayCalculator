package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimeWorkedByDay(
    var hrsReg: Double = 0.0,
    var hrsOt: Double = 0.0,
    var hrsDblOt: Double = 0.0,
    var hrsStat: Double = 0.0,
    var hrsRegByWorkOrderHistory: Double = 0.0,
    var hrsOtByWorkOrderHistory: Double = 0.0,
    var hrsDblOtByWorkOrderHistory: Double = 0.0,
    var hrsRegByTimeEntered: Double = 0.0,
    var hrsOtByTimeEntered: Double = 0.0,
    var hrsDblOtByTimeEntered: Double = 0.0,
) : Parcelable
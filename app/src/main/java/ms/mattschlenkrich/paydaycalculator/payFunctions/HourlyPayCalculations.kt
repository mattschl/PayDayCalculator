package ms.mattschlenkrich.paydaycalculator.payFunctions

class HourlyPayCalculations(
    private val hourlyCalculations: HourlyCalculations
) {
//    fun getPayReg(): Double {
//        return hours.getHoursReg() * rate
//    }
//
//    fun getPayOt(): Double {
//        return hours.getHoursOt() * rate * 1.5
//    }
//
//    fun getPayDblOt(): Double {
//        return hours.getHoursDblOt() * rate * 2
//    }
//
//    fun getPayHourly(): Double {
//        return getPayTimeWorked() + getPayStat()
//    }
//
//    fun getPayStat(): Double {
//        return hours.getHoursStat() * rate
//    }
//
////        fun getPayNet(): Double {
////            return if (getPayGross() - getDebitTotalsByPay() - tax.getAllTaxDeductions() > 0.0) {
////                getPayGross() - getDebitTotalsByPay() - tax.getAllTaxDeductions()
////            } else {
////                0.0
////            }
////        }
//
//    fun getPayGross(): Double {
//        return if (getPayHourly() > 0.0) {
//            getPayHourly() + getCreditTotalByDate() + getCreditTotalsByPay()
//        } else {
//            0.0
//        }
}
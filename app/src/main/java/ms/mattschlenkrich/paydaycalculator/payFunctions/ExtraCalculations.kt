package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateExtraAndTypeFull
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class ExtraCalculations(
    private val mView: View,
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val cutOff: String
) {
    private val workDateExtrasFull = ArrayList<WorkDateExtraAndTypeFull>()
    private val workExtrasByPay = ArrayList<ExtraDefinitionAndType>()
    private val extraTypes = ArrayList<WorkExtraTypes>()

    init {
        findExtraTypes()
        findExtrasPerDay()
        findExtrasPerPay()
    }

    fun getExtraTypes(): ArrayList<WorkExtraTypes> {
        return extraTypes
    }

    fun getWorkDateExtrasFull(): ArrayList<WorkDateExtraAndTypeFull> {
        return workDateExtrasFull
    }

    fun getWorkExtrasByPay(): ArrayList<ExtraDefinitionAndType> {
        return workExtrasByPay
    }


    private fun findExtrasPerDay() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateExtrasPerPay(
                employer.employerId, cutOff
            ).observe(lifecycleOwner) { list ->
                workDateExtrasFull.clear()
                list.listIterator().forEach {
                    workDateExtrasFull.add(it)
                }
            }
        }
    }

    private fun findExtrasPerPay() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getExtraTypesAndDef(
                employer.employerId, cutOff, 3
            ).observe(lifecycleOwner) { list ->
                workExtrasByPay.clear()
                list.listIterator().forEach {
                    workExtrasByPay.add(it)
                }
            }
        }
    }

    private fun findExtraTypes() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getWorkExtraTypeList(
                employer.employerId
            ).observe(
                lifecycleOwner
            ) { list ->
                extraTypes.clear()
                list.listIterator().forEach {
                    extraTypes.add(it)
                }
            }
        }
    }
}
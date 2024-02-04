package ms.mattschlenkrich.paydaycalculator.ui.paydays

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkDateUpdateBinding
import ms.mattschlenkrich.paydaycalculator.model.WorkDates

class WorkDateUpdateFragment : Fragment(
    R.layout.fragment_work_date_update
) {

    private var _binding: FragmentWorkDateUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curDate: WorkDates
    private val df = DateFunctions()
//    private val cf = CommonFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkDateUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = "Update this work date"
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillValues()
    }

    private fun fillValues() {
        if (mainActivity.mainViewModel.getWorkDateObject() != null) {
            curDate = mainActivity.mainViewModel.getWorkDateObject()!!
            binding.apply {
                tvWorkDate.text = df.getDisplayDate(curDate.wdDate)
                etHours.setText(curDate.wdRegHours.toString())
                etOt.setText(curDate.wdOtHours.toString())
                etDblOt.setText(curDate.wdDblOtHours.toString())
                etStat.setText(curDate.wdStatHours.toString())
            }
            fillExtras()
        }
    }

    private fun getCurWorkDate(): WorkDates {
        binding.apply {
            return WorkDates(
                curDate.workDateId,
                curDate.wdPayPeriodId,
                curDate.wdEmployerId,
                curDate.wdCutoffDate,
                curDate.wdDate,
                if (etHours.text.isNullOrBlank()) 0.0 else etHours.text.toString().trim()
                    .toDouble(),
                if (etOt.text.isNullOrBlank()) 0.0 else etOt.text.toString().trim()
                    .toDouble(),
                if (etDblOt.text.isNullOrBlank()) 0.0 else etDblOt.text.toString().trim()
                    .toDouble(),
                if (etStat.text.isNullOrBlank()) 0.0 else etStat.text.toString().trim()
                    .toDouble(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun fillExtras() {
        binding.apply {
            //create an adapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package ms.mattschlenkrich.paycalculator.employer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.databinding.ListEmployerExtraItemBinding

class EmployerExtraDefinitionsShortAdapter(
    private val employer: Employers,
    private val mainActivity: MainActivity,
    private val employerUpdateFragment: EmployerUpdateFragment,
) : RecyclerView.Adapter<EmployerExtraDefinitionsShortAdapter.DefinitionViewHolder>() {

    private val df = DateFunctions()
    private val mainViewModel = mainActivity.mainViewModel


    class DefinitionViewHolder(val itemBinding: ListEmployerExtraItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<WorkExtraTypes>() {
        override fun areItemsTheSame(
            oldItem: WorkExtraTypes, newItem: WorkExtraTypes
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: WorkExtraTypes, newItem: WorkExtraTypes
        ): Boolean {
            return oldItem.wetName == newItem.wetName
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinitionViewHolder {
        return DefinitionViewHolder(
            ListEmployerExtraItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: DefinitionViewHolder, position: Int) {
        val extra = differ.currentList[position]
        holder.itemBinding.apply {
            chkEmployerExtra.text = extra.wetName
            chkEmployerExtra.isChecked = extra.wetIsDefault
            chkEmployerExtra.setOnClickListener {
                updateEmployerExtra(extra, chkEmployerExtra.isChecked)
            }
            btnEdit.setOnClickListener {
                gotoExtraUpdate(extra)
            }
        }
    }

    private fun updateEmployerExtra(extra: WorkExtraTypes, checked: Boolean) {
        mainActivity.let {
            mainActivity.workExtraViewModel.updateWorkExtraType(
                WorkExtraTypes(
                    extra.workExtraTypeId,
                    extra.wetName,
                    extra.wetEmployerId,
                    extra.wetAppliesTo,
                    extra.wetAttachTo,
                    extra.wetIsCredit,
                    checked,
                    false,
                    df.getCurrentTimeAsString()
                )
            )
        }
    }

    private fun gotoExtraUpdate(extra: WorkExtraTypes) {
        mainViewModel.apply {
            setEmployerString(employer.employerName)
            setEmployer(employer)
            setWorkExtraType(extra)
        }
        employerUpdateFragment.gotoEmployerExtraDefinitionsFragment()
    }
}
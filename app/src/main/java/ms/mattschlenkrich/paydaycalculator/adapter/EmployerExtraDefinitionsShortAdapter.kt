package ms.mattschlenkrich.paydaycalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.databinding.ListSingleItemBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.ui.employer.EmployerUpdateFragmentDirections

class EmployerExtraDefinitionsShortAdapter(
    private val employer: Employers,
    private val mainActivity: MainActivity,
    private val mView: View,
) : RecyclerView.Adapter<EmployerExtraDefinitionsShortAdapter.DefinitionViewHolder>() {


    private val cf = CommonFunctions()
//    private val df = DateFunctions()

    class DefinitionViewHolder(val itemBinding: ListSingleItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<WorkExtraTypes>() {
            override fun areItemsTheSame(
                oldItem: WorkExtraTypes,
                newItem: WorkExtraTypes
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WorkExtraTypes,
                newItem: WorkExtraTypes
            ): Boolean {
                return oldItem.wetName == newItem.wetName
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinitionViewHolder {
        return DefinitionViewHolder(
            ListSingleItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: DefinitionViewHolder, position: Int) {
        val extra = differ.currentList[position]
        holder.itemBinding.tvDisplay.text = extra.wetName
        holder.itemView.setOnLongClickListener {
            gotoExtraUpdate(extra)
            false
        }
    }

    private fun gotoExtraUpdate(extra: WorkExtraTypes) {
        mainActivity.mainViewModel.setEmployerString(employer.employerName)
        mainActivity.mainViewModel.setEmployer(employer)
        mainActivity.mainViewModel.setWorkExtraType(extra)
        mView.findNavController().navigate(
            EmployerUpdateFragmentDirections
                .actionEmployerUpdateFragmentToEmployerExtraDefinitionUpdateFragment()
        )
    }
}
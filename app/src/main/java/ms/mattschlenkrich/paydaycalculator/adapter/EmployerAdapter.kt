package ms.mattschlenkrich.paydaycalculator.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paydaycalculator.databinding.ListEmployerItemBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import ms.mattschlenkrich.paydaycalculator.ui.employer.EmployerFragmentDirections

class EmployerAdapter(
    private val mainActivity: MainActivity,
    private val mView: View,
) :
    RecyclerView.Adapter<EmployerAdapter.EmployerViewHolder>() {

    class EmployerViewHolder(val itemBinding: ListEmployerItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallBack =
        object : DiffUtil.ItemCallback<Employers>() {
            override fun areContentsTheSame(oldItem: Employers, newItem: Employers): Boolean {
                return oldItem.employerId == newItem.employerId &&
                        oldItem.employerName == newItem.employerName
            }

            override fun areItemsTheSame(oldItem: Employers, newItem: Employers): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployerViewHolder {
        return EmployerViewHolder(
            ListEmployerItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: EmployerViewHolder, position: Int) {
        val employer = differ.currentList[position]
        holder.itemBinding.apply {
            employerName.text = employer.employerName
            var disp = employer.payFrequency
            if (employer.employerIsDeleted) {
                disp = "*Deleted*"
                tvFrequency.setTextColor(Color.RED)
            } else {
                tvFrequency.setTextColor(Color.BLACK)
            }
            tvFrequency.text = disp
            holder.itemView.setOnClickListener {
                gotoUpdateEmployer(employer)
            }
        }
    }

    private fun gotoUpdateEmployer(employer: Employers) {
        mainActivity.mainViewModel.setEmployer(employer)
        mView.findNavController().navigate(
            EmployerFragmentDirections
                .actionEmployerFragmentToEmployerUpdateFragment()
        )
    }
}
package ms.mattschlenkrich.paycalculator.ui.employer.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.databinding.ListEmployerItemBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.employer.EmployerFragment

class EmployerAdapter(
    private val mainActivity: MainActivity,
    private val employerFragment: EmployerFragment,
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
            var display = employer.payFrequency
            if (employer.employerIsDeleted) {
                display = mView.context.getString(R.string._deleted_)
                tvFrequency.setTextColor(Color.RED)
            } else {
                tvFrequency.setTextColor(Color.BLACK)
            }
            tvFrequency.text = display
        }
        holder.itemView.setOnClickListener {
            gotoUpdateEmployer(employer)
        }
    }

    private fun gotoUpdateEmployer(employer: Employers) {
        mainActivity.mainViewModel.setEmployer(employer)
        employerFragment.gotoEmployerUpdateFragment()
    }
}
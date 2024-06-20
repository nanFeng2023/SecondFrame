package com.fra.sec

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fra.sec.databinding.LayoutItemFrameBinding

class FrameAdapter(private val data: ArrayList<Bean>, private val onSelect: (pos: Int) -> Unit) :
    Adapter<FrameAdapter.FrameHolder>() {
    inner class FrameHolder(itemView: View) : ViewHolder(itemView) {
        val binding = LayoutItemFrameBinding.bind(itemView)

        init {
            binding.root.setOnClickListener {
                onSelect.invoke(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_frame, parent, false)
        return FrameHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: FrameHolder, position: Int) {
        runCatching {
            val bean = data[position]
            holder.binding.ivFrame.setImageResource(bean.frameId)
        }
    }

}
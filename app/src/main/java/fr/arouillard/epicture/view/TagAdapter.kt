package fr.arouillard.epicture.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.mancj.materialsearchbar.adapter.SuggestionsAdapter

import fr.arouillard.epicture.R


class TagAdapter(inflater: LayoutInflater) : SuggestionsAdapter<String, TagAdapter.SuggestionHolder>(inflater) {

    override fun getSingleViewHeight(): Int {
        return 60
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionHolder {
        val view = layoutInflater.inflate(R.layout.tag_suggestion, parent, false)
        return SuggestionHolder(view)
    }

    override fun onBindSuggestionHolder(suggestion: String, holder: SuggestionHolder, position: Int) {
        holder.title.text = suggestion
    }

    class SuggestionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title) as TextView
    }

}
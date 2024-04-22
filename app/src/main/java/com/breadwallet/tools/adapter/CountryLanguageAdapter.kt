package com.breadwallet.tools.adapter

import android.content.Context
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.breadwallet.R
import com.breadwallet.entities.IntroLanguage

class CountryLanguageAdapter(context: Context,val languages: Array<IntroLanguage>) : RecyclerView.Adapter<CountryLanguageAdapter.ViewHolder>() {
    private var mCountryLang: Array<IntroLanguage>? = null
    private var mInflater: LayoutInflater? = null
    private var mSelectedItem = -1;
    private var barlowFont : Typeface? = null
    private var mContext: Context? = null
    private var mediaPlayer: MediaPlayer? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtLang: TextView

        init {
            txtLang = itemView.findViewById(R.id.text_language_intro)
        }
    }

    init {
        this.mCountryLang = languages
        this.mContext = context
        this.mInflater = LayoutInflater.from(context)
        this.barlowFont = ResourcesCompat.getFont(context, R.font.barlowsemicondensed_light)
        this.mediaPlayer = MediaPlayer()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CountryLanguageAdapter.ViewHolder {
        val context = parent.context
        val layoutInflater = LayoutInflater.from(context)
        val languageView =
            layoutInflater.inflate(R.layout.language_list, parent, false)
        return ViewHolder(languageView)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val langQuestion = languages[position]
        val textLanguage = holder.txtLang
        textLanguage.text = langQuestion.name

        // Make text bold if it's in the center
        if (position == mSelectedItem) {
            if(barlowFont == null) {
                Log.e("FONT", "FAILED TO LOAD")
            }
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(mContext!!, Uri.parse("android.resource://" + mContext?.packageName + "/" + selectedAudio()))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            holder.txtLang.setTypeface(barlowFont, Typeface.BOLD)
        } else {
            holder.txtLang.setTypeface(barlowFont, Typeface.NORMAL)
        }
    }

    override fun getItemCount(): Int {
        return mCountryLang?.size ?: 0
    }

    fun updateCenterPosition(position: Int) {
        mSelectedItem = position
        notifyDataSetChanged() // Refresh the list to update the bold text
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun selectedMessage() = languages[mSelectedItem].message

    fun selectedDesc() = languages[mSelectedItem].desc

    fun selectedAudio() = languages[mSelectedItem].audio

    fun selectedLang() = languages[mSelectedItem].lang

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mediaPlayer?.release()
    }
}

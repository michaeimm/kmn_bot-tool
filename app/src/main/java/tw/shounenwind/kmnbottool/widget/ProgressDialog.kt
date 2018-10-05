package tw.shounenwind.kmnbottool.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

import com.rey.material.app.Dialog
import com.rey.material.widget.ProgressView

import tw.shounenwind.kmnbottool.R

@SuppressLint("InflateParams")
class ProgressDialog {
    private val factory: LayoutInflater
    private val pdl: View
    private val content: TextView
    val progressDialog: Dialog
    private val pv: ProgressView


    constructor(context: Context) : this(context, com.rey.material.R.style.Material_App_Dialog_Light)

    constructor(context: Context, theme: Int) {
        factory = LayoutInflater.from(context)
        pdl = factory.inflate(R.layout.progress_dialog_layout, null)
        content = pdl.findViewById(R.id.content)
        progressDialog = Dialog(context, theme).contentView(pdl)
        pv = pdl.findViewById(R.id.progress_view)
    }

    fun setContent(content: String) {
        this.content.text = content
    }

    fun dismiss() {
        progressDialog.dismiss()
    }
}

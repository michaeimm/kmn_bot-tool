package tw.shounenwind.kmnbottool.widget

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.TextView
import tw.shounenwind.kmnbottool.R

class ProgressDialog : Dialog {
    private val content: TextView

    constructor(context: Context) : super(context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.unit_progress_dialog)
        content = findViewById(R.id.content)
    }

    fun setContent(content: String) {
        this.content.text = content
    }

    fun setContent(content: Int) {
        this.content.setText(content)
    }
}

package tw.shounenwind.kmnbottool.widget

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.textColor
import tw.shounenwind.kmnbottool.R

class SimpleDialog : Dialog {

    private val positiveButton: Button
    private val negativeButton: Button
    private val content: TextView
    private val title: TextView

    constructor (context: Activity) : super(context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.unit_simple_dialog)

        title = findViewById(R.id.title)
        positiveButton = findViewById(R.id.positive)
        negativeButton = findViewById(R.id.negative)
        content = findViewById(R.id.content)
    }

    fun message(s: CharSequence) {
        content.text = s
    }

    fun message(rid: Int) {
        content.setText(rid)
    }

    fun positiveAction(resId: Int) {
        positiveButton.setText(resId)
        positiveButton.visibility = View.VISIBLE
    }

    fun negativeAction(resId: Int) {
        negativeButton.setText(resId)
        negativeButton.visibility = View.VISIBLE
    }

    fun positiveAction(c: CharSequence) {
        positiveButton.text = c
        positiveButton.visibility = View.VISIBLE
    }

    fun negativeAction(c: CharSequence) {
        negativeButton.text = c
        negativeButton.visibility = View.VISIBLE
    }

    fun positiveActionClickListener(body: () -> Unit) {
        positiveButton.setOnClickListener {
            body()
        }
    }

    fun negativeActionClickListener(body: () -> Unit) {
        negativeButton.setOnClickListener {
            body()
        }
    }

    fun positiveActionTextColor(color: Int) {
        positiveButton.textColor = color
    }

    fun negativeActionTextColor(color: Int) {
        negativeButton.textColor = color
    }

    override fun setTitle(resId: Int) {
        title.setText(resId)
        title.visibility = View.VISIBLE
    }

    override fun setTitle(c: CharSequence?) {
        title.text = c
        title.visibility = View.VISIBLE
    }

}

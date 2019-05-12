package tw.shounenwind.kmnbottool.widget

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.view.KeyEvent
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import tw.shounenwind.kmnbottool.R
import java.lang.ref.WeakReference

class SingleFieldDialog : Dialog {
    private val wrContext: WeakReference<Activity>
    private val positiveButton: Button
    private val negativeButton: Button
    private val editText: EditText
    private val title: TextView

    val text: Editable
        get() = editText.text

    constructor (context: Activity) : super(context) {
        wrContext = WeakReference(context)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.unit_input)

        title = findViewById(R.id.title)
        positiveButton = findViewById(R.id.positive)
        negativeButton = findViewById(R.id.negative)
        editText = findViewById(R.id.name_input)
        editText.setSingleLine()
        editText.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.action == KeyEvent.ACTION_DOWN && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                positiveButton.performClick()
                wrContext.get()?.also { activity ->
                    val imm =
                            activity.getSystemService(Context.INPUT_METHOD_SERVICE)!! as InputMethodManager
                    if (imm.isActive) {
                        imm.toggleSoftInput(
                                InputMethodManager.SHOW_IMPLICIT,
                                InputMethodManager.HIDE_NOT_ALWAYS
                        )
                    }
                }

                true
            } else {
                false
            }
        }
    }

    fun setText(s: CharSequence) {
        editText.setText(s)
    }

    fun setText(rid: Int) {
        editText.setText(rid)
    }

    fun positiveAction(resId: Int) {
        positiveButton.setText(resId)
    }

    fun negativeAction(resId: Int) {
        negativeButton.setText(resId)
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

    override fun setTitle(resId: Int) {
        title.setText(resId)
    }

}

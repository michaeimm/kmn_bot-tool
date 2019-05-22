package tw.shounenwind.kmnbottool.skeleton

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.util.Pair
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.util.LogUtil
import tw.shounenwind.kmnbottool.widget.ProgressDialog

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    private var progressDialog: ProgressDialog? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        bindToolbar()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        bindToolbar()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        bindToolbar()
    }

    private fun bindToolbar(){
        findViewById<Toolbar>(R.id.toolbar)?.apply {
            setSupportActionBar(this)
        }
    }

    fun bindToolbarHomeButton(){
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    fun startActivityWithTransition(intent: Intent) {
        val appBarLayout = findViewById<AppBarLayout>(R.id.appbar)
        if (appBarLayout != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT != Build.VERSION_CODES.N
                && Build.VERSION.SDK_INT != Build.VERSION_CODES.N_MR1) {
            val options = ActivityOptions.makeSceneTransitionAnimation(
                    this,
                    appBarLayout,
                    appBarLayout.transitionName
            )
            startActivity(intent, options.toBundle())
        } else {
            startActivity(intent)
        }
    }

    fun startActivityWithTransition(intent: Intent, vararg pairs: Pair<View, String>) {
        val appBarLayout = findViewById<AppBarLayout>(R.id.appbar)
        if (appBarLayout != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT != Build.VERSION_CODES.N
                && Build.VERSION.SDK_INT != Build.VERSION_CODES.N_MR1) {
            val options = ActivityOptions.makeSceneTransitionAnimation(
                    this,
                    Pair.create(appBarLayout, appBarLayout.transitionName),
                    *pairs
            )
            startActivity(intent, options.toBundle())
        } else {
            startActivity(intent)
        }
    }

    fun startActivityForResultWithTransition(intent: Intent, responseCode: Int) {
        val appBarLayout = findViewById<AppBarLayout>(R.id.appbar)
        if (appBarLayout != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT != Build.VERSION_CODES.N
                && Build.VERSION.SDK_INT != Build.VERSION_CODES.N_MR1) {
            val options = ActivityOptions.makeSceneTransitionAnimation(
                    this,
                    appBarLayout,
                    appBarLayout.transitionName
            )
            startActivityForResult(intent, responseCode, options.toBundle())
        } else {
            startActivityForResult(intent, responseCode)
        }
    }

    protected fun showProgressDialog(text: String) {
        progressDialog = ProgressDialog(this).apply {
            setContent(text)
            setCancelable(false)
            show()
        }
    }

    protected fun dismissProgressDialog() {
        LogUtil.catchAndPrint {
            progressDialog!!.dismiss()
        }
    }
}
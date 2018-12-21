package tw.shounenwind.kmnbottool.skeleton

import android.annotation.SuppressLint
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import tw.shounenwind.kmnbottool.R

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

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
}
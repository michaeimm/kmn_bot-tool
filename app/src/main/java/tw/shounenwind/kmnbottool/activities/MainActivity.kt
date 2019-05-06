package tw.shounenwind.kmnbottool.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.rey.material.app.Dialog
import com.rey.material.app.SimpleDialog
import org.jetbrains.anko.intentFor
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.gson.BoxData
import tw.shounenwind.kmnbottool.gson.ChipData
import tw.shounenwind.kmnbottool.skeleton.BaseActivity
import tw.shounenwind.kmnbottool.util.CommandExecutor.botDraw
import tw.shounenwind.kmnbottool.util.CommandExecutor.expDraw
import tw.shounenwind.kmnbottool.util.FlowJob
import tw.shounenwind.kmnbottool.util.KmnBotDataLoader
import tw.shounenwind.kmnbottool.util.LogUtil
import tw.shounenwind.kmnbottool.util.glide.GlideApp
import tw.shounenwind.kmnbottool.widget.ProgressDialog

class MainActivity : BaseActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var progressDialog: ProgressDialog? = null

    private var boxData: BoxData? = null
    private var chipData: ChipData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prepareScreen()
        if (savedInstanceState != null && KmnBotDataLoader.boxData != null) {
            return
        }
        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        checkPlurkId{input ->
            FlowJob(this@MainActivity)
                    .addUIJob {
                        showProgressDialog(getString(R.string.monster_loading))
                    }
                    .addIOJob {
                        getData(input)
                    }
                    .start()
        }
    }

    private fun prepareScreen() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        findViewById<CardView>(R.id.bot_draw).setOnClickListener {
            botDraw()
        }
        findViewById<CardView>(R.id.bot_exp).setOnClickListener {
            startActivityForResultWithTransition(intentFor<BoxActivity>("selectFor" to "exp"), FOR_RESULT_EXP)
        }
        findViewById<CardView>(R.id.bot_mons_box).setOnClickListener {
            startActivityWithTransition(intentFor<BoxActivity>())
        }
        findViewById<CardView>(R.id.bot_battle).setOnClickListener {
            startActivityWithTransition(intentFor<TeamActivity>())
        }
        if (isFinishing)
            return
        GlideApp.with(this)
                .load(R.mipmap.bot_draw)
                .into(findViewById(R.id.bot_draw_img))
        GlideApp.with(this)
                .load(R.mipmap.exp)
                .into(findViewById(R.id.bot_exp_img))
        GlideApp.with(this)
                .load(R.mipmap.tsume)
                .into(findViewById(R.id.bot_battle_img))
        GlideApp.with(this)
                .load(R.mipmap.mikann)
                .into(findViewById(R.id.bot_mons_box_img))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK)
            return
        when (requestCode) {
            FOR_RESULT_EXP -> {
                expDraw(data!!.getStringExtra("name"))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    @SuppressLint("ApplySharedPref")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuitem_license -> {
                checkLicense()
                return true
            }
            R.id.bot_link -> {
                run {
                    val intent = Intent()
                    intent.data = Uri.parse("https://www.plurk.com/KMN_BOT")
                    intent.action = Intent.ACTION_VIEW
                    startActivity(intent)
                }
                return true
            }
            R.id.author_link -> {
                run {
                    val intent = Intent()
                    intent.data = Uri.parse("https://www.plurk.com/shounenwind")
                    intent.action = Intent.ACTION_VIEW
                    startActivity(intent)
                }
                return true
            }
            R.id.logout -> {
                run {
                    val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putInt("user_id_ver", 1).commit()
                    checkPlurkId{input ->
                        FlowJob(this@MainActivity)
                                .addUIJob{
                                    showProgressDialog(getString(R.string.monster_loading))
                                }
                                .addIOJob{
                                    getData(input)
                                }
                                .start()
                    }
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun checkLicense() {
        startActivityWithTransition(intentFor<LicenseActivity>())
    }

    private inline fun checkPlurkId(crossinline inputHandler: (userId: String) -> Unit) {
        val userId = sharedPreferences.getString("user_id", null)
        val userIdVer = sharedPreferences.getInt("user_id_ver", 0)
        if (userId != null && userIdVer == LATEST_ID_VERSION) {
            inputHandler(userId)
            return
        }

        openPlurkIdInputDialog{
            inputHandler(it)
        }
    }

    private inline fun openPlurkIdInputDialog(crossinline inputHandler: (input: String) -> Unit) {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams")
        val pdl = factory.inflate(R.layout.unit_single_field_input, null)
        val dialog = Dialog(this, R.style.AppTheme)
        dialog.contentView(pdl)
        val input = pdl.findViewById<EditText>(R.id.name_input)

        @SuppressLint("ApplySharedPref")
        val flowJob = FlowJob(this)
                .addIOJob{
                    sharedPreferences.edit()
                            .putInt("user_id_ver", LATEST_ID_VERSION)
                            .putString("user_id", input.text.toString())
                            .commit()
                }
                .addUIJob{
                    inputHandler(input.text.toString())
                }

        input.setSingleLine()
        input.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.action == KeyEvent.ACTION_DOWN
                    && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                dialog.dismiss()
                val imm =
                        getSystemService(Context.INPUT_METHOD_SERVICE)!! as InputMethodManager
                if (imm.isActive) {
                    imm.toggleSoftInput(
                            InputMethodManager.SHOW_IMPLICIT,
                            InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
                flowJob.start()
                return@setOnEditorActionListener true
            }
            false
        }
        dialog
                .title(R.string.input_plurk_username)
                .cancelable(true)
                .positiveAction(android.R.string.ok)
                .positiveActionClickListener {
                    dialog.dismiss()
                    val imm =
                            getSystemService(Context.INPUT_METHOD_SERVICE)!! as InputMethodManager
                    if (imm.isActive) {
                        imm.toggleSoftInput(
                                InputMethodManager.SHOW_IMPLICIT,
                                InputMethodManager.HIDE_NOT_ALWAYS
                        )
                    }
                    flowJob.start()
                }
                .negativeAction(android.R.string.cancel)
                .negativeActionClickListener {
                    try {
                        dialog.dismiss()
                    } catch (e: Exception) {
                        //Do nothing
                    }
                }
                .show()
    }

    private fun getData(user: String) {
        val kmnBotDataLoader = KmnBotDataLoader()
        kmnBotDataLoader.setUser(user)
                .setOnSuccessListener { boxData, chipData ->
                    LogUtil.catchAndPrint {
                        this@MainActivity.boxData = boxData
                        this@MainActivity.chipData = chipData
                        runOnUiThread {
                            Toast.makeText(
                                    this@MainActivity,
                                    R.string.data_loaded,
                                    Toast.LENGTH_SHORT
                            ).show()
                            findViewById<View>(R.id.bot_draw).visibility = View.VISIBLE
                            findViewById<View>(R.id.bot_mons_box).visibility = View.VISIBLE
                            findViewById<View>(R.id.bot_battle).visibility = View.VISIBLE
                            findViewById<View>(R.id.bot_exp).visibility = View.VISIBLE
                            dismissProgressDialog()
                        }
                    }
                }
                .setOnFailedListener { _, _ ->
                    runOnUiThread {
                        LogUtil.catchAndPrint{
                            findViewById<View>(R.id.bot_draw).visibility = View.VISIBLE
                            dismissProgressDialog()
                            noDataHint()
                        }
                    }
                }
                .start()
    }

    private fun noDataHint() {
        val builder = SimpleDialog.Builder(com.rey.material.R.style.Material_App_Dialog_Simple)
        builder.title(getString(R.string.load_data_failed))
        builder.message(getString(R.string.load_data_failed_message))
                .positiveAction(getString(R.string.bot_draw_d))
                .negativeAction(getString(R.string.retry))
        val mDialog = builder.build(this) as SimpleDialog
        mDialog.apply {
            cancelable(true)
            positiveActionClickListener {
                mDialog.dismiss()
                botDraw()
            }
            negativeActionClickListener {
                mDialog.dismiss()
                checkPlurkId { input ->
                    FlowJob(this@MainActivity)
                            .addUIJob {
                                showProgressDialog(getString(R.string.monster_loading))
                            }
                            .addIOJob {
                                getData(input)
                            }
                            .start()
                }
            }
        }

        mDialog.show()
    }

    private fun showProgressDialog(text: String) {
        progressDialog = ProgressDialog(this).apply {
            setContent(text)
            progressDialog
                    .cancelable(false)
                    .show()
        }
    }

    private fun dismissProgressDialog() {
        LogUtil.catchAndPrint {
            progressDialog!!.dismiss()
        }
    }

    companion object {
        private const val LATEST_ID_VERSION = 2
        private const val FOR_RESULT_EXP = 0
    }
}

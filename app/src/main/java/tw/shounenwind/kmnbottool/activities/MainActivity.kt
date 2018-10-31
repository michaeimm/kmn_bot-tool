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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.rey.material.app.Dialog
import com.rey.material.app.SimpleDialog
import org.jetbrains.anko.intentFor
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.gson.BoxData
import tw.shounenwind.kmnbottool.gson.ChipData
import tw.shounenwind.kmnbottool.util.CommandExecutor
import tw.shounenwind.kmnbottool.util.KmnBotDataLoader
import tw.shounenwind.kmnbottool.util.LogUtil
import tw.shounenwind.kmnbottool.util.FlowJob
import tw.shounenwind.kmnbottool.util.glide.GlideApp
import tw.shounenwind.kmnbottool.widget.ProgressDialog

class MainActivity : AppCompatActivity() {
    private var sharedPreferences: SharedPreferences? = null
    private var progressDialog: ProgressDialog? = null

    internal var toolbar: Toolbar? = null
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
        checkPlurkId(object : InputHandler {
            override fun onEnter(input: String) {
                FlowJob(this@MainActivity)
                        .addUIJob {
                            showProgressDialog(getString(R.string.monster_loading))
                        }
                        .addIOJob {
                            getData(input)
                        }
                        .start()
            }

        })
    }

    private fun prepareScreen() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        findViewById<CardView>(R.id.bot_draw).setOnClickListener {
            CommandExecutor.botDraw(this)
        }
        findViewById<CardView>(R.id.bot_exp).setOnClickListener {
            startActivityForResult(intentFor<BoxActivity>("selectFor" to "exp"), FOR_RESULT_EXP)
        }
        findViewById<CardView>(R.id.bot_mons_box).setOnClickListener {
            startActivity(intentFor<BoxActivity>())
        }
        findViewById<CardView>(R.id.bot_battle).setOnClickListener {
            startActivity(intentFor<TeamActivity>())
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
                CommandExecutor.expDraw(this, data!!.getStringExtra("name"))
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
                    checkPlurkId(object : InputHandler {
                        override fun onEnter(input: String) {
                            FlowJob(this@MainActivity)
                                    .addUIJob{
                                        showProgressDialog(getString(R.string.monster_loading))
                                    }
                                    .addIOJob{
                                        getData(input)
                                    }
                                    .start()
                        }

                    })
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun checkLicense() {
        startActivity(intentFor<LicenseActivity>())
    }

    private fun checkPlurkId(inputHandler: InputHandler) {
        val userId = sharedPreferences!!.getString("user_id", null)
        val userIdVer = sharedPreferences!!.getInt("user_id_ver", 0)
        if (userId != null && userIdVer == LATEST_ID_VERSION) {
            inputHandler.onEnter(userId)
            return
        }

        openPlurkIdInputDialog(inputHandler)
    }

    private fun openPlurkIdInputDialog(inputHandler: InputHandler) {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams")
        val pdl = factory.inflate(R.layout.single_field_view, null)
        val dialog = Dialog(this, R.style.AppTheme)
        dialog.contentView(pdl)
        val input = pdl.findViewById<EditText>(R.id.name_input)

        @SuppressLint("ApplySharedPref")
        val flowJob = FlowJob(this)
                .addIOJob{
                    sharedPreferences!!.edit()
                            .putInt("user_id_ver", LATEST_ID_VERSION)
                            .putString("user_id", input.text.toString())
                            .commit()
                }
                .addUIJob{
                    inputHandler.onEnter(input.text.toString())
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
                .setOnSuccessListener(object : KmnBotDataLoader.Func {
                    override fun run(boxData: BoxData?, chipData: ChipData?) {
                        try {
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
                        } catch (e: Exception) {
                            LogUtil.printStackTrace(e)
                        }
                    }

                })
                .setOnFailedListener(object : KmnBotDataLoader.Func {
                    override fun run(boxData: BoxData?, chipData: ChipData?) {
                        try {
                            runOnUiThread {
                                findViewById<View>(R.id.bot_draw).visibility = View.VISIBLE
                                dismissProgressDialog()
                                noDataHint()
                            }
                        } catch (e: Exception) {
                            LogUtil.printStackTrace(e)
                        }
                    }

                })
                .start()
    }

    private fun noDataHint() {
        val builder = SimpleDialog.Builder(com.rey.material.R.style.Material_App_Dialog_Simple)
        builder.title(getString(R.string.load_data_failed))
        builder.message(getString(R.string.load_data_failed_message))
                .positiveAction(getString(R.string.bot_draw_d))
                .negativeAction(getString(R.string.retry))
        val mDialog = builder.build(this) as SimpleDialog
        mDialog.cancelable(true)
        mDialog.positiveActionClickListener {
            mDialog.dismiss()
            CommandExecutor.botDraw(this)
        }
        mDialog.negativeActionClickListener { _ ->
            mDialog.dismiss()
            checkPlurkId(object : InputHandler {
                override fun onEnter(input: String) {
                    FlowJob(this@MainActivity)
                            .addUIJob{
                                showProgressDialog(getString(R.string.monster_loading))
                            }
                            .addIOJob{
                                getData(input)
                            }
                            .start()
                }
            })
        }
        mDialog.show()
    }

    private fun showProgressDialog(text: String) {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setContent(text)
        progressDialog!!.progressDialog
                .cancelable(false)
                .show()
    }

    private fun dismissProgressDialog() {
        try {
            progressDialog!!.dismiss()
        } catch (e: Exception) {
            LogUtil.printStackTrace(e)
        }

    }

    private interface InputHandler {
        fun onEnter(input: String)
    }

    companion object {

        private const val LATEST_ID_VERSION = 2
        private const val FOR_RESULT_EXP = 0
    }
}

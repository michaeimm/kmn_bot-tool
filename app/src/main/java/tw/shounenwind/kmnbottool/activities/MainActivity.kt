package tw.shounenwind.kmnbottool.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.unit_toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.intentFor
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.gson.BoxData
import tw.shounenwind.kmnbottool.gson.ChipData
import tw.shounenwind.kmnbottool.skeleton.BaseActivity
import tw.shounenwind.kmnbottool.util.CommandExecutor.botDraw
import tw.shounenwind.kmnbottool.util.CommandExecutor.expDraw
import tw.shounenwind.kmnbottool.util.KmnBotData
import tw.shounenwind.kmnbottool.util.KmnBotDataLoader
import tw.shounenwind.kmnbottool.util.LogUtil
import tw.shounenwind.kmnbottool.util.glide.GlideApp
import tw.shounenwind.kmnbottool.widget.SimpleDialog
import tw.shounenwind.kmnbottool.widget.SingleFieldDialog

class MainActivity : BaseActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private var boxData: BoxData? = null
    private var chipData: ChipData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prepareScreen()
        KmnBotData.cache?.also { savedData ->
            boxData = savedData.boxData
            chipData = savedData.chipsData
            KmnBotData.cache = null
            btnBotDraw.visibility = View.VISIBLE
            btnBotMonsBox.visibility = View.VISIBLE
            btnBotBattle.visibility = View.VISIBLE
            btnBotExp.visibility = View.VISIBLE
            return
        }
        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        checkPlurkId { input ->
            mainScope?.launch(Dispatchers.Main) {
                showProgressDialog(getString(R.string.monster_loading))
                getData(input)
                tvUserName.apply {
                    @SuppressLint("SetTextI18n")
                    text = getString(R.string.welcome) + input
                    visibility = View.VISIBLE
                }
            }
        }
    }

    private fun prepareScreen() {
        setSupportActionBar(toolbar)
        btnBotDraw.setOnClickListener {
            botDraw()
        }
        btnBotExp.setOnClickListener {
            checkNotNull(boxData) {
                "boxData is null"
            }
            startActivityForResultWithTransition(intentFor<BoxActivity>(
                    "selectFor" to "exp",
                    "boxData" to (boxData as Parcelable)
            ), FOR_RESULT_EXP)
        }
        btnBotMonsBox.setOnClickListener {
            startActivityWithTransition(intentFor<BoxActivity>(
                    "boxData" to (boxData as Parcelable)
            ))
        }
        btnBotBattle.setOnClickListener {
            startActivityWithTransition(intentFor<TeamActivity>(
                    "boxData" to boxData,
                    "chipData" to chipData
            ))
        }
        if (isFinishing)
            return
        GlideApp.with(this)
                .load(R.mipmap.bot_draw)
                .into(imgBotDraw)
        GlideApp.with(this)
                .load(R.mipmap.exp)
                .into(imgBotExp)
        GlideApp.with(this)
                .load(R.mipmap.tsume)
                .into(imgBotBattle)
        GlideApp.with(this)
                .load(R.mipmap.mikann)
                .into(imgBotMonsBox)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        KmnBotData.cache = KmnBotData(boxData, chipData)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK)
            return
        when (requestCode) {
            FOR_RESULT_EXP -> {
                expDraw(data!!.getStringExtra("name")!!)
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
                    checkPlurkId { input ->
                        mainScope?.launch {
                            showProgressDialog(getString(R.string.monster_loading))
                            getData(input)
                        }
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

        openPlurkIdInputDialog {
            inputHandler(it)
        }
    }

    @SuppressLint("ApplySharedPref")
    private inline fun openPlurkIdInputDialog(crossinline inputHandler: (input: String) -> Unit) {
        val dialog = SingleFieldDialog(this)

        dialog.apply {
            setTitle(R.string.input_plurk_username)
            setCancelable(true)
            positiveAction(android.R.string.ok)
            positiveActionClickListener {
                dialog.dismiss()
                val imm =
                        getSystemService(Context.INPUT_METHOD_SERVICE)!! as InputMethodManager
                if (imm.isActive) {
                    imm.toggleSoftInput(
                            InputMethodManager.SHOW_IMPLICIT,
                            InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
                sharedPreferences.edit()
                        .putInt("user_id_ver", LATEST_ID_VERSION)
                        .putString("user_id", dialog.text.toString())
                        .commit()
                inputHandler(dialog.text.toString())
            }
            negativeAction(android.R.string.cancel)
            negativeActionClickListener {
                try {
                    dialog.dismiss()
                } catch (e: Exception) {
                    //Do nothing
                }
            }
            show()
        }

    }

    private suspend fun getData(user: String) = withContext(Dispatchers.Default) {
        val kmnBotDataLoader = KmnBotDataLoader()
        kmnBotDataLoader.setUser(user)
                .setOnSuccessListener { data ->
                    mainScope?.launch {
                        LogUtil.catchAndPrint {
                            boxData = data.boxData
                            chipData = data.chipsData
                            Toast.makeText(
                                    this@MainActivity,
                                    R.string.data_loaded,
                                    Toast.LENGTH_SHORT
                            ).show()
                            btnBotDraw.visibility = View.VISIBLE
                            btnBotMonsBox.visibility = View.VISIBLE
                            btnBotBattle.visibility = View.VISIBLE
                            btnBotExp.visibility = View.VISIBLE
                            dismissProgressDialog()
                        }
                    }
                }
                .setOnFailedListener {
                    mainScope?.launch {
                        LogUtil.catchAndPrint {
                            btnBotDraw.visibility = View.VISIBLE
                            dismissProgressDialog()
                            noDataHint()
                        }
                    }
                }
                .start()
    }

    private fun noDataHint() {
        val mDialog = SimpleDialog(this)
        mDialog.apply {
            setTitle(getString(R.string.load_data_failed))
            message(getString(R.string.load_data_failed_message))
            positiveAction(getString(R.string.bot_draw_d))
            negativeAction(getString(R.string.retry))
            setCancelable(true)
            positiveActionClickListener {
                mDialog.dismiss()
                botDraw()
            }
            negativeActionClickListener {
                mDialog.dismiss()
                checkPlurkId { input ->
                    mainScope?.launch {
                        showProgressDialog(getString(R.string.monster_loading))
                        getData(input)
                    }
                }
            }
        }

        mDialog.show()
    }

    companion object {
        private const val LATEST_ID_VERSION = 2
        private const val FOR_RESULT_EXP = 0
    }
}

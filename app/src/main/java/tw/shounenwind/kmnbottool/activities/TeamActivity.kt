package tw.shounenwind.kmnbottool.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_team.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.intentFor
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.gson.BoxData
import tw.shounenwind.kmnbottool.gson.ChipData
import tw.shounenwind.kmnbottool.gson.Pet
import tw.shounenwind.kmnbottool.skeleton.BaseActivity
import tw.shounenwind.kmnbottool.util.CommandExecutor.battleHell
import tw.shounenwind.kmnbottool.util.CommandExecutor.battleNormal
import tw.shounenwind.kmnbottool.util.CommandExecutor.battleUltraHell
import tw.shounenwind.kmnbottool.util.LogUtil
import tw.shounenwind.kmnbottool.util.glide.CircularViewTarget
import tw.shounenwind.kmnbottool.util.glide.GlideApp
import tw.shounenwind.kmnbottool.widget.ProgressDialog


class TeamActivity : BaseActivity() {

    private var boxData: BoxData? = null
    private var chipData: ChipData? = null
    private var oldTeam: Int = 0
    private val team by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Spinner>(R.id.team)!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)
        prepareScreen()

        boxData = intent.getParcelableExtra("boxData")
        chipData = intent.getParcelableExtra("chipData")

        readTeamInfo()
    }

    private fun prepareScreen() {
        bindToolbarHomeButton()

        val teamArray = arrayOf(
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10"
        )
        val teamAdapter = ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, teamArray)
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        team.adapter = teamAdapter
        team.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                writeTeamInfo()
                readTeamInfo()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        cardViewTeam.setOnClickListener { team.performClick() }

        cardViewAttacter.setOnClickListener {
            startActivityForResultWithTransition(
                    intentFor<BoxActivity>(
                            "selectFor" to "attacker",
                            "boxData" to boxData
                    ), FOR_RESULT_ATTACKER
            )
        }
        cardViewAttacter.setOnLongClickListener {
            val target = tvAttacterName.text.toString()
            val fakeAttacker = Pet()
            fakeAttacker.name = target
            val petIndex = boxData!!.pets.indexOf(fakeAttacker)
            if (petIndex != -1) {
                val intent = intentFor<DetailActivity>()
                intent.putExtra("pet", boxData!!.pets[petIndex])
                startActivityWithTransition(intent)
                true
            }else {
                false
            }
        }
        cardViewSupporter.setOnClickListener {
            startActivityForResultWithTransition(
                    intentFor<BoxActivity>(
                            "selectFor" to "supporter",
                            "boxData" to boxData
                    ),
                    FOR_RESULT_SUPPORTER
            )
        }
        cardViewSupporter.setOnLongClickListener {
            val target = tvSupporterName.text.toString()
            val fakeSupporter = Pet()
            fakeSupporter.name = target
            val petIndex = boxData!!.pets.indexOf(fakeSupporter)
            if (petIndex != -1) {
                val intent = intentFor<DetailActivity>()
                intent.putExtra("pet", boxData!!.pets[petIndex])
                startActivityWithTransition(intent)
                true
            }else {
                false
            }
        }
        if (chipData?.chips?.isNotEmpty() == true) {
            cardViewChips.visibility = View.VISIBLE
        }

        btnBotBattleNormal.setOnClickListener {
            var target = tvAttacterName.text.toString()
            if (target == getString(R.string.no_select)) {
                Toast.makeText(this, R.string.no_selection, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (tvSupporterName.text.toString() != getString(R.string.no_select)) {
                target += " " + tvSupporterName.text.toString()
            }
            target += " " + tvChips.text
            battleNormal(target)
        }
        btnBotBattleHell.setOnClickListener {
            var target = tvAttacterName.text.toString()
            if (target == getString(R.string.no_select)) {
                Toast.makeText(this, R.string.no_selection, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (tvSupporterName.text.toString() != getString(R.string.no_select)) {
                target += " " + tvSupporterName.text.toString()
            }
            target += " " + tvChips.text
            battleHell(target)
        }
        btnBotBattleUltraHell.setOnClickListener {
            var target = tvAttacterName.text.toString()
            if (target == getString(R.string.no_select)) {
                Toast.makeText(this, R.string.no_selection, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (tvSupporterName.text.toString() != getString(R.string.no_select)) {
                target += " " + tvSupporterName.text.toString()
            }
            target += " " + tvChips.text
            battleUltraHell(target)
        }
        cardViewChips.setOnClickListener {
            openChipDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK)
            return
        when (requestCode) {
            FOR_RESULT_ATTACKER -> {
                val monsterName = data!!.getStringExtra("name")
                tvAttacterName.text = monsterName
                writeTeamInfo()
                readTeamInfo()
            }
            FOR_RESULT_SUPPORTER -> {
                val monsterName = data!!.getStringExtra("name")
                tvSupporterName.text = monsterName
                writeTeamInfo()
                readTeamInfo()
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun writeTeamInfo() {
        var defaultAttacker = "attacker"
        var defaultSupporter = "supporter"
        when (oldTeam) {
            0 -> {
            }
            else -> {
                defaultAttacker = "attacter$oldTeam"
                defaultSupporter = "supporter$oldTeam"
            }
        }
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .edit()
                .putString(defaultAttacker, tvAttacterName.text.toString())
                .putString(defaultSupporter, tvSupporterName.text.toString())
                .putInt("team", team.selectedItemPosition)
                .commit()
    }

    private fun readTeamInfo() {
        if (isFinishing)
            return
        val progressDialog = ProgressDialog(this).apply {
            setContent(getString(R.string.loading))
            setCancelable(false)
            show()
        }
        GlobalScope.launch {
            val sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(this@TeamActivity)
            val defaultAttacker: String?
            val defaultSupporter: String?
            val defaultTeam = sharedPref.getInt("team", 0)
            val pets = boxData!!.pets
            oldTeam = defaultTeam
            when (defaultTeam) {
                0 -> {
                    defaultAttacker =
                            sharedPref.getString("attacker", getString(R.string.no_select))
                    defaultSupporter =
                            sharedPref.getString("supporter", getString(R.string.no_select))
                }
                else -> {
                    defaultAttacker =
                            sharedPref.getString("attacter$defaultTeam", getString(R.string.no_select))
                    defaultSupporter =
                            sharedPref.getString("supporter$defaultTeam", getString(R.string.no_select))
                }
            }
            val fakeAttacker = Pet()
            fakeAttacker.name = defaultAttacker!!
            val attackerIndex = pets.indexOf(fakeAttacker)
            if (attackerIndex == -1 || defaultAttacker == getString(R.string.no_select)){
                runOnUiThread {
                    tvAttacterName.text = getString(R.string.no_select)
                    GlideApp.with(this@TeamActivity)
                            .clear(findViewById<View>(R.id.attacter_img))
                }

            }else{
                val monster = pets[attackerIndex]
                runOnUiThread {
                    tvAttacterName.text = monster.name
                    val imageView = findViewById<ImageView>(R.id.attacter_img)
                    GlideApp.with(this@TeamActivity)
                            .asBitmap()
                            .load(monster.image)
                            .apply(RequestOptions().centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                            )
                            .centerCrop()
                            .into(CircularViewTarget(this@TeamActivity, imageView))
                }

            }
            val fakeSupporter = Pet()
            fakeSupporter.name = defaultSupporter!!
            val supporterIndex = pets.indexOf(fakeSupporter)
            if (supporterIndex == -1 || defaultSupporter == getString(R.string.no_select)){
                runOnUiThread {
                    tvSupporterName.text = getString(R.string.no_select)
                    GlideApp.with(this@TeamActivity)
                            .clear(findViewById<View>(R.id.supporter_img))
                }
            }else{
                val monster = pets[supporterIndex]
                runOnUiThread {
                    tvSupporterName.text = monster.name
                    val imageView = findViewById<ImageView>(R.id.supporter_img)
                    GlideApp.with(this@TeamActivity)
                            .asBitmap()
                            .load(monster.image)
                            .apply(RequestOptions().centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                            )
                            .centerCrop()
                            .into(CircularViewTarget(this@TeamActivity, imageView))
                }
            }
            runOnUiThread {
                team.setSelection(defaultTeam)
                LogUtil.catchAndIgnore {
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun openChipDialog() {
        val chips = chipData!!.chips
        val options = ArrayList<String>(chips.size)
        chips.forEach { chip ->
            options.add(chip.name + "\n" + chip.component)
        }
        AlertDialog.Builder(this)
                .setTitle(R.string.chips)
                .setItems(options.toTypedArray()) { _, which ->
                    tvChips.text =
                            chips[which].name
                }
                .show()
    }

    companion object {
        private const val FOR_RESULT_ATTACKER = 0
        private const val FOR_RESULT_SUPPORTER = 1
    }
}
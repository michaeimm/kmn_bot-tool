package tw.shounenwind.kmnbottool.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.jetbrains.anko.intentFor
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.gson.Pet
import tw.shounenwind.kmnbottool.util.CommandExecutor
import tw.shounenwind.kmnbottool.util.KmnBotDataLoader
import tw.shounenwind.kmnbottool.util.StaticCachedThreadPool
import tw.shounenwind.kmnbottool.util.glide.CircularViewTarget
import tw.shounenwind.kmnbottool.util.glide.GlideApp
import tw.shounenwind.kmnbottool.widget.ProgressDialog


class TeamActivity : AppCompatActivity() {

    private var oldTeam: Int = 0
    private var team: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)
        prepareScreen()

        readTeamInfo()
    }

    private fun prepareScreen() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        team = findViewById(R.id.team)
        val teamArray = arrayOfNulls<String>(10)
        teamArray[0] = "1"
        teamArray[1] = "2"
        teamArray[2] = "3"
        teamArray[3] = "4"
        teamArray[4] = "5"
        teamArray[5] = "6"
        teamArray[6] = "7"
        teamArray[7] = "8"
        teamArray[8] = "9"
        teamArray[9] = "10"
        val teamAdapter = ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, teamArray)
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        team!!.adapter = teamAdapter
        team!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                writeTeamInfo()
                readTeamInfo()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        findViewById<CardView>(R.id.team_card).setOnClickListener { team!!.performClick() }

        findViewById<CardView>(R.id.attacter_card).setOnClickListener {
            startActivityForResult(
                    intentFor<BoxActivity>("selectFor" to "attacker"),
                    FOR_RESULT_ATTACKER
            )
        }
        findViewById<CardView>(R.id.attacter_card).setOnLongClickListener {
            val target = findViewById<TextView>(R.id.attacter_name).text.toString()
            val fakeAttacker = Pet()
            fakeAttacker.name = target
            val petIndex = KmnBotDataLoader.boxData!!.pets!!.indexOf(fakeAttacker)
            if (petIndex != -1) {
                val intent = intentFor<DetailActivity>()
                intent.putExtra("pet", KmnBotDataLoader.boxData!!.pets!![petIndex])
                startActivity(intent)
                true
            }else {
                false
            }
        }
        findViewById<CardView>(R.id.supporter_card).setOnClickListener {
            startActivityForResult(
                    intentFor<BoxActivity>("selectFor" to "supporter"),
                    FOR_RESULT_SUPPORTER
            )
        }
        findViewById<CardView>(R.id.supporter_card).setOnLongClickListener {
            val target = findViewById<TextView>(R.id.supporter_name).text.toString()
            val fakeSupporter = Pet()
            fakeSupporter.name = target
            val petIndex = KmnBotDataLoader.boxData!!.pets!!.indexOf(fakeSupporter)
            if (petIndex != -1) {
                val intent = intentFor<DetailActivity>()
                intent.putExtra("pet", KmnBotDataLoader.boxData!!.pets!![petIndex])
                startActivity(intent)
                true
            }else {
                false
            }
        }
        if (KmnBotDataLoader.chipData?.chips != null && KmnBotDataLoader.chipData?.chips!!.isNotEmpty()) {
            findViewById<CardView>(R.id.chips_card).visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.bot_battle_normal).setOnClickListener {
            var target = findViewById<TextView>(R.id.attacter_name).text.toString()
            if (target == getString(R.string.no_select)) {
                Toast.makeText(this, R.string.no_selection, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (findViewById<TextView>(R.id.supporter_name).text.toString() != getString(R.string.no_select)) {
                target += " " + findViewById<TextView>(R.id.supporter_name).text.toString()
            }
            target += " " + findViewById<TextView>(R.id.chips).text
            CommandExecutor.battleNormal(this, target)
        }
        findViewById<Button>(R.id.bot_battle_hell).setOnClickListener {
            var target = findViewById<TextView>(R.id.attacter_name).text.toString()
            if (target == getString(R.string.no_select)) {
                Toast.makeText(this, R.string.no_selection, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (findViewById<TextView>(R.id.supporter_name).text.toString() != getString(R.string.no_select)) {
                target += " " + findViewById<TextView>(R.id.supporter_name).text.toString()
            }
            target += " " + findViewById<TextView>(R.id.chips).text
            CommandExecutor.battleHell(this, target)
        }
        findViewById<Button>(R.id.bot_battle_ultra_hell).setOnClickListener {
            var target = findViewById<TextView>(R.id.attacter_name).text.toString()
            if (target == getString(R.string.no_select)) {
                Toast.makeText(this, R.string.no_selection, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (findViewById<TextView>(R.id.supporter_name).text.toString() != getString(R.string.no_select)) {
                target += " " + findViewById<TextView>(R.id.supporter_name).text.toString()
            }
            target += " " + findViewById<TextView>(R.id.chips).text
            CommandExecutor.battleUltraHell(this, target)
        }
        findViewById<CardView>(R.id.chips_card).setOnClickListener {
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
                findViewById<TextView>(R.id.attacter_name).text = monsterName
                writeTeamInfo()
                readTeamInfo()
            }
            FOR_RESULT_SUPPORTER -> {
                val monsterName = data!!.getStringExtra("name")
                findViewById<TextView>(R.id.supporter_name).text = monsterName
                writeTeamInfo()
                readTeamInfo()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
                .putString(defaultAttacker, findViewById<TextView>(R.id.attacter_name).text.toString())
                .putString(defaultSupporter, findViewById<TextView>(R.id.supporter_name).text.toString())
                .putInt("team", team!!.selectedItemPosition)
                .commit()
    }

    private fun readTeamInfo() {
        if (isFinishing)
            return
        val progressDialog = ProgressDialog(this)
        progressDialog.setContent(getString(R.string.loading))
        progressDialog.progressDialog
                .cancelable(false)
                .show()
        StaticCachedThreadPool.instance.execute {
            val sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(this)
            val defaultAttacker: String?
            val defaultSupporter: String?
            val defaultTeam = sharedPref.getInt("team", 0)
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
            fakeAttacker.name = defaultAttacker
            val attackerIndex = KmnBotDataLoader.boxData!!.pets!!.indexOf(fakeAttacker)
            if (attackerIndex == -1 || defaultAttacker == getString(R.string.no_select)){
                runOnUiThread {
                    findViewById<TextView>(R.id.attacter_name).text = getString(R.string.no_select)
                    GlideApp.with(this)
                            .clear(findViewById<View>(R.id.attacter_img))
                }

            }else{
                val monster = KmnBotDataLoader.boxData!!.pets!![attackerIndex]
                runOnUiThread {
                    findViewById<TextView>(R.id.attacter_name).text = monster.name
                    val imageView = findViewById<ImageView>(R.id.attacter_img)
                    GlideApp.with(this)
                            .asBitmap()
                            .load(monster.image)
                            .apply(RequestOptions().centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                            )
                            .centerCrop()
                            .into(CircularViewTarget(this, imageView))
                }

            }
            val fakeSupporter = Pet()
            fakeSupporter.name = defaultSupporter
            val supporterIndex = KmnBotDataLoader.boxData!!.pets!!.indexOf(fakeSupporter)
            if (supporterIndex == -1 || defaultSupporter == getString(R.string.no_select)){
                runOnUiThread {
                    findViewById<TextView>(R.id.supporter_name).text = getString(R.string.no_select)
                    GlideApp.with(this)
                            .clear(findViewById<View>(R.id.supporter_img))
                }
            }else{
                val monster = KmnBotDataLoader.boxData!!.pets!![supporterIndex]
                runOnUiThread {
                    findViewById<TextView>(R.id.supporter_name).text = monster.name
                    val imageView = findViewById<ImageView>(R.id.supporter_img)
                    GlideApp.with(this)
                            .asBitmap()
                            .load(monster.image)
                            .apply(RequestOptions().centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                            )
                            .centerCrop()
                            .into(CircularViewTarget(this, imageView))
                }
            }
            runOnUiThread {
                team!!.setSelection(defaultTeam)
                try {
                    progressDialog.dismiss()
                }catch (e: Exception){
                    //Ignore
                }
            }
        }
    }

    private fun openChipDialog() {
        val options = ArrayList<String>()
        val iterator = KmnBotDataLoader.chipData!!.chips!!.iterator()
        while (iterator.hasNext()) {
            val chip = iterator.next()
            options.add(chip.name!! + "\n" + chip.component)
        }
        AlertDialog.Builder(this)
                .setTitle(R.string.chips)
                .setItems(options.toTypedArray()) { _, which ->
                    findViewById<TextView>(R.id.chips).text =
                            KmnBotDataLoader.chipData!!.chips!![which].name
                }
                .show()
    }

    companion object {
        private const val FOR_RESULT_ATTACKER = 0
        private const val FOR_RESULT_SUPPORTER = 1
    }
}
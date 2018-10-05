package tw.shounenwind.kmnbottool.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import org.jetbrains.anko.intentFor
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.util.CommandExecutor
import tw.shounenwind.kmnbottool.util.GlideApp
import tw.shounenwind.kmnbottool.util.KmnBotDataLoader


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
            startActivityForResult(intentFor<BoxActivity>("selectFor" to "attacker"), FOR_RESULT_ATTACKER)
        }
        findViewById<CardView>(R.id.supporter_card).setOnClickListener {
            startActivityForResult(intentFor<BoxActivity>("selectFor" to "supporter"), FOR_RESULT_SUPPORTER)
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
            CommandExecutor.battleUltraHell(this, target)
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
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val defaultAttacker: String?
        val defaultSupporter: String?
        val defaultTeam = sharedPref.getInt("team", 0)
        oldTeam = defaultTeam
        when (defaultTeam) {
            0 -> {
                defaultAttacker = sharedPref.getString("attacker", getString(R.string.no_select))
                defaultSupporter = sharedPref.getString("supporter", getString(R.string.no_select))
            }
            else -> {
                defaultAttacker = sharedPref.getString("attacter$defaultTeam", getString(R.string.no_select))
                defaultSupporter = sharedPref.getString("supporter$defaultTeam", getString(R.string.no_select))
            }
        }
        val len = KmnBotDataLoader.boxData!!.pets!!.size
        for (i in 0 until len) {
            val monster = KmnBotDataLoader.boxData!!.pets!![i]
            if (defaultAttacker == monster.name) {
                findViewById<TextView>(R.id.attacter_name).text = monster.name
                val imageView = findViewById<ImageView>(R.id.attacter_img)
                GlideApp.with(this)
                        .asBitmap()
                        .load(monster.image)
                        .apply(RequestOptions().centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                        )
                        .centerCrop()
                        .into(object : BitmapImageViewTarget(imageView) {
                            override fun setResource(resource: Bitmap?) {
                                val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                                circularBitmapDrawable.isCircular = true
                                imageView.setImageDrawable(circularBitmapDrawable)
                            }
                        })
            }
            if (defaultSupporter == monster.name) {
                findViewById<TextView>(R.id.supporter_name).text = monster.name
                val imageView = findViewById<ImageView>(R.id.supporter_img)
                GlideApp.with(this)
                        .asBitmap()
                        .load(monster.image)
                        .apply(RequestOptions().centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                        )
                        .centerCrop()
                        .into(object : BitmapImageViewTarget(imageView) {
                            override fun setResource(resource: Bitmap?) {
                                val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                                circularBitmapDrawable.isCircular = true
                                imageView.setImageDrawable(circularBitmapDrawable)
                            }
                        })
            }
        }
        if (defaultAttacker == getString(R.string.no_select)) {
            findViewById<TextView>(R.id.attacter_name).text = getString(R.string.no_select)
            GlideApp.with(this)
                    .clear(findViewById<View>(R.id.attacter_img))
        }
        if (defaultSupporter == getString(R.string.no_select)) {
            findViewById<TextView>(R.id.supporter_name).text = getString(R.string.no_select)
            GlideApp.with(this)
                    .clear(findViewById<View>(R.id.supporter_img))
        }
        team!!.setSelection(defaultTeam)

    }

    companion object {
        private const val FOR_RESULT_ATTACKER = 0
        private const val FOR_RESULT_SUPPORTER = 1
    }
}
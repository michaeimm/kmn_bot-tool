package tw.shounenwind.kmnbottool.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.TextView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.gson.Pet
import tw.shounenwind.kmnbottool.util.GlideApp

class DetailActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monster_detail)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val intent = intent
        val pet = intent.getParcelableExtra<Pet>("pet")
        title = pet.name
        findViewById<TextView>(R.id.name).text = pet.name
        findViewById<TextView>(R.id.rare).text =
                "${getString(R.string.sort_rare)}\n${pet.rare.toString()}"
        findViewById<TextView>(R.id.level).text =
                "${getString(R.string.monster_level)}\n${pet.level.toString()}"
        findViewById<TextView>(R.id.monster_class).text =
                "${getString(R.string.monster_class)}\n${pet.petClass.toString()}"
        findViewById<TextView>(R.id.text_holder).text = pet.skill

        GlideApp.with(this)
                .asBitmap()
                .load(pet.image)
                .apply(RequestOptions().centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                )
                .fitCenter()
                .into(findViewById(R.id.avatar))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                this.finish()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
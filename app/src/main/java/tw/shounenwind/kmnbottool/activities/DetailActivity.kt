package tw.shounenwind.kmnbottool.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_monster_detail.*
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.gson.Pet
import tw.shounenwind.kmnbottool.skeleton.BaseActivity
import tw.shounenwind.kmnbottool.util.glide.GlideApp

class DetailActivity : BaseActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monster_detail)

        bindToolbarHomeButton()

        val intent = intent
        val pet = intent.getParcelableExtra<Pet>("pet")!!
        val position = intent.getIntExtra("position", -1)
        title = pet.name
        tvName.text = pet.name
        tvRare.text = "${getString(R.string.sort_rare)}\n${pet.rare}"
        tvLevel.text = "${getString(R.string.monster_level)}\n${pet.level}"
        tvMonsterClass.text = "${getString(R.string.monster_class)}\n${pet.petClass}"
        tvMonsterDetail.text = pet.skill
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgMonster.transitionName = "list_p$position"
        }
        GlideApp.with(this)
                .asBitmap()
                .load(pet.image)
                .apply(RequestOptions().centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                )
                .fitCenter()
                .into(imgMonster)
    }
}
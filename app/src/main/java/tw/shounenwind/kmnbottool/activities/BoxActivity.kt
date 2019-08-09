package tw.shounenwind.kmnbottool.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Pair
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.intentFor
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.gson.BoxData
import tw.shounenwind.kmnbottool.gson.Pet
import tw.shounenwind.kmnbottool.skeleton.BaseActivity
import tw.shounenwind.kmnbottool.util.glide.CircularViewTarget
import tw.shounenwind.kmnbottool.util.glide.GlideApp
import java.lang.ref.WeakReference
import java.text.Collator
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class BoxActivity : BaseActivity() {

    private lateinit var boxData: BoxData
    private var selectFor: String? = null
    private lateinit var adapter: BoxAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mons_data)

        bindToolbarHomeButton()

        selectFor = intent.getStringExtra("selectFor")
        boxData = intent.getParcelableExtra("boxData")
        adapter = BoxAdapter(this)

        val listView = findViewById<RecyclerView>(R.id.list)
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.mons_data_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_profile -> {
                val s: String = if (boxData.player.completedRate != "0%") {
                    boxData.player.name +
                            "\n" +
                            getString(R.string.completion) +
                            ": " +
                            boxData.player.completedRate
                } else {
                    getString(R.string.no_data)
                }


                val alertDialog = AlertDialog.Builder(this)
                        .setMessage(s)
                        .setPositiveButton(R.string.confirm, null)
                        .create()
                alertDialog.show()
                return true
            }
            R.id.sort_series -> {
                mainScope?.launch {
                    showProgressDialog(getString(R.string.loading))
                    val data = ArrayList<Pet>(boxData.pets.size)
                    data.addAll(boxData.pets)
                    val diffResult = calculateDiff(adapter.monsters, data)
                    dismissProgressDialog()
                    adapter.monsters = data
                    diffResult.dispatchUpdatesTo(adapter)
                    item.isChecked = true
                }
                return true
            }
            R.id.sort_name -> {
                mainScope?.launch {
                    showProgressDialog(getString(R.string.loading))
                    val collator = Collator.getInstance(Locale.CHINESE)
                    val data = ArrayList<Pet>(boxData.pets.size)
                    data.addAll(boxData.pets)
                    sort(data, Comparator { o1, o2 ->
                        collator.compare(o1.name, o2.name)
                    })
                    val diffResult = calculateDiff(adapter.monsters, data)
                    dismissProgressDialog()
                    adapter.monsters = data
                    diffResult.dispatchUpdatesTo(adapter)
                    item.isChecked = true
                }
                return true
            }
            R.id.sort_rare -> {
                mainScope?.launch {
                    showProgressDialog(getString(R.string.loading))
                    val data = ArrayList<Pet>(boxData.pets.size)
                    data.addAll(boxData.pets)
                    sort(data, Comparator { o1, o2 ->
                        o1.rare.compareTo(o2.rare) * -1
                    })
                    val diffResult = calculateDiff(adapter.monsters, data)
                    dismissProgressDialog()
                    adapter.monsters = data
                    diffResult.dispatchUpdatesTo(adapter)
                    item.isChecked = true
                }
                return true
            }
            R.id.sort_level -> {
                mainScope?.launch {
                    showProgressDialog(getString(R.string.loading))
                    val data = ArrayList<Pet>(boxData.pets.size)
                    data.addAll(boxData.pets)
                    data.sortWith(Comparator { o1, o2 ->
                        val r1 = if (o1!!.level == o1.maxLevel) {
                            Int.MAX_VALUE
                        } else {
                            o1.level
                        }
                        val r2 = if (o2!!.level == o2.maxLevel) {
                            Int.MAX_VALUE
                        } else {
                            o2.level
                        }
                        r1.compareTo(r2) * -1
                    })
                    val diffResult = calculateDiff(adapter.monsters, data)
                    dismissProgressDialog()
                    adapter.monsters = data
                    diffResult.dispatchUpdatesTo(adapter)
                    item.isChecked = true
                }
                return true
            }
            R.id.sort_class -> {
                mainScope?.launch {
                    showProgressDialog(getString(R.string.loading))
                    val data = ArrayList<Pet>(boxData.pets.size)
                    data.addAll(boxData.pets)
                    sort(data, Comparator { o1, o2 ->
                        o1.petClass.compareTo(o2.petClass) * -1
                    })
                    val diffResult = calculateDiff(adapter.monsters, data)
                    dismissProgressDialog()
                    adapter.monsters = data
                    diffResult.dispatchUpdatesTo(adapter)
                }
                item.isChecked = true
                return true
            }
            R.id.sort_type -> {
                mainScope?.launch {
                    showProgressDialog(getString(R.string.loading))
                    val collator = Collator.getInstance(Locale.CHINESE)
                    val data = ArrayList<Pet>(boxData.pets.size)
                    data.addAll(boxData.pets)
                    sort(data, Comparator { o1, o2 ->
                        collator.compare(o1.type + o1.name, o2.type + o2.name)
                    })
                    val diffResult = calculateDiff(adapter.monsters, data)
                    dismissProgressDialog()
                    adapter.monsters = data
                    diffResult.dispatchUpdatesTo(adapter)
                }
                item.isChecked = true
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getPets() : List<Pet>{
        boxData.pets.also { pets ->
            val monsters = ArrayList<Pet>(pets.size + 1)

            if (selectFor != null && selectFor != "exp") {
                val pet = Pet().apply {
                    name = getString(R.string.no_select)
                    image = "https://i.imgur.com/6sPlPEzb.jpg"
                    battleType = ""
                    maxLevel = Integer.MAX_VALUE
                    level = 0
                    maxClass = Integer.MAX_VALUE
                    petClass = 0
                    rare = 0
                    series = ""
                    skill = ""
                    type = ""
                }
                monsters.add(pet)
            }
            monsters.addAll(pets)
            return monsters
        }
    }

    private suspend fun calculateDiff(
            oldData: List<Pet>,
            newData: List<Pet>
    ) = withContext(Dispatchers.IO) {
        DiffUtil.calculateDiff(BoxDiffCallback(oldData, newData))
    }

    private suspend fun <T> sort(
            data: ArrayList<T>,
            comparator: Comparator<T>
    ) = withContext(Dispatchers.IO) {
        data.sortWith(comparator)
    }

    private class BoxDiffCallback(private val oldData: List<Pet>, private val newData: List<Pet>) : DiffUtil.Callback() {
        override fun areItemsTheSame(p0: Int, p1: Int): Boolean {
            return oldData[p0].name == newData[p1].name
        }

        override fun getOldListSize(): Int {
            return oldData.size
        }

        override fun getNewListSize(): Int {
            return newData.size
        }

        override fun areContentsTheSame(p0: Int, p1: Int): Boolean {
            return oldData[p0].name == newData[p1].name
        }

    }

    private inner class BoxAdapter(mContext: BoxActivity) : RecyclerView.Adapter<BoxAdapter.MonsterDataHolder>() {

        private val wrContext: WeakReference<BoxActivity> = WeakReference(mContext)
        var monsters: List<Pet> = getPets()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonsterDataHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.unit_monster, parent, false)
            view.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            return MonsterDataHolder(view)
        }

        override fun getItemCount(): Int {
            return monsters.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: MonsterDataHolder, position: Int) {
            val mContext: BoxActivity = wrContext.get()?:return
            if (mContext.isFinishing)
                return
            val monster = monsters[position]
            when {
                monster.name == getString(R.string.no_select) ->
                    holder.name.text = monster.name
                monster.level == monster.maxLevel ->
                    holder.name.text = "${monster.name} (Lv.Max)"
                else ->
                    holder.name.text = "${monster.name} (Lv.${monster.level})"
            }
            val imageView = holder.image
            GlideApp.with(mContext)
                    .asBitmap()
                    .load(monster.image)
                    .apply(RequestOptions().centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                    )
                    .centerCrop()
                    .into(CircularViewTarget(mContext, imageView))
            holder.type.setTextColor(getMonsterColor(monster.type))
            if (monster.name != getString(R.string.no_select)) {
                if (monster.petClass == monster.maxClass) {
                    holder.monsterClass.text = "階級：${monster.petClass}(最大)"
                } else {
                    holder.monsterClass.text = "階級：${monster.petClass}"
                }
                val star = StringBuilder()
                val len = monster.rare
                repeat(len) {
                    star.append("☆")
                }
                holder.type.text = star.toString()
            }
            if (mContext.selectFor != null) {
                holder.item.setOnClickListener {
                    val intent = Intent()
                    intent.putExtra("name", monster.name)
                    mContext.setResult(Activity.RESULT_OK, intent)
                    mContext.finish()
                }
                if (monster.name != getString(R.string.no_select)) {
                    holder.item.setOnLongClickListener {
                        val intent = intentFor<DetailActivity>()
                        intent.putExtra("pet", monster)
                        startActivityWithTransition(intent)
                        true
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.transitionName = "list_p$position"
                }
                holder.item.setOnClickListener {
                    val intent = intentFor<DetailActivity>().apply {
                        putExtra("pet", monster)
                        putExtra("position", position)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startActivityWithTransition(
                                intent,
                                Pair.create(imageView, imageView.transitionName)
                        )
                    } else {
                        startActivityWithTransition(intent)
                    }
                }
            }
        }

        override fun onViewRecycled(holder: MonsterDataHolder) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.image.transitionName = null
            }
        }

        private fun getMonsterColor(type: String): Int {
            return when (type) {
                "[紅]" -> RED_TYPE
                "[綠]" -> GREEN_TYPE
                "[藍]" -> BLUE_TYPE
                "[黃]" -> YELLOW_TYPE
                "[黑]" -> BLACK_TYPE
                else -> Color.WHITE
            }
        }

        inner class MonsterDataHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val item = itemView.findViewById<ConstraintLayout>(R.id.monster_unit)!!
            val image = itemView.findViewById<ImageView>(R.id.monster_img)!!
            val name = itemView.findViewById<TextView>(R.id.monster_name)!!
            val type = itemView.findViewById<TextView>(R.id.monster_type)!!
            val monsterClass = itemView.findViewById<TextView>(R.id.monster_class)!!
        }

    }

    companion object {
        @JvmStatic
        private val RED_TYPE = Color.parseColor("#fc699a")
        @JvmStatic
        private val GREEN_TYPE = Color.parseColor("#aae467")
        @JvmStatic
        private val BLUE_TYPE = Color.parseColor("#5ac2f2")
        @JvmStatic
        private val YELLOW_TYPE = Color.parseColor("#f4e225")
        @JvmStatic
        private val BLACK_TYPE = Color.parseColor("#a379ec")
    }
}
package tw.shounenwind.kmnbottool.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.jetbrains.anko.intentFor
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.gson.Pet
import tw.shounenwind.kmnbottool.util.FlowJob
import tw.shounenwind.kmnbottool.util.KmnBotDataLoader
import tw.shounenwind.kmnbottool.util.LogUtil
import tw.shounenwind.kmnbottool.util.glide.CircularViewTarget
import tw.shounenwind.kmnbottool.util.glide.GlideApp
import tw.shounenwind.kmnbottool.widget.ProgressDialog
import java.lang.ref.WeakReference
import java.text.Collator
import java.util.*
import kotlin.Comparator


class BoxActivity : AppCompatActivity() {

    private var selectFor: String? = null
    private var progressDialog: ProgressDialog? = null
    private val adapter: BoxAdapter by lazy {
        BoxAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mons_data)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        selectFor = intent.getStringExtra("selectFor")

        val listView = findViewById<RecyclerView>(R.id.list)
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.mons_data_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_profile -> {
                val s: String = try {
                    KmnBotDataLoader.boxData!!.player!!.completedRate!!
                } catch (e: Exception) {
                    getString(R.string.no_data)
                }

                val alertDialog = AlertDialog.Builder(this)
                        .setMessage(getString(R.string.completion) + ": " + s)
                        .setPositiveButton(R.string.confirm, null)
                        .create()
                alertDialog.show()
                return true
            }
            R.id.sort_series -> {
                showProgressDialog(getString(R.string.loading))
                val data = ArrayList<Pet>()
                var diffResult: DiffUtil.DiffResult? = null
                FlowJob(this)
                        .addIOJob {
                            data.addAll(KmnBotDataLoader.boxData!!.pets!!)
                            diffResult = DiffUtil.calculateDiff(BoxDiffCallback(adapter.monsters, data))
                        }
                        .addUIJob {
                            dismissProgressDialog()
                            adapter.monsters = data
                            diffResult!!.dispatchUpdatesTo(adapter)
                        }
                        .start()
                item.isChecked = true
                return true
            }
            R.id.sort_name -> {
                showProgressDialog(getString(R.string.loading))
                val collator = Collator.getInstance(Locale.CHINESE)
                val data = ArrayList<Pet>()
                var diffResult: DiffUtil.DiffResult? = null
                FlowJob(this)
                        .addIOJob{
                            data.addAll(KmnBotDataLoader.boxData!!.pets!!)
                            data.sortWith(Comparator { o1, o2 ->
                                collator.compare(o1.name, o2.name)
                            })
                            diffResult = DiffUtil.calculateDiff(BoxDiffCallback(adapter.monsters, data))
                        }
                        .addUIJob{
                            dismissProgressDialog()
                            adapter.monsters = data
                            diffResult!!.dispatchUpdatesTo(adapter)
                        }
                        .start()
                item.isChecked = true
                return true
            }
            R.id.sort_rare -> {
                showProgressDialog(getString(R.string.loading))
                val data = ArrayList<Pet>()
                var diffResult: DiffUtil.DiffResult? = null
                FlowJob(this)
                        .addIOJob{
                            data.addAll(KmnBotDataLoader.boxData!!.pets!!)
                            data.sortWith(Comparator { o1, o2 -> o1!!.rare!!.compareTo(o2!!.rare!!) * -1 })
                            diffResult = DiffUtil.calculateDiff(BoxDiffCallback(adapter.monsters, data))
                        }
                        .addUIJob{
                            dismissProgressDialog()
                            adapter.monsters = data
                            diffResult!!.dispatchUpdatesTo(adapter)
                        }
                        .start()
                item.isChecked = true
                return true
            }
            R.id.sort_level -> {
                showProgressDialog(getString(R.string.loading))
                val data = ArrayList<Pet>()
                var diffResult: DiffUtil.DiffResult? = null
                FlowJob(this)
                        .addIOJob{
                            data.addAll(KmnBotDataLoader.boxData!!.pets!!)
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
                                r1!!.compareTo(r2!!) * -1
                            })
                            diffResult = DiffUtil.calculateDiff(BoxDiffCallback(adapter.monsters, data))
                        }
                        .addUIJob{
                            dismissProgressDialog()
                            adapter.monsters = data
                            diffResult!!.dispatchUpdatesTo(adapter)
                        }
                        .start()
                item.isChecked = true
                return true
            }
            R.id.sort_class -> {
                showProgressDialog(getString(R.string.loading))
                val data = ArrayList<Pet>()
                var diffResult: DiffUtil.DiffResult? = null
                FlowJob(this)
                        .addIOJob{
                            data.addAll(KmnBotDataLoader.boxData!!.pets!!)
                            data.sortWith(Comparator { o1, o2 -> o1!!.petClass!!.compareTo(o2!!.petClass!!) * -1 })
                            diffResult = DiffUtil.calculateDiff(BoxDiffCallback(adapter.monsters, data))
                        }
                        .addUIJob{
                            dismissProgressDialog()
                            adapter.monsters = data
                            diffResult!!.dispatchUpdatesTo(adapter)
                        }
                        .start()
                item.isChecked = true
                return true
            }
            R.id.sort_type -> {
                showProgressDialog(getString(R.string.loading))
                val collator = Collator.getInstance(Locale.CHINESE)
                val data = ArrayList<Pet>()
                var diffResult: DiffUtil.DiffResult? = null
                FlowJob(this)
                        .addIOJob{
                            data.addAll(KmnBotDataLoader.boxData!!.pets!!)
                            data.sortWith(Comparator { o1, o2 ->
                                collator.compare(o1.type + o1.name, o2.type + o2.name)
                            })
                            diffResult = DiffUtil.calculateDiff(BoxDiffCallback(adapter.monsters, data))
                        }
                        .addUIJob{
                            dismissProgressDialog()
                            adapter.monsters = data
                            diffResult!!.dispatchUpdatesTo(adapter)
                        }
                        .start()
                item.isChecked = true
                return true
            }
        }

        return super.onOptionsItemSelected(item)
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

    private fun getPets() : List<Pet>{
        val monsters = ArrayList<Pet>()

        if (selectFor != null && selectFor != "exp"){
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
        monsters.addAll(KmnBotDataLoader.boxData!!.pets!!)
        return monsters
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
                    .inflate(R.layout.monster_unit, parent, false)
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
            val mContext: BoxActivity? = wrContext.get()
            if (mContext == null || mContext.isFinishing)
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
            holder.type.setTextColor(getMonsterColor(monster.type!!))
            if (monster.name != getString(R.string.no_select)) {
                if (monster.petClass == monster.maxClass) {
                    holder.monsterClass.text = "階級：${monster.petClass}(最大)"
                } else {
                    holder.monsterClass.text = "階級：${monster.petClass}"
                }
                val star = StringBuilder()
                val len = monster.rare!!
                for (i in 0 until len) star.append("☆")
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
                        startActivity(intent)
                        true
                    }
                }
            } else {
                holder.item.setOnClickListener {
                    val intent = intentFor<DetailActivity>()
                    intent.putExtra("pet", monster)
                    startActivity(intent)
                }
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

            val item: ConstraintLayout = itemView.findViewById(R.id.monster_unit)
            val image: ImageView = itemView.findViewById(R.id.monster_img)
            val name: TextView = itemView.findViewById(R.id.monster_name)
            val type: TextView = itemView.findViewById(R.id.monster_type)
            val monsterClass: TextView = itemView.findViewById(R.id.monster_class)
        }

    }

    companion object {
        private val RED_TYPE = Color.parseColor("#ff4081")
        private val GREEN_TYPE = Color.parseColor("#8bc34a")
        private val BLUE_TYPE = Color.parseColor("#00b0ff")
        private val YELLOW_TYPE = Color.parseColor("#ffea00")
        private val BLACK_TYPE = Color.parseColor("#673AB7")
    }
}
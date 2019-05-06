package tw.shounenwind.kmnbottool.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.CacheControl
import okhttp3.Request
import okhttp3.ResponseBody
import tw.shounenwind.kmnbottool.gson.BoxData
import tw.shounenwind.kmnbottool.gson.ChipData

class KmnBotDataLoader {
    private var boxUrl: String? = null
    private var chipUrl: String? = null
    private var onSuccessListener: Func? = null
    private var onFailedListener: Func? = null

    private val boxData: BoxData?
        @Throws(Exception::class)
        get() {
            var body: ResponseBody? = null
            val boxData: BoxData
            try {
                val request = Request.Builder()
                        .cacheControl(
                                CacheControl.Builder()
                                        .noCache()
                                        .build()
                        ).url(boxUrl!!)
                        .build()
                val response = LinkUtil.instance.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("box")
                }

                body = response.body()!!
                boxData = Gson().fromJson<BoxData>(body.charStream(), BoxData::class.java)
                LogUtil.boxData(TAG, boxData)
            } finally {
                body?.close()
            }
            return boxData
        }

    private val chipsData: ChipData?
        @Throws(Exception::class)
        get() {
            var body: ResponseBody? = null
            val chipsData: ChipData
            try {
                val request = Request.Builder()
                        .cacheControl(
                                CacheControl.Builder()
                                        .noCache()
                                        .build()
                        ).url(chipUrl!!)
                        .build()
                val response = LinkUtil.instance.newCall(request).execute()

                body = response.body()!!
                val data = body.string()
                chipsData = Gson().fromJson(data, ChipData::class.java)
                LogUtil.d(TAG, data)
            } finally {
                body?.close()
            }
            return chipsData
        }

    fun setUser(user: String): KmnBotDataLoader {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            this.boxUrl = PETS_URL_PREFIX_TRANSFER + user
            this.chipUrl = CHIPS_URL_PREFIX_TRANSFER + user
        }else{
            this.boxUrl = PETS_URL_PREFIX + user + FILE_EXTENSION
            this.chipUrl = CHIPS_URL_PREFIX + user + FILE_EXTENSION
        }
        return this
    }

    fun setOnSuccessListener(onSuccessListener: Func): KmnBotDataLoader {
        this.onSuccessListener = onSuccessListener
        return this
    }

    inline fun setOnSuccessListener(crossinline body: (boxData: BoxData?, chipData: ChipData?) -> Unit): KmnBotDataLoader {
        return setOnSuccessListener(object : Func {
            override fun run(boxData: BoxData?, chipData: ChipData?) {
                body(boxData, chipData)
            }

        })
    }

    fun setOnFailedListener(onFailedListener: Func): KmnBotDataLoader {
        this.onFailedListener = onFailedListener
        return this
    }

    inline fun setOnFailedListener(crossinline body: (boxData: BoxData?, chipData: ChipData?) -> Unit): KmnBotDataLoader {
        return setOnFailedListener(object : Func {
            override fun run(boxData: BoxData?, chipData: ChipData?) {
                body(boxData, chipData)
            }
        })
    }

    fun start() {
        boxUrl!!
        onSuccessListener!!
        onFailedListener!!

        GlobalScope.launch {
            try {
                KmnBotDataLoader.boxData = this@KmnBotDataLoader.boxData!!
            } catch (e: Exception) {
                onFailedListener!!.run(null, null)
                LogUtil.printStackTrace(e)
                return@launch
            }

            try {
                chipData = this@KmnBotDataLoader.chipsData!!
            } catch (e: Exception) {
                onSuccessListener!!.run(boxData, null)
                LogUtil.printStackTrace(e)
                return@launch
            }

            onSuccessListener!!.run(boxData, chipData)
        }

    }

    interface Func {
        fun run(boxData: BoxData?, chipData: ChipData?)
    }

    companion object {
        private const val PETS_URL_PREFIX = "http://www.kmnbot.ga/pets/"
        private const val CHIPS_URL_PREFIX = "http://www.kmnbot.ga/chips/"
        @RequiresApi(api = Build.VERSION_CODES.P)
        private const val PETS_URL_PREFIX_TRANSFER = "https://shounenwind.tw/kmnbot/pets.php?username="
        @RequiresApi(api = Build.VERSION_CODES.P)
        private const val CHIPS_URL_PREFIX_TRANSFER = "https://shounenwind.tw/kmnbot/chips.php?username="
        private const val FILE_EXTENSION = ".json"
        var boxData: BoxData? = null
            private set(value) {
                field = value
            }
        var chipData: ChipData? = null
            private set(value) {
                field = value
            }
        private val TAG = KmnBotDataLoader::class.java.simpleName
    }
}

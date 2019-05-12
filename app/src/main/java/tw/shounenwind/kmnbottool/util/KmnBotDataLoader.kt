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

    @Throws(Exception::class)
    private fun loadBoxData(): BoxData {
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
        } catch (e: Exception) {
            throw(e)
        } finally {
            body?.close()
        }
        return boxData
    }

    @Throws(Exception::class)
    private fun loadChipData(): ChipData {
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
        } catch (e: Exception) {
            throw(e)
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

    inline fun setOnSuccessListener(crossinline body: (kmnBotData: KmnBotData) -> Unit): KmnBotDataLoader {
        return setOnSuccessListener(object : Func {
            override fun run(kmnBotData: KmnBotData) {
                body(kmnBotData)
            }

        })
    }

    fun setOnFailedListener(onFailedListener: Func): KmnBotDataLoader {
        this.onFailedListener = onFailedListener
        return this
    }

    inline fun setOnFailedListener(crossinline body: (kmnBotData: KmnBotData) -> Unit): KmnBotDataLoader {
        return setOnFailedListener(object : Func {
            override fun run(kmnBotData: KmnBotData) {
                body(kmnBotData)
            }
        })
    }

    fun start() {
        checkNotNull(boxUrl) {
            "機器狼 box 網址未設定"
        }
        val onSuccessListener = checkNotNull(onSuccessListener) {
            "onSuccessListener 未設定"
        }
        val onFailedListener = checkNotNull(onFailedListener) {
            "onFailedListener 未設定"
        }

        GlobalScope.launch {
            val boxData: BoxData
            try {
                boxData = loadBoxData()
            } catch (e: Exception) {
                onFailedListener.run(KmnBotData())
                LogUtil.printStackTrace(e)
                return@launch
            }

            val chipsData: ChipData
            try {
                chipsData = loadChipData()
            } catch (e: Exception) {
                onSuccessListener.run(
                        KmnBotData(boxData)
                )
                LogUtil.printStackTrace(e)
                return@launch
            }

            onSuccessListener.run(
                    KmnBotData(boxData, chipsData)
            )
        }

    }

    interface Func {
        fun run(kmnBotData: KmnBotData)
    }

    companion object {
        private const val PETS_URL_PREFIX = "http://www.kmnbot.ga/pets/"
        private const val CHIPS_URL_PREFIX = "http://www.kmnbot.ga/chips/"
        @RequiresApi(api = Build.VERSION_CODES.P)
        private const val PETS_URL_PREFIX_TRANSFER = "https://shounenwind.tw/kmnbot/pets.php?username="
        @RequiresApi(api = Build.VERSION_CODES.P)
        private const val CHIPS_URL_PREFIX_TRANSFER = "https://shounenwind.tw/kmnbot/chips.php?username="
        private const val FILE_EXTENSION = ".json"

        private val TAG = KmnBotDataLoader::class.java.simpleName
    }
}

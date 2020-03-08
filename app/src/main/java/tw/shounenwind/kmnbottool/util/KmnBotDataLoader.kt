package tw.shounenwind.kmnbottool.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.Request
import tw.shounenwind.kmnbottool.gson.BoxData
import tw.shounenwind.kmnbottool.gson.ChipData
import tw.shounenwind.kmnbottool.gson.fromJson
import tw.shounenwind.kmnbottool.gson.gsonInstances

class KmnBotDataLoader {
    private var boxUrl: String? = null
    private var chipUrl: String? = null
    private var onSuccessListener: Func? = null
    private var onFailedListener: Func? = null
    private val cacheControl by lazy {
        CacheControl.Builder()
                .noCache()
                .build()
    }

    @Throws(Exception::class)
    private suspend fun loadBoxData() = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                    .cacheControl(cacheControl)
                    .url(boxUrl!!)
                    .build()
            val response = LinkUtil.instance.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("box")
            }

            response.body!!.use { resBody ->
                val boxData = gsonInstances.fromJson<BoxData>(resBody.charStream())
                LogUtil.boxData(TAG, boxData)
                boxData
            }
        } catch (e: Exception) {
            throw(e)
        }
    }

    @Throws(Exception::class)
    private suspend fun loadChipData() = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                    .cacheControl(cacheControl)
                    .url(chipUrl!!)
                    .build()
            val response = LinkUtil.instance.newCall(request).execute()

            response.body!!.use { resBody ->
                val chipsData = gsonInstances.fromJson<ChipData>(resBody.charStream())
                LogUtil.d(TAG, chipsData.toString())
                chipsData
            }
        } catch (e: Exception) {
            throw(e)
        }
    }

    fun setUser(user: String): KmnBotDataLoader {
        this.boxUrl = PETS_URL_PREFIX + user + FILE_EXTENSION
        this.chipUrl = CHIPS_URL_PREFIX + user + FILE_EXTENSION
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

    suspend fun start() = withContext(Dispatchers.IO) {
        checkNotNull(boxUrl) {
            "機器狼 box 網址未設定"
        }
        val onSuccessListener = checkNotNull(onSuccessListener) {
            "onSuccessListener 未設定"
        }
        val onFailedListener = checkNotNull(onFailedListener) {
            "onFailedListener 未設定"
        }

        val boxData: BoxData
        try {
            boxData = loadBoxData()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onFailedListener.run(KmnBotData())
            }
            LogUtil.printStackTrace(e)
            return@withContext
        }

        val chipsData: ChipData
        try {
            chipsData = loadChipData()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onSuccessListener.run(KmnBotData(boxData))
            }
            LogUtil.printStackTrace(e)
            return@withContext
        }

        onSuccessListener.run(
                KmnBotData(boxData, chipsData)
        )

    }

    interface Func {
        fun run(kmnBotData: KmnBotData)
    }

    companion object {
        private const val PETS_URL_PREFIX = "https://www.kmnbot.ga/pets/"
        private const val CHIPS_URL_PREFIX = "https://www.kmnbot.ga/chips/"
        private const val FILE_EXTENSION = ".json"

        private val TAG = KmnBotDataLoader::class.java.simpleName
    }
}

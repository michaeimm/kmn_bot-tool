package tw.shounenwind.kmnbottool.util

import com.google.gson.Gson
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
                val response = LinkUtil.getLink().newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("box")
                }

                body = response.body()!!
                boxData = Gson().fromJson<BoxData>(body.charStream()!!, BoxData::class.java)
                LogUtil.boxData(TAG, boxData)
            } finally {
                body?.close()
            }
            return boxData
        }

    private var chipsData: ChipData? = null
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
                val response = LinkUtil.getLink().newCall(request).execute()

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
        this.boxUrl = PETS_URL_PREFIX + user + FILE_EXTENSION
        this.chipUrl = CHIPS_URL_PREFIX + user + FILE_EXTENSION
        return this
    }

    fun setOnSuccessListener(onSuccessListener: Func): KmnBotDataLoader {
        this.onSuccessListener = onSuccessListener
        return this
    }

    fun setOnFailedListener(onFailedListener: Func): KmnBotDataLoader {
        this.onFailedListener = onFailedListener
        return this
    }

    fun start() {
        boxUrl!!
        onSuccessListener!!
        onFailedListener!!

        StaticCachedThreadPool.instance.execute {

            try {
                KmnBotDataLoader.boxData = this.boxData!!
            } catch (e: Exception) {
                onFailedListener!!.run(null, null)
                LogUtil.printStackTrace(e)
                return@execute
            }

            try {
                KmnBotDataLoader.chipData = this.chipsData!!
            } catch (e: Exception) {
                onSuccessListener!!.run(boxData, null)
                LogUtil.printStackTrace(e)
                return@execute
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

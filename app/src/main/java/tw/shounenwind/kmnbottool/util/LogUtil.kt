package tw.shounenwind.kmnbottool.util

import android.util.Log

import com.google.gson.Gson

import tw.shounenwind.kmnbottool.BuildConfig
import tw.shounenwind.kmnbottool.gson.BoxData

object LogUtil {

    fun printStackTrace(e: Throwable) {
        if (BuildConfig.DEBUG)
            e.printStackTrace()
    }

    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG)
            Log.d(tag, msg)
    }

    fun boxData(tag: String, boxData: BoxData) {
        if (BuildConfig.DEBUG) {
            val msg = Gson().toJson(boxData, BoxData::class.java)
            Log.d(tag, msg)
        }
    }

}
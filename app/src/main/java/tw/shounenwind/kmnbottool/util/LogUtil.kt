package tw.shounenwind.kmnbottool.util

import android.util.Log
import tw.shounenwind.kmnbottool.BuildConfig
import tw.shounenwind.kmnbottool.gson.BoxData
import tw.shounenwind.kmnbottool.gson.gsonInstances

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
            val msg = gsonInstances.toJson(boxData)
            Log.d(tag, msg)
        }
    }

    inline fun catchAndPrint(action: () -> Unit){
        try {
            action()
        }catch (e: Exception){
            printStackTrace(e)
        }
    }

    inline fun catchAndIgnore(action: () -> Unit){
        try {
            action()
        }catch (e: Exception){
            //Ignore
        }
    }

}
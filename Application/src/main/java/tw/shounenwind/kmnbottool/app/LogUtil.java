package tw.shounenwind.kmnbottool.app;

import android.util.Log;

import tw.shounenwind.kmnbottool.BuildConfig;

class LogUtil {

    static void printStackTrace(Throwable e){
        if(BuildConfig.DEBUG)
            e.printStackTrace();
    }

    static void d(String tag, String msg){
        if(BuildConfig.DEBUG)
            Log.d(tag, msg);
    }

}

package tw.shounenwind.kmnbottool.util

import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.net.Proxy
import java.util.*
import java.util.concurrent.TimeUnit

object LinkUtil {
    private var link: OkHttpClient? = null

    fun getLink(): OkHttpClient {
        if (link == null) {
            link = OkHttpClient.Builder()
                    .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                    .proxy(Proxy.NO_PROXY)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build()
        }
        return link!!
    }
}
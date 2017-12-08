package tw.shounenwind.kmnbottool.app;

import java.net.Proxy;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;

public class LinkUtil {
    private static OkHttpClient link = null;

    public static OkHttpClient getLink() {
        if (link == null) {
            link = new OkHttpClient.Builder()
                    .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                    .proxy(Proxy.NO_PROXY)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build();
        }
        return link;
    }
}

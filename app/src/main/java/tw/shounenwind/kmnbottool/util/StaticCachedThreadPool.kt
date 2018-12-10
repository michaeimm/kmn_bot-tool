package tw.shounenwind.kmnbottool.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object StaticCachedThreadPool {
    val instance: ExecutorService by lazy {
        Executors.newCachedThreadPool()
    }
}

package tw.shounenwind.kmnbottool.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object StaticCachedThreadPool {
    private var threadPool: ExecutorService? = null

    val instance: ExecutorService
        get() {
            if (threadPool == null)
                synchronized(StaticCachedThreadPool::class.java) {
                    if (threadPool == null)
                        threadPool = Executors.newCachedThreadPool()
                }
            return threadPool!!
        }
}

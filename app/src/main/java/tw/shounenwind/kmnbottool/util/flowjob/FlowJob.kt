package tw.shounenwind.kmnbottool.util.flowjob

import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import tw.shounenwind.kmnbottool.util.StaticCachedThreadPool
import java.util.*

class FlowJob(base: Context) : ContextWrapper(base) {

    private val funcLinkedList = LinkedList<Runnable>()
    private val threadPool = StaticCachedThreadPool.instance
    @Volatile
    private var running = false
    private val mHandler: Handler = Handler(mainLooper)

    fun addIOJob(func: Func): FlowJob {
        if (running)
            throw RuntimeException("Running")
        funcLinkedList.add(object : IOJob(this) {
            override fun run() {
                func.run(this@FlowJob)
                done()
            }
        })
        return this
    }

    fun addUIJob(func: Func): FlowJob {
        if (running)
            throw RuntimeException("Running")
        funcLinkedList.add(object : UIJob(this) {
            override fun run() {
                func.run(this@FlowJob)
                done()
            }
        })
        return this
    }

    fun start() {
        if (funcLinkedList.size == 0) {
            return
        }
        running = true
        val action = funcLinkedList[0]
        if (action is IOJob) {
            threadPool.execute(action)
        } else if (action is UIJob) {
            mHandler.post(action)
        }
    }

    fun stop() {
        running = false
    }

    internal fun doNextJob() {
        if (running && funcLinkedList.size > 0) {
            funcLinkedList.removeAt(0)
            start()
        }
    }

    interface Func {
        fun run(f: FlowJob)
    }

}

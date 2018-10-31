package tw.shounenwind.kmnbottool.util

import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
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
        funcLinkedList.add(object : IOJob {
            override fun run() {
                func.run(this@FlowJob)
                doNextJob()
            }

        })
        return this
    }

    inline fun addIOJob(crossinline body: (self: FlowJob) -> Unit): FlowJob {
        return addIOJob(object : Func {
            override fun run(f: FlowJob) {
                body(this@FlowJob)
            }
        })
    }

    fun addUIJob(func: Func): FlowJob {
        if (running)
            throw RuntimeException("Running")
        funcLinkedList.add(object : UIJob {
            override fun run() {
                func.run(this@FlowJob)
                doNextJob()
            }
        })
        return this
    }

    inline fun addUIJob(crossinline body: (self: FlowJob) -> Unit): FlowJob {
        return addUIJob(object : Func {
            override fun run(f: FlowJob) {
                body(this@FlowJob)
            }
        })
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

    private fun doNextJob() {
        if (running && funcLinkedList.size > 0) {
            funcLinkedList.removeAt(0)
            start()
        }
    }

    interface Func {
        fun run(f: FlowJob)
    }

    private interface IOJob : Runnable

    private interface UIJob : Runnable

}
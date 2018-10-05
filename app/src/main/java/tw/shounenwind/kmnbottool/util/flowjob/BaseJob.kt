package tw.shounenwind.kmnbottool.util.flowjob

abstract class BaseJob(private val flowJob: FlowJob) : Runnable {

    fun done() {
        flowJob.doNextJob()
    }
}
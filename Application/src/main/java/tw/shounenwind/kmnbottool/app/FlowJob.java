package tw.shounenwind.kmnbottool.app;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlowJob extends ContextWrapper {

    private List<Runnable> funcLinkedList = new LinkedList<>();
    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;
    private final Handler mHandler;

    public FlowJob(Context base) {
        super(base);
        mHandler = new Handler(getMainLooper());
    }


    public FlowJob addIOJob(Func func) {
        if (running)
            throw new RuntimeException("Running");
        funcLinkedList.add(new IOJob() {
            @Override
            public void run() {
                func.run();
                done();
            }
        });
        return this;
    }

    public FlowJob addUIJob(Func func) {
        if (running)
            throw new RuntimeException("Running");
        funcLinkedList.add(new UIJob() {
            @Override
            public void run() {
                func.run();
                done();
            }
        });
        return this;
    }

    public void start() {
        if (funcLinkedList.size() == 0) {
            return;
        }
        running = true;
        Runnable action = funcLinkedList.get(0);
        if (action instanceof IOJob) {
            threadPool.execute(action);
        } else if (action instanceof UIJob) {
            mHandler.post(action);
        }
    }

    public void stop() {
        running = false;
    }

    private void doNextJob() {
        if (running && funcLinkedList.size() > 0) {
            funcLinkedList.remove(0);
            start();
        }
    }

    public interface Func {
        void run();
    }

    public abstract class baseJob implements Runnable {
        public void done() {
            doNextJob();
        }
    }

    abstract class IOJob extends baseJob {
    }

    abstract class UIJob extends baseJob {
    }
}
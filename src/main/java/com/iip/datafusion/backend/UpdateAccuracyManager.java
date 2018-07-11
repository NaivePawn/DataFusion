package com.iip.datafusion.backend;

import com.iip.datafusion.backend.channel.ChannelManager;
import com.iip.datafusion.backend.channel.WorkStealingChannel;
import com.iip.datafusion.backend.common.TerminationToken;
import com.iip.datafusion.backend.config.Capabilities;
import com.iip.datafusion.backend.executor.UpdateAccuracyJobExecutor;
import com.iip.datafusion.backend.job.accuracy.UpdateAccuracyJob;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UpdateAccuracyManager {
    private final TerminationToken token= new TerminationToken();

    // 关闭标识
    private volatile boolean shutdownRequested = false;

    private final static UpdateAccuracyManager singleInstance = new UpdateAccuracyManager();

    private UpdateAccuracyJobExecutor[] updateAccuracyJobExecutors = new UpdateAccuracyJobExecutor[Capabilities.JOBEXECUTOR_COUNT];

    private UpdateAccuracyManager(){
        @SuppressWarnings("unchecked")
        BlockingQueue<UpdateAccuracyJob>[] managedQueues = new LinkedBlockingQueue[Capabilities.JOBEXECUTOR_COUNT];

        ChannelManager.getInstance().setUpdateAccuracyChannel(new WorkStealingChannel<>(managedQueues));

        for (int i = 0; i < Capabilities.JOBEXECUTOR_COUNT; ++i){
            managedQueues[i] = new LinkedBlockingQueue<>();
            updateAccuracyJobExecutors[i] = new UpdateAccuracyJobExecutor(token, managedQueues[i]);
        }
    }

    public static UpdateAccuracyManager getInstance(){
        return singleInstance;
    }

    public void init(){
        for (int i = 0; i < Capabilities.JOBEXECUTOR_COUNT; ++i){
            updateAccuracyJobExecutors[i].start();
        }
    }

    public synchronized void shutdown(){
        if (shutdownRequested) {
            throw new IllegalStateException("shutdown already requested!");
        }

        for (int i = 0; i < Capabilities.JOBEXECUTOR_COUNT; ++i){
            updateAccuracyJobExecutors[i].terminate();
        }

        shutdownRequested = true;
    }

    public void commitJob(UpdateAccuracyJob updateAccuracyJob){
        try {
            ChannelManager.getInstance().getUpdateAccuracyChannel().put(updateAccuracyJob);
            token.reservations.incrementAndGet();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

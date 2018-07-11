package com.iip.datafusion.backend.executor;

import com.iip.datafusion.backend.JobRegistry;
import com.iip.datafusion.backend.channel.ChannelManager;
import com.iip.datafusion.backend.common.AbstractTerminatableThread;
import com.iip.datafusion.backend.common.TerminationToken;
import com.iip.datafusion.backend.jdbchelper.JDBCHelper;
import com.iip.datafusion.backend.job.JobStatusType;
import com.iip.datafusion.backend.job.accuracy.AccuracyJob;
import com.iip.datafusion.backend.job.accuracy.UpdateAccuracyJob;
import com.iip.datafusion.dgs.model.accuracy.*;
import com.iip.datafusion.util.dbutil.DataSourceRouterManager;
import com.iip.datafusion.util.jsonutil.Result;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class UpdateAccuracyJobExecutor extends AbstractTerminatableThread implements JobExecutor<UpdateAccuracyJob> {
    private final BlockingQueue<UpdateAccuracyJob> workQueue;

    private final JdbcTemplate jdbcTemplate = JDBCHelper.getJdbcTemplate();

    public UpdateAccuracyJobExecutor(TerminationToken token, BlockingQueue<UpdateAccuracyJob> workQueue){
        super(token);
        this.workQueue = workQueue;
    }

    @Override
    protected void doRun() throws Exception {
        UpdateAccuracyJob updateAccuracyJob = ChannelManager.getInstance().getUpdateAccuracyChannel().take(workQueue);
        JobRegistry.getInstance().update(updateAccuracyJob, JobStatusType.EXECUTING);

        try{
            doJob(updateAccuracyJob);
            JobRegistry.getInstance().update(updateAccuracyJob,JobStatusType.SUCCESS);
        } catch (Exception e){
            e.printStackTrace();
            JobRegistry.getInstance().update(updateAccuracyJob,JobStatusType.ERROR);
        } finally {
            terminationToken.reservations.decrementAndGet();
        }
    }

    @Override
    public void doJob(UpdateAccuracyJob job) throws Exception {

        String dataSourceId = job.getDataSourceId();
        String tableName = job.getTableName();
        List<String> sqlList = job.getSqlList();
        DataSourceRouterManager.setCurrentDataSourceKey(dataSourceId);

        Result result = new Result();
        try {
            for(int i = 0;i < sqlList.size();i++){
                jdbcTemplate.execute(sqlList.get(i));
            }
            result = new Result(1,"更新成功",null);
        }catch (Exception e){
            result = new Result(0,"出现内部错误",null);
        }
        System.out.println(result.toString());

    }
}

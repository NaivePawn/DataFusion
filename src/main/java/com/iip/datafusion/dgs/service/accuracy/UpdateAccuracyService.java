package com.iip.datafusion.dgs.service.accuracy;

import com.iip.datafusion.backend.JobRegistry;
import com.iip.datafusion.backend.UpdateAccuracyManager;
import com.iip.datafusion.backend.job.JobType;
import com.iip.datafusion.backend.job.accuracy.UpdateAccuracyJob;
import com.iip.datafusion.backend.parser.UpdateAccuracyParser;
import com.iip.datafusion.dgs.model.accuracy.UpdateAccuracyConfiguration;
import org.springframework.stereotype.Service;

/**
 * @author ChenQian
 * @date 2018/7/10
 */

@Service
public class UpdateAccuracyService {
    public void commitJob(UpdateAccuracyConfiguration updateAccuracyConfiguration,int userID){
        UpdateAccuracyJob updateAccuracyJob = UpdateAccuracyParser.parser(updateAccuracyConfiguration);
        updateAccuracyJob.setJobType(JobType.ACCURACY);
        updateAccuracyJob.setUserID(userID);
        JobRegistry.getInstance().regist(updateAccuracyJob);
        UpdateAccuracyManager.getInstance().commitJob(updateAccuracyJob);
    }
}

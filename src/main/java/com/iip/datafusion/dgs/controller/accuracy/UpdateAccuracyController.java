package com.iip.datafusion.dgs.controller.accuracy;

import com.iip.datafusion.dgs.model.accuracy.UpdateAccuracyConfiguration;
import com.iip.datafusion.dgs.service.accuracy.UpdateAccuracyService;
import com.iip.datafusion.util.jsonutil.Result;
import com.iip.datafusion.util.userutil.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author ChenQian
 * @date 2018/7/10
 */

@Controller
@RequestMapping("/dgs/accuracy")
public class UpdateAccuracyController {

    @Autowired
    UpdateAccuracyService updateAccuracyService;

    @Autowired
    UserManager userManager;

    @RequestMapping(path = {"/commitupdateaccuracyjob"}, method = RequestMethod.POST)
    @ResponseBody
    public Result update(@RequestBody UpdateAccuracyConfiguration updateAccuracyConfiguration) {
        try{
            updateAccuracyService.commitJob(updateAccuracyConfiguration,userManager.getUserId());
            return new Result(1,"Task finished successfully!",null);
        }catch (Exception e){
            return new Result(0,e.getMessage(),null);
        }
    }

}

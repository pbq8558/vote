package com.pbq.vote.controller;

import com.pbq.vote.common.ApiResponse;
import com.pbq.vote.dao.ActivityMapper;
import com.pbq.vote.po.ActivityPo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityMapper activityMapper;

    @GetMapping("/getActivityName")
    public String getActivityName(){
       return activityMapper.getActivityName();
    }
    @GetMapping("/getAllActivity")
    public ApiResponse getAllActivity(){
        List<ActivityPo> list = activityMapper.getAllActivity();
        logger.info("=====getAllActivity resp is {}.", list);
        return ApiResponse.succ(list);
    }
}

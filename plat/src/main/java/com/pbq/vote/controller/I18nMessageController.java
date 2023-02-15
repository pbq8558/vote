package com.pbq.vote.controller;

import com.pbq.vote.common.ApiResponse;
import com.pbq.vote.service.StringResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/i18n")
public class I18nMessageController {

    @Autowired
    private StringResourceService stringResourceService;

    @GetMapping("/getMessage")
    public ApiResponse getI18nMessage(@RequestParam("strId") String strId){

        return ApiResponse.succ(stringResourceService.getStringResourceByStrId(strId));


    }
}

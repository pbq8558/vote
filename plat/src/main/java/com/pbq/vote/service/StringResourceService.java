package com.pbq.vote.service;

import com.pbq.vote.dao.StringResourceMapper;
import com.pbq.vote.po.StringResourcePo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StringResourceService {

    @Autowired
    private StringResourceMapper stringResourceMapper;

    public StringResourcePo getStringResourceByStrId(String strId){

        StringResourcePo stringResourcePo = stringResourceMapper.getStringResourceByStrId(strId);

        return stringResourcePo;
    }
}

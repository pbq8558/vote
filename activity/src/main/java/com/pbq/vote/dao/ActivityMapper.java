package com.pbq.vote.dao;

import com.pbq.vote.po.ActivityPo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface ActivityMapper {
    String getActivityName();
    List<ActivityPo> getAllActivity();
}

package com.pbq.vote.dao;

import com.pbq.vote.po.StringResourcePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StringResourceMapper {

    StringResourcePo getStringResourceByStrId(@Param("strId") String strId);
}

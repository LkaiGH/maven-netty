package com.open.coinnews.app.dao;

import com.open.coinnews.app.model.UserMsg;
import com.open.coinnews.utils.MyMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component("UserMsgMapper")
public interface UserMsgMapper extends MyMapper<UserMsg> {

    void insertMsg(UserMsg msg);
}
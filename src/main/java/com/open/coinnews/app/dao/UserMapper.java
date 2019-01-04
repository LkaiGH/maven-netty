package com.open.coinnews.app.dao;

import com.open.coinnews.app.model.Users;
import com.open.coinnews.utils.MyMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component(value = "UserMapper")
public interface UserMapper extends MyMapper<Users> {

    void updateByImg(Users users);

    Users selectByUserId(String userId);
}
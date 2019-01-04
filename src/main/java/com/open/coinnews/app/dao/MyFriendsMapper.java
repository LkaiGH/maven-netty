package com.open.coinnews.app.dao;

import com.open.coinnews.app.model.MyFriends;

import com.open.coinnews.utils.MyMapper;
import org.apache.ibatis.annotations.Mapper;

import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "MyFriendsMapper")
public interface MyFriendsMapper extends MyMapper<MyFriends> {

}
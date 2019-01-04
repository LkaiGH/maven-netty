package com.open.coinnews.app.dao;

import com.open.coinnews.app.model.UserFriends;
import com.open.coinnews.app.model.Users;
import com.open.coinnews.app.model.UsersCustom;
import com.open.coinnews.utils.MyMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "UserMapperCustom")
public interface UserMapperCustom extends MyMapper<Users> {


    List<UsersCustom> queryFriendRequestList(String acceptUserId);

    List<UserFriends> queryMyfriends(String userId);

    void batchUpdateMsgSigned(List<String> msgList);

}
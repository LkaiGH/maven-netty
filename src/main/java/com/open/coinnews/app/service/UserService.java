package com.open.coinnews.app.service;

import com.open.coinnews.app.model.UserFriends;
import com.open.coinnews.app.model.Users;
import com.open.coinnews.app.model.UsersCustom;
import com.open.coinnews.netty.ChatMsg;

import java.util.List;

public interface UserService {

    boolean queryUserNameIsExite(String username);

    Users queryUserForLogin(String username, String md5Str);

    Users updateUserInfo(Users users);

    Users saveUser(Users users);
    
    Integer preconditionSearch(String myUserId, String friendUsername);
    
    Users queryUserInfoByUsername(String username);

    void sendFriendRequest(String myUserId,String friendUsername);
    
    List<UsersCustom> queryFriendRequestList(String acceptUserId);
    
    void deleteFridendRequest(String sendUserId,String acceptUserId);

    void passFridendRequest(String sendUserId,String acceptUserId);
    
    List<UserFriends> queryMyFriends(String userId);

    String savenMsg(ChatMsg chatMsg);

    //批量签收消息
    void udpateMsgSigned(List<String> msgIdList);
}

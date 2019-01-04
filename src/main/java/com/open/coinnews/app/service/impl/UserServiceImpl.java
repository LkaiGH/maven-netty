package com.open.coinnews.app.service.impl;

import com.open.coinnews.app.dao.*;
import com.open.coinnews.app.model.*;
import com.open.coinnews.basic.tools.MsgActionEnum;
import com.open.coinnews.basic.tools.MsgSignFlagEnum;
import com.open.coinnews.basic.tools.SearchFriendsStatusEnum;
import com.open.coinnews.netty.ChatMsg;
import com.open.coinnews.netty.DataContent;
import com.open.coinnews.netty.UserChannelRel;
import com.open.coinnews.utils.*;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import com.open.coinnews.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service("userServiceImpl")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserMapperCustom userMapperCustom;
    @Autowired
    private FriendsRecordMapper friendsRecordMapper;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Autowired
    private UserMsgMapper userMsgMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUserNameIsExite(String username) {

        Users users = new Users();
        users.setUsername(username);
        Users result = userMapper.selectOne(users);

        return result!=null ? true : false;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String md5Str) {

        Example userExample = new Example(Users.class);
        Criteria criteria = userExample.createCriteria();

        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", md5Str);

        Users result = userMapper.selectOneByExample(userExample);

        return result;

    }


    @Transactional(propagation = Propagation.REQUIRED)
    public Users saveUser(Users users){

        String userId = sid.nextShort();

        String qrCodepath = "C:\\Users\\Mliukai\\Desktop\\"+userId+"Code.png";
        qrCodeUtils.createQRCode(qrCodepath,"qrcode:"+users.getUsername());
        MultipartFile facefile = FileUtils.fileToMultipart(qrCodepath);

        String qrCodeUrl = "";
        try {
            qrCodeUrl = fastDFSClient.uploadQRCode(facefile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        users.setQrcode(qrCodeUrl);
        users.setId(userId);
        userMapper.insert(users);
        return users;

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Integer preconditionSearch(String myUserId, String friendUsername) {

        //用户不存在
        Users user = queryUserInfoByUsername(friendUsername);
        if(user == null){
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        //不能添加自己
        if(myUserId.equals(user.getId())){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        //搜索的好友已添加
        Example userExample = new Example(MyFriends.class);
        Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("myUserId",myUserId);
        criteria.andEqualTo("myFriendUserId",user.getId());

        MyFriends  myFriends = myFriendsMapper.selectOneByExample(userExample);
        if(myFriends!=null){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }


    @Transactional(propagation = Propagation.SUPPORTS)
    public Users queryUserInfoByUsername(String username){

        Example userExample = new Example(Users.class);
        Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username",username);
        return userMapper.selectOneByExample(userExample);

    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {

        Users friends = queryUserInfoByUsername(friendUsername);
        //查询发送好友请求记录

        Example fre = new Example(FriendsRecord.class);
        Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId", myUserId);
        frc.andEqualTo("acceptUserId", friends.getId());

        FriendsRecord friendsRecord= friendsRecordMapper.selectOneByExample(fre);

        if(friendsRecord ==null){
            //如果不是好友，并且没有添加
            String requestId = sid.nextShort();
            FriendsRecord friend= new FriendsRecord();
            friend.setId(requestId);
            friend.setSendUserId(myUserId);
            friend.setAcceptUserId(friends.getId());
            friend.setRequestDateTime(new Date());
            friendsRecordMapper.insert(friend);

        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<UsersCustom> queryFriendRequestList(String acceptUserId) {
        return userMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteFridendRequest(String sendUserId, String acceptUserId) {
        Example fre = new Example(FriendsRecord.class);
        Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId", sendUserId);
        frc.andEqualTo("acceptUserId",acceptUserId);

        friendsRecordMapper.deleteByExample(fre);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passFridendRequest(String sendUserId, String acceptUserId) {

        sendFriends(sendUserId, acceptUserId);
        sendFriends(acceptUserId, sendUserId);

        deleteFridendRequest(sendUserId, acceptUserId);

        //使用websocket 主动推送消息到请求发起者，更新他的通讯录列表为最新

        Channel sendchannel = UserChannelRel.get(sendUserId);
        if(sendchannel!=null){
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

            sendchannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<UserFriends> queryMyFriends(String userId) {

        return userMapperCustom.queryMyfriends(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String savenMsg(ChatMsg chatMsg) {

        UserMsg msg = new UserMsg();
        String msgId = sid.nextShort();
        msg.setId(msgId);
        msg.setAcceptUserId(chatMsg.getReceiverId());
        msg.setSendUserId(chatMsg.getSenderId());
        msg.setCreateTime(new Date());
        msg.setSignFlag(MsgSignFlagEnum.unsign.type);
        msg.setMsg(chatMsg.getMsg());
        userMsgMapper.insertMsg(msg);

        return msgId;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void udpateMsgSigned(List<String> msgIdList) {

        userMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void sendFriends(String sendUserId, String acceptUserId){

        MyFriends myFriends = new MyFriends();

        String id = sid.nextShort();
        myFriends.setId(id);
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);

        myFriendsMapper.insert(myFriends);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updateUserInfo(Users users) {

        userMapper.updateByImg(users);

        return querUserById(users.getId());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Users querUserById(String userId) {

        return userMapper.selectByUserId(userId);

    }



}

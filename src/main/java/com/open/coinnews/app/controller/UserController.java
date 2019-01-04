package com.open.coinnews.app.controller;

import com.open.coinnews.app.model.UserFriends;
import com.open.coinnews.app.model.Users;
import com.open.coinnews.app.service.UserService;
import com.open.coinnews.basic.tools.OperatorFriendRequestTypeEnum;
import com.open.coinnews.basic.tools.SearchFriendsStatusEnum;
import com.open.coinnews.model.bo.UserBo;
import com.open.coinnews.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "user")
public class UserController {

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private UserService userService;

    @PostMapping("/registOrlogin")
    @ResponseBody
    public JSONResult registOrlogin(@RequestBody Users users) throws Exception {
        if(StringUtils.isBlank(users.getUsername())||StringUtils.isBlank(users.getPassword())){
            return JSONResult.errorMsg("用户名密码不能为空");
        }
        //判断用户名是否存在 登录/注册
        boolean usernameIsExite = userService.queryUserNameIsExite(users.getUsername());
        Users usersResult =null;
        if(usernameIsExite){
            usersResult = userService.queryUserForLogin(users.getUsername(), MD5Utils.getMD5Str(users.getPassword()));
            if(usersResult == null){
                return JSONResult.errorMsg("用户名密码不正确");
            }
        }else {
            //注册
            users.setNickname(users.getUsername());
            users.setFaceImageBig("");
            users.setFaceImage("");
            users.setPassword(MD5Utils.getMD5Str(users.getPassword()));
            usersResult = userService.saveUser(users);

        }
        Map map = JsonUtil.convertObjectByFiled(usersResult,"id","username","nickname","faceImage","faceImageBig","qrcode");
        return JSONResult.ok(map);
    }

    /*
     *上传头像
     */
    @PostMapping(value = "/uploadFace")
    @ResponseBody
    public JSONResult uploadFace(@RequestBody UserBo userbo) throws Exception {

        //获取前端传过来的base64,转换成文件对象在上传
        String base64 = userbo.getFaceData();
        String userfacePath = "C:\\Users\\Mliukai\\Desktop\\"+userbo.getUserId()+"base64.png";
        FileUtils.base64ToFile(userfacePath,base64);

        MultipartFile facefile = FileUtils.fileToMultipart(userfacePath);

        String url = fastDFSClient.uploadBase64(facefile);

        String thump = "_80x80.";
        String arr[] = url.split("\\.");
        String thumpUrl =arr[0]+thump+arr[1];

        Users users = new Users();
        users.setId(userbo.getUserId());
        users.setFaceImage(thumpUrl);
        users.setFaceImageBig(url);

       Users us = userService.updateUserInfo(users);

        return JSONResult.ok(us);
    }


    @PostMapping(value = "/setNickName")
    @ResponseBody
    public JSONResult setNickName(@RequestBody UserBo userbo) throws Exception {


        Users users = new Users();
        users.setId(userbo.getUserId());
        users.setNickname(userbo.getNickname());

        Users us = userService.updateUserInfo(users);

        Map map = JsonUtil.convertObjectByFiled(us,"id","username","nickname","faceImage","faceImageBig","qrcode");

        return JSONResult.ok(map);
    }

    @PostMapping(value = "/search")
    @ResponseBody
    public JSONResult search(String myUserId,String friendUsername) throws Exception {

        if(StringUtils.isBlank(myUserId)|| StringUtils.isBlank(friendUsername)){
            return JSONResult.errorMsg("");
        }

        //搜索的用户不存在
        //搜索自己
        //搜索已添加的好友
        Integer status = userService.preconditionSearch(myUserId,friendUsername);
        if(status == SearchFriendsStatusEnum.SUCCESS.status){

            Users us = userService.queryUserInfoByUsername(friendUsername);
            Map map = JsonUtil.convertObjectByFiled(us,"id","username","nickname","faceImage","faceImageBig","qrcode");
            return JSONResult.ok(map);
        }else{
           String errmsg =  SearchFriendsStatusEnum.getMsgByKey(status);
            return JSONResult.errorMsg(errmsg);
        }

    }

    @PostMapping(value = "/addFriendRequest")
    @ResponseBody
    public JSONResult addFriendRequest(String myUserId,String friendUsername) throws Exception {

        if(StringUtils.isBlank(myUserId)|| StringUtils.isBlank(friendUsername)){
            return JSONResult.errorMsg("");
        }

        //搜索的用户不存在
        //搜索自己
        //搜索已添加的好友
        Integer status = userService.preconditionSearch(myUserId,friendUsername);
        if(status == SearchFriendsStatusEnum.SUCCESS.status){

            userService.sendFriendRequest(myUserId,friendUsername);
        }else{
            String errmsg =  SearchFriendsStatusEnum.getMsgByKey(status);
            return JSONResult.errorMsg(errmsg);
        }

        return JSONResult.ok();
    }

    @PostMapping(value = "/queryfriend")
    public JSONResult queryfriend(String userId ) throws Exception {

        if(StringUtils.isBlank(userId)){
            return JSONResult.errorMsg("");
        }

        return JSONResult.ok(userService.queryFriendRequestList(userId));


    }

    @PostMapping(value = "/operFriendRequest")
    public JSONResult operFriendRequest(String acceptUserId,String sendUserId,Integer operType ) throws Exception {

        if(StringUtils.isBlank(acceptUserId)||StringUtils.isBlank(sendUserId)||operType==null){
            return JSONResult.errorMsg("");
        }

        if(StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))){
            return JSONResult.errorMsg("");
        }

        if(operType == OperatorFriendRequestTypeEnum.IGNORE.type){
            //忽略  删除好友请求
            userService.deleteFridendRequest(sendUserId,acceptUserId);
        }else if(operType == OperatorFriendRequestTypeEnum.PASS.type){

            //通过 增加好友记录到数据库 并删除请求记录
            userService.passFridendRequest(sendUserId,acceptUserId);
        }

        List<UserFriends> list = userService.queryMyFriends(acceptUserId);
        return JSONResult.ok(list);


    }

    @PostMapping(value = "/myFriends")
    public JSONResult myFriends(String userId ) throws Exception {

        if(StringUtils.isBlank(userId)){
            return JSONResult.errorMsg("");
        }

        List<UserFriends> list = userService.queryMyFriends(userId);
        return JSONResult.ok(list);


    }
}

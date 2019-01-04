package com.open.coinnews.app.model;

public class UserFriends {

    private String friendUserId;

    private String friendUsername;

    private String friendFaceImage;

    private String FriendNickname;

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public void setFriendUsername(String friendUsername) {
        this.friendUsername = friendUsername;
    }

    public String getFriendFaceImage() {
        return friendFaceImage;
    }

    public void setFriendFaceImage(String friendFaceImage) {
        this.friendFaceImage = friendFaceImage;
    }

    public String getFriendNickname() {
        return FriendNickname;
    }

    public void setFriendNickname(String friendNickname) {
        FriendNickname = friendNickname;
    }
}

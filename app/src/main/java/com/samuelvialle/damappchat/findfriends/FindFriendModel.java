package com.samuelvialle.damappchat.findfriends;

public class FindFriendModel {
    /** 1 Ajout des variables **/
    private String UserName;
    private String Avatar;
    private String UserId;
    private boolean requestSent;

    /** 2 Géneration des constructeurs **/

    // 2.1 Un constructeur vide
    public FindFriendModel() {
    }

    // 2.2 Un constructeur avec nos variables
    public FindFriendModel(String userName, String avatar, String userId, boolean requestSent) {
        UserName = userName;
        Avatar = avatar;
        UserId = userId;
        this.requestSent = requestSent;
    }

    /** 3 Création des getter et setters **/
    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getAvatar() {
        return Avatar;
    }

    public void setAvatar(String avatar) {
        Avatar = avatar;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public boolean isRequestSent() {
        return requestSent;
    }

    public void setRequestSent(boolean requestSent) {
        this.requestSent = requestSent;
    }
}

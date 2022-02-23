package com.samuelvialle.damappchat.findfriends;

/** Model pour remplir la table friendRequest **/
public class FriendRequestsModel {
    String userId;
    String request_type;

    public FriendRequestsModel(String userId, String request_type) {
        this.userId = userId;
        this.request_type = request_type;
    }

    public FriendRequestsModel(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}

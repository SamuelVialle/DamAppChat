package com.samuelvialle.damappchat.Common;

public class NodesNames {

    // Node des Users
    public static final String USERS = "Users";

    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String ONLINE = "online";
    public static final String AVATAR = "avatar";

    // Node Friend_Requests, ce node affiche de la façon suivante :
    // + FriendRequests
    //   |_UserId du currentUser
    //      |_UserId du friend auquel on fait la demande
    //          |_request_type : received // sent
    public static final String FRIEND_REQUESTS = "FriendRequests";

    public static final String REQUEST_TYPE = "request_type";
}

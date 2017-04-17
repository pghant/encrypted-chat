package com.chatsecure.client;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jcavalie on 3/13/17.
 */
public class Message implements Serializable{

    final private User user;
    final private String content;
    private MessageType type;
    private ArrayList<User> userList = null;
    private Status status = null;



    public Message( MessageType type, User user, String content ){
        this.user = user;
        this.content = content;
        this.type=type;

    }

    public Message setUserList( final ArrayList<User> userList ){
        this.userList = userList;
        return this;
    }

    public Message setStatus( final Status status ){
        this.status = status;
        return this;
    }



    public User getUser( ){
        return user;
    }

    public String getContent( ){
        return content;
    }

    public MessageType getType( ){
        return type;
    }

    public Message setType(MessageType type ){
        this.type=type;
        return this;
    }


    public ArrayList<User> getUserList( ){
        return userList;
    }

    public Status getStatus( ){
        return status;
    }
}

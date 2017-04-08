package com.chatsecure.client;

/**
 * Created by jcavalie on 3/13/17.
 */
public class Message{

    final private User user;
    final private byte[] content;
    final private MessageType type;

    public Message(MessageType type, User user, byte[] content ){
        this.user = user;
        this.content = content;
        this.type=type;
    }

    public User getUser( ){
        return user;
    }

    public byte[] getContent( ){
        return content;
    }

    public MessageType getType( ){
        return type;
    }
}

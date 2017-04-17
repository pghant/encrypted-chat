package com.chatsecure.client;

import java.io.Serializable;

/**
 * Created by jcavalie on 3/13/17.
 */
public class User implements Serializable{

    private String name;


    public User( final String name ){
        this.name = name;
    }

    public String getName( ){
        return name;
    }
}

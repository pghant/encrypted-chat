package com.chatsecure.client;

import com.chatsecure.net.SecureConnection;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by jcavalie on 4/17/17.
 */
public class MessageReceiver implements Runnable
{

    private SecureConnection secureCon;
    private ChatController chatController;
    private String username;
    private User user;


    /**
     * constructor called by the user who is assuming role is P2P coordinator
     * @param portNum
     * @param chatController
     * @param username
     * @throws IOException
     * @throws ClassNotFoundException
     */
    MessageReceiver( int portNum,
                     final ChatController chatController,
                     final String username ) throws IOException, ClassNotFoundException{


        this.chatController = chatController;
        this.username = username;

        this.user = new User( this.username, Status.ONLINE );


        try{
            SecureConnection.initalize(this.user,portNum,true );
        } catch ( IOException | ClassNotFoundException e ){
            Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                       "Error in MessageReceiver Ctor",
                                                                        e );
            throw e;
        }

        this.secureCon = SecureConnection.getConnection( );

    }


    MessageReceiver(  String hostName, int portNum,
                      final ChatController chatController,
                      final String username ) throws IOException, ClassNotFoundException{


        this.chatController = chatController;
        this.username = username;


        this.user = new User( this.username, Status.ONLINE );


        try{
            SecureConnection.initalize( hostName,this.user,portNum,false );
        } catch ( IOException | ClassNotFoundException e ){
            Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                       "Error in MessageReceiver Ctor",
                                                                       e );
            throw e;
        }

        this.secureCon = SecureConnection.getConnection( );


    }

    @Override
    public void run( ){

    }
}

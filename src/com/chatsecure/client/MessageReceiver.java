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


        this.secureCon = SecureConnection.getConnection( );

        try{
            secureCon.initalize( user, portNum, true );
        } catch ( IOException | ClassNotFoundException e ){
            Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                       "Error in MessageReceiver Ctor",
                                                                        e );
            throw e;
        }



    }


    MessageReceiver(  String hostName, int portNum,
                      final ChatController chatController,
                      final String username ) throws IOException, ClassNotFoundException{


        this.chatController = chatController;
        this.username = username;


        this.user = new User( this.username, Status.ONLINE );


        this.secureCon = SecureConnection.getConnection( );
        try{
            secureCon.initalize( hostName, user, portNum, false );
        } catch ( IOException | ClassNotFoundException e ){
            Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                       "Error in MessageReceiver Ctor",
                                                                       e );
            throw e;
        }


    }

    @Override
    public void run( ){


        Message incoming_msg = null;
        while ( secureCon.isConnected( ) ){

            try{
                incoming_msg = secureCon.readMessage( );

                if ( incoming_msg == null ){
                    throw new IOException( "readMessage returned null" );
                }
            } catch ( IOException | ClassNotFoundException e ){
                Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                           "Error in MessageReceiver" +
                                                                           " reading new message",
                                                                           e );
                //returning will kill thread
                return;
            } finally{
                //use chatController to show error message then exit
            }

            switch ( incoming_msg.getType( ) ){

                case USER:
                    //get userList attached to message and compare against our current list
                    //because if you sign on last then you won't get ADDUSER messages from
                    //people already signed on so you'll need to get userlist this way
                    break;
                case REMOVEUSER:
                    //here we don't need to compare the entire attached userList with our
                    //current list; just remove user from onlineUsers list
                    break;
                case ADDUSER:
                    //here we don't need to compare the entire attached userList with our
                    //current list; just append the user onto onlineUsers list
                    break;
                case STATUS:
                    break;
                case HANDSHAKE:
                    break;
                case MIGRATECONTROLLER:
                    break;
            }


        }

    }
}

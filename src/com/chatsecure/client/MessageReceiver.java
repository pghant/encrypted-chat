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
    
    private static User userSelf;


    /**
     * constructor called by the userSelf who is assuming role is P2P coordinator
     * @param portNum
     * @param chatController
     * @param username
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public MessageReceiver( int portNum,
                            final ChatController chatController,
                            final String username ) throws IOException, ClassNotFoundException{


        this.chatController = chatController;
        this.username = username;

        userSelf = new User( this.username, Status.ONLINE );


        this.secureCon = SecureConnection.getConnection( );

        try{
            secureCon.initalize( userSelf, portNum, true );
        } catch ( IOException | ClassNotFoundException e ){
            Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                       "Error in MessageReceiver Ctor",
                                                                        e );
            throw e;
        }


        chatController.addSelfUserToChat( userSelf );

    }


    public MessageReceiver( String hostName, int portNum,
                            final ChatController chatController,
                            final String username ) throws IOException, ClassNotFoundException{


        this.chatController = chatController;
        this.username = username;


        userSelf = new User( this.username, Status.ONLINE );


        this.secureCon = SecureConnection.getConnection( );
        try{
            secureCon.initalize( hostName, userSelf, portNum, false );
        } catch ( IOException | ClassNotFoundException e ){
            Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                       "Error in MessageReceiver Ctor",
                                                                       e );
            throw e;
        }


    }

    public static User getUserSelf( ){
        return userSelf;
    }

    @Override
    public void run( ){


        Message incoming_msg = null;
        while ( secureCon.isConnected( ) ){

            try{

                incoming_msg = secureCon.readMessage( );


            } catch ( IOException e ){
                Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                           "Error in MessageReceiver" +
                                                                           " reading new message",
                                                                           e );
                //returning will kill thread
                break;
            } catch ( ClassNotFoundException e ){
                Logger.getLogger( MessageReceiver.class.toString( ) ).log( Level.SEVERE,
                                                                           "Error in MessageReceiver" +
                                                                           " reading new message",
                                                                           e );
                break;
            }

            assert incoming_msg != null;
            chatController.updateChatRoomState( incoming_msg.getType( ), incoming_msg );



        }

    }
}

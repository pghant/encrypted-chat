package com.chatsecure.net;

import com.chatsecure.client.Message;
import com.chatsecure.client.MessageType;
import com.chatsecure.client.Status;
import com.chatsecure.client.User;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by jcavalie on 3/14/17.
 */

/**
 * singleton class
 */
public class SecureConnection
{

    private static Thread coordinator = null;
    private static String hostAddr;
    private static Integer portNum;
    private static Socket userSocket = null;
    private static ObjectOutputStream stream_to_P2Pcoord;
    private static ObjectInputStream stream_from_P2Pcoord;
    private static User user;
    private static SecureConnection theInstance = new SecureConnection( );



    //if you are P2Pcoordinator then you generate this key and distribute to all clients via RSA;
    //else this key is set in call doHandShake
    private static byte[] SHARED_KEY = new byte[ 16 ];


    public static SecureConnection getConnection( ){

        return theInstance;
    }

    private SecureConnection( ){
    }


    /**
     * version of init called by the user who becomes P2P coordinator where host address is local host
     * @param new_user name of user becoming P2P coordinator
     * @param port_num port number
     * @param becomeP2Pcoordinator must be true here
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void initalize( User new_user, int port_num, boolean becomeP2Pcoordinator ) throws
            IOException, ClassNotFoundException{

        assert becomeP2Pcoordinator : "Initialized SecureConnection without host address and becomeP2P is false";

        initalize(InetAddress.getLocalHost().toString(),user,port_num,true);

    }


    /**
     * @param host_addr            IP address of P2P coordinator as string
     * @param port_num             port number for SecureChat app
     * @param becomeP2Pcoordinator flag to indicate if you are the sole P2P coordinator for a chat room
     */
    public static void initalize( String host_addr, User new_user, int port_num, boolean becomeP2Pcoordinator ) throws
            IOException, ClassNotFoundException{

        //if userSocket is not null then SecureConnection already initialized so just return
        if ( userSocket != null ){
            return;
        }

        user = new_user;
        hostAddr = host_addr;
        portNum = port_num;


        userSocket = new Socket( hostAddr, portNum );
        stream_from_P2Pcoord = new ObjectInputStream( userSocket.getInputStream( ) );
        stream_to_P2Pcoord = new ObjectOutputStream( userSocket.getOutputStream( ) );


        if ( becomeP2Pcoordinator ){
            //generate random 128-bit key for AES128 that will be shared with all clients using RSA and
            SecureRandom random = new SecureRandom( );

            random.nextBytes( SHARED_KEY );

            coordinator = new Thread( new P2Pcoordinator( ) );
            coordinator.start();

        } else{

            try{
                doHandShake( );
            } catch ( IOException | ClassNotFoundException e ){
                Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE, "Error Initialize :doHandShake()", e );
                throw e;
            }
        }

    }

    private static Long byteArrayToLong( byte[] b ){
        final ByteBuffer bb = ByteBuffer.wrap( b );
        return bb.getLong( );
    }

    private static byte[] LongToByteArray( Long i ){
        final ByteBuffer bb = ByteBuffer.allocate( Long.SIZE / Byte.SIZE );
        bb.putLong( i );
        return bb.array( );
    }

    private static String byteToString( byte[] b ){
        return new String( b, Charset.forName( "US-ASCII" ) );
    }

    public static void doHandShake( ) throws IOException, ClassNotFoundException{
        byte[] pubkey;

        byte[] shared_secret;

        //EXAMPLE of what call will look like
        //pubkey=RSA.get_public_key()

        //testing
        pubkey = "PUBLIC_KEY_VALUE".getBytes( );
        //testing


        try{

            //sending public key to P2P coordinator; so this is the first thing com.chatsecure.client sends to
            //p2p coordinator after connecting in initialize

            stream_to_P2Pcoord.writeObject( new Message( MessageType.HANDSHAKE,
                                                         user,
                                                         byteToString( pubkey ) ) );


            Message returnMsg = (Message) stream_from_P2Pcoord.readObject( );

            assert returnMsg.getType( ) ==
                   MessageType.HANDSHAKE : "doHandShake got back wrong message type from P2P coordinator";

            //here the com.chatsecure.client is using his private RSA key to decrypt this message--that key should exist
            //within RSA object


            //shared_secret=RSA.decryptSharedSecret(returnMsg.getContent().getBytes())

            //testing
            shared_secret = returnMsg.getContent( ).getBytes( );
            //testing


        } catch ( IOException | ClassNotFoundException e ){
            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE,
                                                                        "Error in doHandShake()", e );
            throw e;
        } finally{
            stream_from_P2Pcoord.close( );
            stream_to_P2Pcoord.close( );

        }


        SHARED_KEY = shared_secret;

    }


    public static void writeMessage( Message msg ) throws IOException{

        writeMessage_( msg, null );
    }

    public static void writeMessage( Message msg, ObjectOutputStream oos ) throws IOException{

        writeMessage_( msg, oos );
    }


    private static void writeMessage_( Message msg, ObjectOutputStream oos ) throws IOException{
        byte[] msg_bytes;
        byte[] encrypted_msg_bytes;

        try ( ByteArrayOutputStream byte_stream = new ByteArrayOutputStream( );
              ObjectOutputStream to_byte_stream = new ObjectOutputStream( byte_stream ) ){

            to_byte_stream.writeObject( msg );
            msg_bytes = byte_stream.toByteArray( );

            //example of what the call will look like eventually when I encrypt this
            //message with AES128 counter mode using the shared key established in doHandShake
            // encrypted_msg_bytes = Encrypter.AESencrypt(SHARED_KEY,msg_bytes);

            //test call until encryption alg is implemented
            encrypted_msg_bytes = "Encrypted User Message".getBytes( );
            //testing


            ( ( oos != null ) ? oos : stream_to_P2Pcoord ).write( encrypted_msg_bytes );

        } catch ( IOException e ){
            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE, "Error in WriteMessage()", e );
            throw e;
        }
    }

    /**
     * helper function to read in all bytes from input stream without knowing how many bytes in stream;
     * this is needed because calls to ObjectInputStream.read will block until data shows up and so there is
     * no way to know number of bytes in stream before blocking
     *
     * @return
     *
     * @throws IOException
     */
    private static byte[] consumeAllBytesFromInputStream( ObjectInputStream iis ) throws IOException{
        ArrayList<Byte> byte_array = new ArrayList<>( );

        do{
            try{
                byte_array.add( ( ( iis != null ) ? iis : stream_from_P2Pcoord ).readByte( ) );
            } catch ( IOException e ){
                Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE,
                                                                            "Socket Read Error in ReadMessage()", e );
                throw e;
            }

        } while ( ( ( iis != null ) ? iis : stream_from_P2Pcoord ).available( ) > 0 );

        byte[] msg_bytes = new byte[ byte_array.size( ) ];


        //unboxing bytes from Bytes
        int idx = 0;
        for ( Byte b : byte_array ){
            msg_bytes[ idx++ ] = b;
        }

        return msg_bytes;

    }


    public static Message readMessage( ) throws IOException, ClassNotFoundException{
        return readMessage_( null );
    }

    public static Message readMessage( ObjectInputStream iis ) throws IOException, ClassNotFoundException{
        return readMessage_( iis );
    }


    private static Message readMessage_( ObjectInputStream iis ) throws IOException, ClassNotFoundException{


        byte[] encrypted_msg_bytes;
        byte[] decrypted_msg_bytes;


        encrypted_msg_bytes = consumeAllBytesFromInputStream( iis );

        //example of what call will look like when I decrypt using AES decryption alg using
        //shared key established in doHandShake
        //decrypted_msg_bytes = Encrypter.AESdecrypt(SHARED_KEY,encrypted_msg_bytes);

        //testing
        decrypted_msg_bytes = "Decrypted User Message".getBytes( );
        //testing

        Message new_msg;

        try ( ByteArrayInputStream byte_stream = new ByteArrayInputStream( decrypted_msg_bytes );
              ObjectInputStream from_byte_stream = new ObjectInputStream( byte_stream ) ){

            new_msg = (Message) from_byte_stream.readObject( );

        } catch ( ClassNotFoundException e ){
            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE,
                                                                        "Deserializing Error in ReadMessage()", e );
            throw e;
        }


        return new_msg;
    }

    void updateStatus(Status status) throws IOException{
        writeMessage( new Message( MessageType.STATUS,
                                   user, null ).setStatus( status ) );

    }

    void closeConnection() throws IOException{

        writeMessage( new Message( MessageType.REMOVEUSER,user,null ) );

//        if( coordinator != null){
//            coordinator.closeConnection( null );
//        }
    }


    private static class P2Pcoordinator implements Runnable
    {


        final ConcurrentMap<Long, Socket> host_connections = new ConcurrentHashMap<>( );
        final AtomicBoolean continue_coordinating = new AtomicBoolean( true );
        final ConcurrentHashMap<Long, User> online_users;
        final User ControllerUser;
        final ArrayBlockingQueue<Message> outgoingMessages;


        Thread P2Plistener_thread;

        P2Pcoordinator( ) throws IOException{
            P2Plistener_thread = new Thread( new P2Plistener( ) );
            P2Plistener_thread.start( );

            online_users = new ConcurrentHashMap<>( );
            ControllerUser = new User( "P2PController", Status.CONTROLLER );

            //add connection to user that is P2P coordinator
            online_users.put( Thread.currentThread( ).getId( ), user );
            host_connections.put( Thread.currentThread( ).getId( ), userSocket );

            int queueCapacity=100;
            outgoingMessages = new ArrayBlockingQueue<Message>( queueCapacity);

        }

        void closeConnection( Long threadID ){

            host_connections.forEach( ( ID, socketConn ) -> {

                if ( ID.equals( threadID ) || ( threadID == null ) ){
                    try{
                        socketConn.close( );
                    } catch ( IOException e ){
                        Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                               "Socket close failed in P2Plistner" );



                    }
                }


            } );

            if ( threadID == null ){

                host_connections.clear( );

            } else{
                online_users.remove( threadID );
                host_connections.remove( threadID );
            }
        }



        @Override
        public void run( ){
            while ( continue_coordinating.get( ) ){

                Message msg = null;
                try{
                    msg = outgoingMessages.take( );
                } catch ( InterruptedException e ){
                    Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                           "Server Socket failed in P2Plistner" );

                }

                final Message finalMsg = msg;
                host_connections.forEach( ( ID, socketConn ) -> {
                    if ( online_users.get( ID ) != finalMsg.getUser( ) ){

                        try{
                            writeMessage( finalMsg, new ObjectOutputStream( socketConn.getOutputStream( ) ) );
                        } catch ( IOException e ){
                            Logger.getLogger( this.getClass( ).toString( ) ).log( Level.SEVERE,
                                                                                  "A broadcast message failed to send",
                                                                                  e );

                        }
                        if ( finalMsg.getType( ) == MessageType.REMOVEUSER ){
                            closeConnection( ID );
                        }
                    }
                } );


            }
        }

        private class P2Plistener implements Runnable
        {

            final ServerSocket listening_sock;
            final ArrayList<Thread> threadList;

            final static int BACKLOG = 10;

            public P2Plistener( ) throws IOException{

                try{
                    listening_sock = new ServerSocket( SecureConnection.portNum, BACKLOG );
                } catch ( IOException e ){
                    Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                           "Server Socket failed in P2Plistner" );
                    throw e;
                }

                threadList = new ArrayList<Thread>();
            }

            @Override
            public void run( ){
                Thread t;
                while ( true ){
                    try{

                        t = new Thread( new P2Phandler( listening_sock.accept( ) ) );
                        threadList.add( t );
                        t.start( );


                    } catch ( IOException e ){
                        Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                               "Server userSocket accept failed in P2Plistner" );

                        continue_coordinating.set( false );
                        break;

                    }


                }

                P2Pcoordinator.this.closeConnection( null );


            }
        }

        private class P2Phandler implements Runnable
        {
            final Socket connected_sock;
            Boolean handshakeDone = false;

            P2Phandler( Socket connected_sock ){
                this.connected_sock = connected_sock;
                host_connections.put( Thread.currentThread( ).getId( ), connected_sock );



            }



            @Override
            public void run( ){

                try ( ObjectInputStream iis = new ObjectInputStream( connected_sock.getInputStream( ) );
                      ObjectOutputStream oos = new ObjectOutputStream( connected_sock.getOutputStream( ) ) ){

                    Message msg;
                    while ( connected_sock.isConnected( ) ){

                        if ( !handshakeDone ){

                            msg = (Message) iis.readObject( );

                            assert msg.getType( ) == MessageType.HANDSHAKE :
                                    "First sent message must be type HANDSHAKE";

                            online_users.put( Thread.currentThread( ).getId( ), msg.getUser( ) );

                            byte[] usersPubkey = msg.getContent( ).getBytes( );

                            byte[] encryptedSharedSecret;

                            //WHAT CALL WILL LOOK LIKE
                            //encryptedSharedSecret = RSA.encrypt( usersPubkey, SHARED_KEY );
                            //

                            //testing
                            encryptedSharedSecret = "ENCRYPTED SHARED SECRET".getBytes( );


                            oos.writeObject( new Message( MessageType.HANDSHAKE,
                                                          ControllerUser,
                                                          byteToString( encryptedSharedSecret ) ) );


                            outgoingMessages.put( msg.setType( MessageType.ADDUSER )
                                                          .setUserList( new ArrayList<>( online_users.values( ) ) ) );


                            handshakeDone = true;

                        } else{

                            msg = readMessage( iis );

                            switch ( msg.getType( ) ){


                                case STATUS:

                                    //fall through
                                case REMOVEUSER:

                                    online_users.remove( Thread.currentThread( ).getId( ) );

                                    //fall through
                                case USER:
                                    msg.setUserList( new ArrayList<>( online_users.values( ) ) );
                                    outgoingMessages.put( msg );
                                    break;

                            }
                        }


                    }

                } catch ( IOException | ClassNotFoundException | InterruptedException e ){
                    Logger.getLogger( this.getClass( ).toString( ) ).log( Level.SEVERE,
                                                                          "Stream created failed in P2Phandler", e );

                } finally{
                    closeConnection( Thread.currentThread( ).getId( ) );


                }


            }
        }


    }

}

package com.chatsecure.net;

import com.chatsecure.client.Message;
import com.chatsecure.client.MessageType;
import com.chatsecure.client.Status;
import com.chatsecure.client.User;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Objects;
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

    private Thread coordinator = null;
    private String hostAddr;
    private Integer portNum;
    private Socket userSocket = null;
    private ObjectOutputStream stream_to_P2Pcoord;
    private ObjectInputStream stream_from_P2Pcoord;
    private User userSelf;
    private static final SecureConnection theInstance = new SecureConnection( );

    private static AtomicBoolean initialized = new AtomicBoolean( false );


    //if you are P2Pcoordinator then you generate this key and distribute to all clients via RSA;
    //else this key is set in call doHandShake
    private static byte[] SHARED_KEY = new byte[ 16 ];




    public static SecureConnection getConnection( ){

        return theInstance;
    }

    private SecureConnection( ){
    }





    /**
     * version of init called by the userSelf who becomes P2P coordinator where host address is local host
     *
     * @param new_user             name of userSelf becoming P2P coordinator
     * @param port_num             port number
     * @param becomeP2Pcoordinator must be true here
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void initalize( User new_user, int port_num, boolean becomeP2Pcoordinator ) throws
            IOException, ClassNotFoundException{

        assert becomeP2Pcoordinator : "Initialized SecureConnection without host address and becomeP2P is false";

        initalize( InetAddress.getLocalHost( ).getHostAddress(), userSelf, port_num, true );


    }


    /**
     * @param host_addr            IP address of P2P coordinator as string
     * @param port_num             port number for SecureChat app
     * @param becomeP2Pcoordinator flag to indicate if you are the sole P2P coordinator for a chat room
     */
    public synchronized void initalize( String host_addr, User new_user, int port_num, boolean becomeP2Pcoordinator ) throws
            IOException, ClassNotFoundException{

        //if userSocket is not null then SecureConnection already initialized so just return
        if ( userSocket != null ){
            return;
        }

        userSelf = new_user;
        hostAddr = host_addr;
        portNum = port_num;


        if ( becomeP2Pcoordinator ){
            //generate random 128-bit key for AES128 that will be shared with all clients using RSA and
            SecureRandom random = new SecureRandom( );

            random.nextBytes( SHARED_KEY );

            Long myID = Thread.currentThread( ).getId( );
            coordinator = new Thread( new P2Pcoordinator( myID ) );
            coordinator.setDaemon(true);
            coordinator.start( );


            //start coordinator then connect where hostAddr is localAddr so
            //coordinator will handle connection same way it would handle
            //connection from remote host.
            while ( true ){
                try{
                    //chance that P2P coordinator may not have started P2P listener
                    //quick enough and this call will throw, so loop on socketExceptions
                    //until sucessful
                    userSocket = new Socket( hostAddr, portNum );
                } catch ( SocketException e ){
                    Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.INFO,
                                                                                "Looping waiting for p2p" +
                                                                                " listener to start", e );
                    continue;
                }
                break;

            }
            stream_to_P2Pcoord = new ObjectOutputStream( userSocket.getOutputStream( ) );
            stream_from_P2Pcoord = new ObjectInputStream( userSocket.getInputStream( ) );


            stream_to_P2Pcoord.writeObject( new Message( MessageType.SELFCONNECTION,
                                                         userSelf, null ) );

        } else{
            userSocket = new Socket( hostAddr, portNum );
            stream_to_P2Pcoord = new ObjectOutputStream( userSocket.getOutputStream( ) );

            stream_from_P2Pcoord = new ObjectInputStream( userSocket.getInputStream( ) );

            try{
                doHandShake( );
            } catch ( IOException | ClassNotFoundException e ){
                Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE,
                                                                            "Error Initialize :doHandShake()", e );
                throw e;
            }
        }

        notifyAll( );

    }

    public boolean isConnected( ){
        return userSocket.isConnected( );
    }

    public synchronized SecureConnection waitForInitialization( ){
        //TESTING
        long loopCount = 0;
        //

        while ( !initialized.get( ) ){
            try{

                //TESTING
                if ( loopCount++ == 5 ){
                    return this;
                }
                //

                wait( 1 );

            } catch ( InterruptedException e ){
                Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.WARNING,
                                                                            "SecureConnection interrupted wait", e );

            }
        }

        return this;
    }

    private Long byteArrayToLong( byte[] b ){
        final ByteBuffer bb = ByteBuffer.wrap( b );
        return bb.getLong( );
    }

    private byte[] LongToByteArray( Long i ){
        final ByteBuffer bb = ByteBuffer.allocate( Long.SIZE / Byte.SIZE );
        bb.putLong( i );
        return bb.array( );
    }

    private String byteToString( byte[] b ){
        return new String( b, Charset.forName( "US-ASCII" ) );
    }

    private void doHandShake( ) throws IOException, ClassNotFoundException{
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
                                                         userSelf,
                                                         byteToString( pubkey ) ) );

//
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



    public void writeMessage( Message msg ) throws IOException{

        writeMessage_( msg, null );
    }

    public void writeMessage( Message msg, ObjectOutputStream oos ) throws IOException{

        writeMessage_( msg, oos );
    }


    synchronized private void writeMessage_( Message msg, ObjectOutputStream oos ) throws IOException{
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
            encrypted_msg_bytes = msg_bytes;
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
     * @return byte[]
     *
     * @throws IOException
     */
    private byte[] consumeAllBytesFromInputStream( ObjectInputStream iis ) throws IOException{
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


    public Message readMessage( ) throws IOException, ClassNotFoundException{
        return readMessage_( null );
    }

    public Message readMessage( ObjectInputStream iis ) throws IOException, ClassNotFoundException{
        return readMessage_( iis );
    }


    synchronized private Message readMessage_( ObjectInputStream iis ) throws IOException, ClassNotFoundException{


        byte[] encrypted_msg_bytes;
        byte[] decrypted_msg_bytes;


        encrypted_msg_bytes = consumeAllBytesFromInputStream( iis );

        //example of what call will look like when I decrypt using AES decryption alg using
        //shared key established in doHandShake
        //decrypted_msg_bytes = Encrypter.AESdecrypt(SHARED_KEY,encrypted_msg_bytes);

        //testing
        decrypted_msg_bytes = encrypted_msg_bytes;
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

//    public void updateStatus( Status status ) throws IOException{
//        writeMessage( new Message( MessageType.STATUS,
//                                   userSelf, null ).setStatus( status ) );
//
//    }

    public void closeConnection( ) throws IOException{

        writeMessage( new Message( MessageType.REMOVEUSER, userSelf, null ) );

//        if ( coordinator != null ){
//            //if coordinator closes connection then need to migrate coordinator
//            //or just kick everyone off
//
//        }
    }


    private class P2Pcoordinator implements Runnable
    {


        final ConcurrentMap<Long, Socket> host_connections;
        final AtomicBoolean continue_coordinating;
        final ConcurrentMap<Long, User> online_users;
        final User ControllerUser;
        final ArrayBlockingQueue<Message> outgoingMessages;
        final Long ID_ofCoordinator;


        Thread P2Plistener_thread;

        P2Pcoordinator( Long userID ) throws IOException{

            int queueCapacity = 100;
            outgoingMessages = new ArrayBlockingQueue<>( queueCapacity );

            ID_ofCoordinator = userID;
            host_connections = new ConcurrentHashMap<>( );
            online_users = new ConcurrentHashMap<>( );

            ControllerUser = new User( "P2PController", Status.CONTROLLER );

            continue_coordinating = new AtomicBoolean( true );


            P2Plistener_thread = new Thread( new P2Plistener( ) );
            P2Plistener_thread.setDaemon(true);
            P2Plistener_thread.start( );

        }

        void internalCloseConnection( Long threadID ){

            //if threadID is null then close all connections because
            //P2P coordinator is signing off (or something like that)
            if ( threadID == null || Objects.equals( threadID, ID_ofCoordinator ) ){
                host_connections.forEach( ( ID, socketConn ) -> {
                    try{
                        socketConn.close( );
                    } catch ( IOException e ){
                        Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                               "Socket close failed in P2Plistner" );


                    }


                } );
                host_connections.clear( );
                online_users.clear( );

            } else{
                try{
                    host_connections.get( threadID ).close( );
                } catch ( IOException e ){
                    Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                           "Socket close failed in P2Plistner" );

                }
                online_users.remove( threadID );
                host_connections.remove( threadID );
            }
        }


        @Override
        public void run( ){

            while ( continue_coordinating.get( ) ){

                Message msg;
                try{
                    msg = outgoingMessages.take( );
                } catch ( InterruptedException e ){
                    Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.WARNING,
                                                                           "Interrupted exception" +
                                                                           " in P2P coordinator run" );
                    continue;

                }


                //broadcast to everyone but sender
                final Message finalMsg = msg;
                host_connections.forEach( ( ID, socketConn ) -> {
                    if ( finalMsg != null ){
                        if ( online_users.get( ID ) != finalMsg.getUser( ) ){

                            try{
                                writeMessage( finalMsg, new ObjectOutputStream( socketConn.getOutputStream( ) ) );
                            } catch ( IOException e ){
                                Logger.getLogger( this.getClass( ).toString( ) ).log( Level.SEVERE,
                                                                                      "A broadcast message failed to send",
                                                                                      e );

                                //maybe kill the entire program here

                            }
                            if ( finalMsg.getType( ) == MessageType.REMOVEUSER ){
                                internalCloseConnection( ID );
                            }
                        }
                    } else{
                        Logger.getLogger( this.getClass( ).toString( ) ).log( Level.WARNING,
                                                                              "Message extracted from outgoing message" +
                                                                              "queue was null" );
                    }
                } );


            }
        }

        private class P2Plistener implements Runnable
        {

            final ServerSocket listening_sock;
            final ArrayList<Thread> threadList;

            final static int BACKLOG = 10;

            P2Plistener( ) throws IOException{

                try{
                    listening_sock = new ServerSocket( portNum, BACKLOG );
                    listening_sock.setReuseAddress(true);
                } catch ( IOException e ){
                    Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                           "Server Socket failed in P2Plistner" );
                    throw e;
                }

                threadList = new ArrayList<>( );
            }

            @Override
            public void run( ){
                Thread t;
                while ( continue_coordinating.get( ) ){
                    try{

                        t = new Thread( new P2Phandler( listening_sock.accept( ) ) );
                        t.setDaemon(true);
                        System.out.println("about to sleep");
                        Thread.sleep(500);
                        System.out.println("waking up now");
                        threadList.add( t );
                        t.start( );


                    } catch ( IOException e ){
                        Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                               "Server userSocket accept failed in P2Plistner" );

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }

            }
        }

        private class P2Phandler implements Runnable
        {
            final Socket connected_sock;
            Boolean handshakeDone = false;
            User connecteduser = null;
            boolean continueHandling = true;

            P2Phandler( Socket connected_sock ){
                System.out.println("inside handler ctor");
                this.connected_sock = connected_sock;
                host_connections.put( Thread.currentThread( ).getId( ), connected_sock );


            }


            @Override
            public void run( ){

                System.out.println("inside p2phandler running");
                try ( ObjectOutputStream oos = new ObjectOutputStream( connected_sock.getOutputStream( ) );
                ObjectInputStream iis = new ObjectInputStream( connected_sock.getInputStream( ) );
                      ){

                    Message msg;
                    while ( connected_sock.isConnected( ) && continueHandling ){

                        if ( !handshakeDone ){

                            //handshake message is first message sent upon connection
                            //and is not encrypted yet because encryption depends
                            //on handshake--so just use readObject to extract Message
                            //containing users public key, then encrypt the shared
                            //secret AES128 bit key via RSAencrypt then send back to userSelf;
                            //now all further communication will be encrypted via AES
                            msg = (Message) iis.readObject( );

                            assert msg.getType( ) == MessageType.HANDSHAKE ||
                                   msg.getType( ) == MessageType.SELFCONNECTION :
                                    "First sent message must be type HANDSHAKE or SELFCONNECTION";

                            connecteduser = msg.getUser( );

                            online_users.put( Thread.currentThread( ).getId( ), connecteduser );

                            //if the userSelf connecting to P2P coordinator is the
                            //userSelf assuming role of p2P coordinator then no need
                            //for remaining handshake operations to continue
                            if ( msg.getType( ) == MessageType.SELFCONNECTION ){
                                handshakeDone = true;
                                continue;
                            }



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

                            //decrypts message to take appropriate actions depending on
                            //message type then broadcasts message to all other users
                            msg = readMessage( iis );

                            switch ( msg.getType( ) ){


                                case STATUS:
                                    online_users.put( Thread.currentThread( ).getId( ),
                                                      connecteduser.setStatus( msg.getStatus( ) ) );
                                    break;

                                case REMOVEUSER:
                                    online_users.remove( Thread.currentThread( ).getId( ) );
                                    continueHandling = false;
                                    break;

                                case USER:

                                    break;

                            }


                            msg.setUserList( new ArrayList<>( online_users.values( ) ) );
                            outgoingMessages.put( msg );

                        }


                    }

                } catch ( IOException | ClassNotFoundException | InterruptedException e ){
                    Logger.getLogger( this.getClass( ).toString( ) ).log( Level.SEVERE,
                                                                          "Stream created failed in P2Phandler", e );

                }

                if ( continueHandling ){
                    //if continueHandling true then socket closed unexpectedly
                    //internalCloseConnection( Thread.currentThread( ).getId( ) );

                    try{
                        outgoingMessages.put( new Message( MessageType.REMOVEUSER, connecteduser, null ) );
                    } catch ( InterruptedException e ){
                        Logger.getLogger( this.getClass( ).toString( ) ).log( Level.WARNING,
                                                                              "Interrupted exception in handler",
                                                                              e );
                    }


                }


            }
        }


    }

}

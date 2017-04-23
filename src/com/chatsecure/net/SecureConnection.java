package com.chatsecure.net;

import com.chatsecure.aes.CTR;
import com.chatsecure.client.Message;
import com.chatsecure.client.MessageType;
import com.chatsecure.client.Status;
import com.chatsecure.client.User;
import com.chatsecure.rsa.RSAEncryption;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
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
    private OutputStream stream_to_P2Pcoord;
    private InputStream stream_from_P2Pcoord;
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

        initalize( InetAddress.getLocalHost( ).getHostAddress( ), new_user, port_num, true );


    }


    /**
     * @param host_addr            IP address of P2P coordinator as string
     * @param port_num             port number for SecureChat app
     * @param becomeP2Pcoordinator flag to indicate if you are the sole P2P coordinator for a chat room
     */
    public void initalize( String host_addr, User new_user, int port_num,
                           boolean becomeP2Pcoordinator ) throws
            IOException, ClassNotFoundException{

        //if userSocket is not null then SecureConnection already initialized so just return
        if ( userSocket != null ){
            return;
        }

        userSelf = new_user;
        hostAddr = host_addr;
        portNum = port_num;

        ByteArrayOutputStream byte_stream_in = new ByteArrayOutputStream( );
        ObjectOutputStream to_byte_stream = new ObjectOutputStream( byte_stream_in );

        byte[] msg_bytes;


        if ( becomeP2Pcoordinator ){
            //generate random 128-bit key for AES128 that will be shared with all clients using RSA and
            SecureRandom random = new SecureRandom( );

            random.nextBytes( SHARED_KEY );

            Long myID = Thread.currentThread( ).getId( );
            coordinator = new Thread( new P2Pcoordinator( myID ) );
            coordinator.setDaemon( true );
            coordinator.start( );


            userSocket = new Socket( hostAddr, portNum );
            //start coordinator then connect where hostAddr is localAddr so
            //coordinator will handle connection same way it would handle
            //connection from remote host.

            stream_to_P2Pcoord = userSocket.getOutputStream( );
            stream_from_P2Pcoord = userSocket.getInputStream( );



            Message m = new Message( MessageType.SELFCONNECTION,
                                     userSelf, null );

            to_byte_stream.writeObject( m );
            msg_bytes = byte_stream_in.toByteArray( );

            System.out.println( "INIT MSG BYTES LENGTH: " + msg_bytes.length );

            stream_to_P2Pcoord.write( msg_bytes, 0, msg_bytes.length );



        } else{
            userSocket = new Socket( hostAddr, portNum );
            stream_to_P2Pcoord = userSocket.getOutputStream( );

            stream_from_P2Pcoord = userSocket.getInputStream( );

            try{
                doHandShake( );
            } catch ( IOException | ClassNotFoundException e ){
                Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE,
                                                                            "Error Initialize:  doHandShake()", e );
                throw e;
            }
        }


        initialized.set( true );


    }

    public boolean isConnected( ){
        return userSocket.isConnected( );
    }

    public SecureConnection waitForInitialization( ){


        while ( !initialized.get( ) ){

            //busy wait if not yet initialized--will be quick
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

        byte[] shared_secret;

        RSAEncryption RSAenc = new RSAEncryption( );



        try{

            //sending public key to P2P coordinator; so this is the first thing com.chatsecure.client sends to
            //p2p coordinator after connecting in initialize
            ByteArrayOutputStream byte_stream_in = new ByteArrayOutputStream( );
            ObjectOutputStream to_byte_stream = new ObjectOutputStream( byte_stream_in );

            byte[] msg_bytes;
            to_byte_stream.writeObject( new Message( MessageType.HANDSHAKE,
                                                     userSelf,
                                                     null )
                                                .setPublicKey_exponent(
                                                        RSAenc.getPublicKey( ).get( "exp" ) )
                                                .setPublicKey_moduls(
                                                        RSAenc.getPublicKey( ).get( "mod" ) ) );
            msg_bytes = byte_stream_in.toByteArray( );

            stream_to_P2Pcoord.write( msg_bytes, 0, msg_bytes.length );

            int num;
            byte[] in_buff = new byte[ 8192 * 4 ];
//            byte[] in_msg_bytes = new byte[ 8192 ];
//
//            int total=0;
//            while ((num = stream_from_P2Pcoord.read( in_buff, 0, 8192 ))>0){
//
//                for ( int i = total; i < (total+in_buff.length); i++ ){
//                    in_msg_bytes[ i ] = in_buff[ i ];
//                }
//                total += num;
//            }

            num = stream_from_P2Pcoord.read( in_buff, 0, in_buff.length );
            Message returnMsg;

            ByteArrayInputStream byte_stream_out = new ByteArrayInputStream( in_buff );
            ObjectInputStream from_byte_stream = new ObjectInputStream( byte_stream_out );

            returnMsg = (Message) from_byte_stream.readObject( );


            assert returnMsg.getType( ) ==
                   MessageType.HANDSHAKE : "doHandShake got back wrong message type from P2P coordinator";

            //here the com.chatsecure.client is using his private RSA key to decrypt this message--that key should exist
            //within RSA object


            shared_secret = RSAenc.decrypt( returnMsg.getRSAresult( ) );

            System.out.println( "RSA COMPUTER SHARED KEY: " + Arrays.toString( shared_secret ) );
            

//            testing
//            shared_secret = returnMsg.getContent( ).getBytes( );
//            testing


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

        writeMessage_( msg, stream_to_P2Pcoord );
    }

    private void writeMessage( Message msg, OutputStream oos ) throws IOException{

        writeMessage_( msg, oos );
    }


    private void writeMessage_( Message msg, OutputStream oos ) throws IOException{
        byte[] msg_bytes;
        byte[] encrypted_msg_bytes = null;

        try ( ByteArrayOutputStream byte_stream = new ByteArrayOutputStream( );
              ObjectOutputStream to_byte_stream = new ObjectOutputStream( byte_stream ) ){

            to_byte_stream.writeObject( msg );
            msg_bytes = byte_stream.toByteArray( );


            try{
                CTR.setkey( SHARED_KEY );
                encrypted_msg_bytes = CTR.encryptMessage( msg_bytes );
            } catch ( Exception e ){
                // TODO Auto-generated catch block
                e.printStackTrace( );
            }





            assert encrypted_msg_bytes != null : "CTR.encryptMessage returned null";

            oos.write( encrypted_msg_bytes );

        } catch ( IOException e ){
            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE, "Error in WriteMessage()", e );
            throw e;
        }
    }


    public Message readMessage( ) throws IOException, ClassNotFoundException{
        return readMessage_( stream_from_P2Pcoord );
    }

    private Message readMessage( InputStream iis ) throws IOException, ClassNotFoundException{
        return readMessage_( iis );
    }


    private Message readMessage_( InputStream iis ) throws IOException, ClassNotFoundException{

        byte[] decrypted_msg_bytes = null;


        byte[] received_msg_bytes = new byte[ 8192 ];
        int num = iis.read( received_msg_bytes, 0, 8192 );
        if ( num == -1 ){
            return null;
        }
        byte[] encrypted_msg_bytes = new byte[ num ];
        System.arraycopy( received_msg_bytes, 0, encrypted_msg_bytes, 0, num );

        try{
            CTR.setkey( SHARED_KEY );
            decrypted_msg_bytes = CTR.decryptMessage( encrypted_msg_bytes );
        } catch ( Exception e1 ){
            // TODO Auto-generated catch block
            e1.printStackTrace( );
        }




        assert decrypted_msg_bytes != null : "CTR.decrypt returned null";


        ByteArrayInputStream byte_stream_out = new ByteArrayInputStream( decrypted_msg_bytes );
        ObjectInputStream from_byte_stream = new ObjectInputStream( byte_stream_out );

        return (Message) from_byte_stream.readObject( );


    }


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
            P2Plistener_thread.setDaemon( true );
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

                    online_users.remove( threadID );
                    host_connections.remove( threadID ).close( );
                } catch ( IOException e ){
                    Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                           "Socket close failed in P2Plistner" );

                }

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

                // !online_users.get( ID ).getName().equals( finalMsg.getUser( ).getName() )

                //broadcast to everyone
                final Message finalMsg = msg;
                System.out.println( "INSIDE BROADCAST--MSG: " + msg );
                host_connections.forEach( ( ID, socketConn ) -> {
                    if ( finalMsg != null ){


                        try{
                            writeMessage( finalMsg, socketConn.getOutputStream( ) );
                        } catch ( IOException e ){
                            Logger.getLogger( this.getClass( ).toString( ) ).log( Level.SEVERE,
                                                                                  "A broadcast message failed to send",
                                                                                  e );

                            //don't kill entire program here because if socket write
                            //fails it may just be that users socket closed unexpectedly
                            //so just ignore fail and broadcast to everyone else

                        }
                        if ( finalMsg.getType( ) == MessageType.REMOVEUSER ){
                            internalCloseConnection( ID );
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
                    listening_sock.setReuseAddress( true );
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
                        t.setDaemon( true );


                        threadList.add( t );
                        t.start( );


                    } catch ( IOException e ){
                        Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                                                               "Server userSocket accept failed in P2Plistner" );

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
                System.out.println( "inside handler ctor" );
                this.connected_sock = connected_sock;


            }


            @Override
            public void run( ){


                try ( OutputStream oos = connected_sock.getOutputStream( );
                      InputStream iis = connected_sock.getInputStream( ) ){

                    Message msg;
                    while ( connected_sock.isConnected( ) && continueHandling ){

                        if ( !handshakeDone ){

                            //handshake message is first message sent upon connection
                            //and is not encrypted yet because encryption depends
                            //on handshake--so just use readObject to extract Message
                            //containing users public key, then encrypt the shared
                            //secret AES128 bit key via RSAencrypt then send back to userSelf;
                            //now all further communication will be encrypted via AES

                            byte[] in_buff = new byte[ 8192 ];
                            int num;

//                            byte[] in_msg_bytes = new byte[ 8192*10 ];
//
//                            int total = 0;
//                            while ( ( num = iis.read( in_buff, 0, 8192 ) ) > 0 ){
//
//                                System.arraycopy( in_buff, 0, in_msg_bytes, 0 + total, in_buff.length );
//                                total += num;
//                            }

                            num = iis.read( in_buff, 0, in_buff.length );

                            ByteArrayInputStream byte_stream_out = new ByteArrayInputStream( in_buff );
                            ObjectInputStream from_byte_stream = new ObjectInputStream( byte_stream_out );

                            msg = (Message) from_byte_stream.readObject( );

                            System.out.println( "TESTING IN HANDLER HANDSHAKE AREA: " + msg );


                            assert msg.getType( ) == MessageType.HANDSHAKE ||
                                   msg.getType( ) == MessageType.SELFCONNECTION :
                                    "First sent message must be type HANDSHAKE or SELFCONNECTION";

                            connecteduser = msg.getUser( );

                            online_users.put( Thread.currentThread( ).getId( ), connecteduser );
                            host_connections.put( Thread.currentThread( ).getId( ), connected_sock );

                            //if the userSelf connecting to P2P coordinator is the
                            //userSelf assuming role of p2P coordinator then no need
                            //for remaining handshake operations to continue
                            if ( msg.getType( ) == MessageType.SELFCONNECTION ){
                                handshakeDone = true;
                                continue;
                            }


                            // byte[] usersPubkey = msg.getContent( ).getBytes( );

                            BigInteger encryptedSharedSecret;


                            encryptedSharedSecret = RSAEncryption.encrypt( msg.getPublicKey_moduls( ),
                                                                           msg.getPublicKey_exponent( ),
                                                                           new BigInteger( SHARED_KEY ) );


                            ByteArrayOutputStream byte_stream_in = new ByteArrayOutputStream( );
                            ObjectOutputStream to_byte_stream = new ObjectOutputStream( byte_stream_in );

                            byte[] msg_bytes;
                            to_byte_stream.writeObject( new Message( MessageType.HANDSHAKE,
                                                                     ControllerUser,
                                                                     ( null ) ).setRSAresult( encryptedSharedSecret ) );
                            msg_bytes = byte_stream_in.toByteArray( );

                            System.out.println( "INSIDE p2PHANDLER SIZE OF RSA MSG: " + msg_bytes.length );

                            oos.write( msg_bytes, 0, msg_bytes.length );


                            outgoingMessages.put( msg.setType( MessageType.ADDUSER )
                                                          .setUserList( new ArrayList<>( online_users.values( ) ) ) );


                            handshakeDone = true;

                        } else{

                            //decrypts message to take appropriate actions depending on
                            //message type then broadcasts message to all other users

                            msg = readMessage( iis );


                            if ( msg == null ){
                                Logger.getLogger( this.getClass( ).toString( ) ).log( Level.WARNING,
                                                                                      "P2Phandler--readMessage returned null" );
                                continue;
                            }

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

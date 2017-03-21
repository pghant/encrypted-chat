package com.chatsecure.net;

import com.chatsecure.client.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
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
public class SecureConnection{

    private static P2Pcoordinator coordinator = null;
    private static String hostAddr;
    private static Integer portNum;
    private static Socket socket = null;
    private static ObjectOutputStream out_stream;
    private static ObjectInputStream in_stream;
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
     * @param host_addr            address of P2P coordinator or localhost if you are P2P coordinator
     * @param port_num             port number for SecureChat app
     * @param becomeP2Pcoordinator flag to indicate if you are the sole P2P coordinator for a chat room
     */
    public static void initalize( String host_addr, int port_num, boolean becomeP2Pcoordinator ) throws IOException{

        //if socket is not null then SecureConnection already initialized so just return
        if ( socket != null ){
            return;
        }

        hostAddr = host_addr;
        portNum = port_num;


        socket = new Socket( hostAddr, portNum );
        in_stream = new ObjectInputStream( socket.getInputStream( ) );
        out_stream = new ObjectOutputStream( socket.getOutputStream( ) );


        if ( becomeP2Pcoordinator ){
            //generate random 128-bit key for AES128 that will be shared with all clients using RSA and
            SecureRandom random = new SecureRandom( );

            random.nextBytes( SHARED_KEY );

            coordinator = new P2Pcoordinator( );

        } else{

            doHandShake( );
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

    public static void doHandShake( ) throws IOException{
        byte[] pubkey;
        byte[] encrypted_shared_secret;
        byte[] shared_secret;

        //EXAMPLE of what call will look like
        //pubkey=RSA.get_public_key()

        //testing
        pubkey = "PUBLIC_KEY_VALUE".getBytes( );
        //testing


        try{

            //sending public key to P2P coordinator; so this is the first thing com.chatsecure.client sends to
            //p2p coordinator after connecting in initialize
            out_stream.write( pubkey );

        } catch ( IOException e ){
            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE, "Error in doHandShake()", e );
        }


        //P2Pcoordinator encrypts shared secret and sends it back here;
        //consumeAllBytesFromInputStream is going to read in the encrypted share secret


        encrypted_shared_secret = consumeAllBytesFromInputStream( );

        //here the com.chatsecure.client is using his private RSA key to decrypt this message--that key should exist
        //within RSA object

        //shared_secret=RSA.decryptSharedSecret(encrypted_shared_secret)

        //testing
        shared_secret = "DECRYPTED_SHARED_SECRET".getBytes( );
        //testing


        SHARED_KEY = shared_secret;

    }

    public static void writeMessage( Message msg ) throws IOException{
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


            out_stream.write( encrypted_msg_bytes );

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
    private static byte[] consumeAllBytesFromInputStream( ) throws IOException{
        ArrayList<Byte> byte_array = new ArrayList<>( );

        do{
            try{
                byte_array.add( in_stream.readByte( ) );
            } catch ( IOException e ){
                Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE, "Socket Read Error in ReadMessage()", e );
                throw e;
            }

        } while ( in_stream.available( ) > 0 );

        byte[] msg_bytes = new byte[ byte_array.size( ) ];


        //unboxing bytes from Bytes
        int idx = 0;
        for ( Byte b : byte_array ){
            msg_bytes[ idx++ ] = b;
        }

        return msg_bytes;

    }


    public static Message readMessage( ) throws IOException, ClassNotFoundException{


        byte[] encrypted_msg_bytes = consumeAllBytesFromInputStream( );
        byte[] decrypted_msg_bytes;


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
            Logger.getLogger( SecureConnection.class.toString( ) ).log( Level.SEVERE, "Deserializing Error in ReadMessage()", e );
            throw e;
        }


        return new_msg;
    }


    private static class P2Pcoordinator{

        //maps com.chatsecure.client ID to associated socket
        final ConcurrentMap<Long, Socket> host_connections = new ConcurrentHashMap<>( );
        final AtomicBoolean continue_coordinating = new AtomicBoolean( true );

        Thread P2Plistener_thread;

        P2Pcoordinator( ) throws IOException{
            P2Plistener_thread = new Thread(new P2Plistener());
            P2Plistener_thread.start();

        }

        void closeConnection( Long threadID ){

            host_connections.forEach( ( ID, socket1 ) -> {

                if ( ID.equals( threadID ) || (threadID== null)  ){
                    try{
                        socket1.close( );
                    } catch ( IOException e ){
                        Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                "Socket close failed in P2Plistner" );


                    }
                }


            } );

            if ( threadID == null){

                host_connections.clear();

            }else {
                host_connections.remove( threadID );
            }
        }

        private class P2Plistener implements Runnable{

            final ServerSocket listening_sock;
            final ArrayList<Thread> threads = new ArrayList<>( );
            final static int BACKLOG = 10;

            public P2Plistener( ) throws IOException{

                try{
                    listening_sock = new ServerSocket( SecureConnection.portNum, BACKLOG );
                } catch ( IOException e ){
                    Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                            "Server Socket failed in P2Plistner" );
                    throw e;
                }

            }

            @Override
            public void run( ){
                Thread t;
                while ( true ){
                    try{

                        t = new Thread( new P2Phandler( listening_sock.accept( ) ) );
                        t.start( );
                        threads.add( t );

                    } catch ( IOException e ){
                        Logger.getLogger( P2Plistener.class.toString( ) ).log( Level.SEVERE,
                                "Server socket accept failed in P2Plistner" );

                        continue_coordinating.set( false );
                        break;

                    }


                }

                P2Pcoordinator.this.closeConnection( null );


            }
        }

        private class P2Phandler implements Runnable{
            final Socket connected_sock;

            public P2Phandler( Socket connected_sock ){
                this.connected_sock = connected_sock;
                host_connections.put( Thread.currentThread().getId(),connected_sock );
            }

            @Override
            public void run( ){

                try( ObjectInputStream iis = new ObjectInputStream( connected_sock.getInputStream( ) );
                     ObjectOutputStream oos = new ObjectOutputStream( connected_sock.getOutputStream( ) )){

                    while ( connected_sock.isConnected( ) ){





                    }

                } catch ( IOException e ){
                    Logger.getLogger( this.getClass( ).toString( ) ).log( Level.SEVERE,
                            "Stream created failed in P2Phandler", e );
                }finally{

                }






            }
        }


    }

}

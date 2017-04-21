package com.chatsecure.client;


import com.chatsecure.net.SecureConnection;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

public class ChatController{


    @FXML private Label user_count;
    @FXML private TextField msgTextArea;
    @FXML private BorderPane borderpane_root;
    @FXML private VBox vbox_center;
    @FXML
    private ListView<Message> chatDisplay;
    @FXML private ScrollPane scrollpane_center;
    @FXML private HBox hbox_bottom;
    @FXML private AnchorPane anchorpane_bottom;
    @FXML private Button clearMsgBtn;
    @FXML private Button sendMsgBtn;
    @FXML private ListView<User> userDisplay;
    @FXML private VBox vbox_right;
    @FXML private Label onlineUsersLabel;

    private final ObservableList<Message> messages = FXCollections.observableArrayList(  );

    private final ObservableList<User> onlineUsers = FXCollections.observableArrayList( );


    public ChatController( ){

    }

    @FXML public void initialize(){


        VBox.setVgrow( vbox_center, Priority.ALWAYS );
        VBox.setVgrow( vbox_right, Priority.ALWAYS );
        HBox.setHgrow( hbox_bottom, Priority.ALWAYS );


        onlineUsers.addListener( (ListChangeListener<? super User>) change -> {
            while ( change.next( ) ){
                //noinspection StatementWithEmptyBody
                if ( change.wasReplaced( ) ){

                    //don't think I really need to do anything here
                } else if ( change.wasRemoved( ) ){

                    user_count.setText( String.valueOf( Integer.valueOf( user_count.getText( ) )
                                                        - change.getRemovedSize( ) ) );
                } else if ( change.wasAdded( ) ){

                    user_count.setText( String.valueOf( Integer.valueOf( user_count.getText( ) )
                                                        + change.getAddedSize( ) ) );

                }
            }
        } );


        chatDisplay.setItems( messages );

        chatDisplay.setCellFactory( list -> new MessageListCell( ) );

        userDisplay.setItems( onlineUsers );
        userDisplay.setCellFactory( list -> new UserListCell( ) );


        vbox_center.widthProperty().addListener( ( observable, oldValue, newValue ) ->
                                                         chatDisplay.setPrefWidth( (double)newValue ));

        vbox_center.heightProperty( ).addListener( ( observable, oldValue, newValue ) ->
                                                           chatDisplay.setPrefHeight( (double) newValue ) );

        vbox_right.widthProperty( ).addListener( ( observable, oldValue, newValue ) ->
                                                         userDisplay.setPrefWidth( (double) newValue ) );

        vbox_right.heightProperty( ).addListener( ( observable, oldValue, newValue ) ->
                                                          userDisplay.setPrefHeight( (double) newValue ) );

        hbox_bottom.widthProperty().addListener( ( observable, oldValue, newValue ) ->
                                                         msgTextArea.setPrefWidth( (double)newValue ));

        hbox_bottom.heightProperty().addListener( ( observableValue, number, t1 ) -> {
            msgTextArea.setPrefHeight( (double)t1 );
        } );


        msgTextArea.addEventFilter( KeyEvent.KEY_PRESSED, ke -> {
            if ( ke.getCode( ).equals( KeyCode.ENTER ) ){
                try{
                    sendButtonAction( );
                } catch ( IOException e ){
                    e.printStackTrace( );
                }
                ke.consume( );
            }
        } );


    }

    private void sendButtonAction( ) throws IOException{
        Message msg = new Message( MessageType.USER, null, null );
        SecureConnection.getConnection( ).waitForInitialization( ).writeMessage( msg );


    }

    public void signSelfOff( ){
        try{
            SecureConnection.getConnection( ).waitForInitialization( ).closeConnection( );
        } catch ( IOException e ){
            System.exit( 0 );
        }
    }

    public void addNewUser( User user ){
        onlineUsers.add( user );
    }

    public void removeUser( User user ){
        onlineUsers.remove( user );


    }

    public void updateUserStatus( User user ){
        onlineUsers.replaceAll( userArg -> {
            if ( userArg.equals( user ) ){
                return userArg.setStatus( user.getStatus( ) );
            } else{
                return userArg;
            }

        } );
    }


    public void sendMethod( KeyEvent event ) throws IOException{
        if ( event.getCode( ) == KeyCode.ENTER ){
            sendButtonAction( );
        }
    }


    static class MessageListCell extends ListCell<Message>
    {
        @Override
        protected void updateItem( final Message item, final boolean empty ){
            super.updateItem( item, empty );
            Rectangle rect = new Rectangle( 100, 20 );
            if ( item != null ){
                rect.setFill( Color.web( item.toString( ) ) );
                setGraphic( rect );
            }
        }


    }


    static class UserListCell extends ListCell<User>
    {
        @Override
        protected void updateItem( final User item, final boolean empty ){
            super.updateItem( item, empty );
            Rectangle rect = new Rectangle( 100, 20 );
            if ( item != null ){
                rect.setFill( Color.web( item.toString( ) ) );
                setGraphic( rect );
            }
        }


    }





}

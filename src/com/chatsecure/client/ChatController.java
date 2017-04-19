package com.chatsecure.client;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ChatController{


    @FXML private Label user_count;
    @FXML private TextField msgTextArea;
    @FXML private BorderPane borderpane_root;
    @FXML private VBox vbox_center;
    @FXML private ListView<Label> chatDisplay;
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

    @FXML public void initialize(){


        VBox.setVgrow( vbox_center, Priority.ALWAYS );
        VBox.setVgrow( vbox_right,Priority.ALWAYS );

        HBox.setHgrow( hbox_bottom,Priority.ALWAYS );




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













    }




}

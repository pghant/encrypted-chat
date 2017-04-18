package com.chatsecure.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class GUI extends Application {






    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource( "resources/views/chat_window.fxml" ));

        BorderPane actualRoot = (BorderPane)root;



        Scene scene = new Scene( root);



        primaryStage.setTitle("SecureChat");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

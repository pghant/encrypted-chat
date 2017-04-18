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


        //TESTING out some random stuff

        // Use Java Collections to create the List.
//        Map<String, String> map = new HashMap<String, String>( );
//
//        map.put( "preKey1", "preVal1" );
//        map.put( "preKey2", "preVal2" );
//
//
//        // Now add observability by wrapping it with ObservableList.
//        ObservableMap<String, String> observableMap = FXCollections.observableMap( map );
//        observableMap.addListener( (MapChangeListener) change ->
//        {
//            System.out.println( "inside Map handler: " + change.wasAdded( ) + ",  " + change.getValueAdded( ) );
//        } );
//
//        // Changes to the observableMap WILL be reported.
//        observableMap.put( "key 1", "value 1" );
//        System.out.println( "Size: " + observableMap.size( ) );
//
//        observableMap.put( "key 3", "value 3" );
//        System.out.println( "Size: " + observableMap.size( ) );
//
//
//        observableMap.put( "key 3", "value 66" );
//        System.out.println( "Size: " + observableMap.size( ) );
//
//
//        // Changes to the underlying map will NOT be reported.
//        map.put( "key 2", "value 2" );
//        System.out.println( "Size: " + observableMap.size( ) );
//
//
//        List<String> list = new ArrayList<String>( );
//        list.add( "pre1" );
//        list.add( "pre2" );
//
//        // Now add observability by wrapping it with ObservableList.
//        ObservableList<String> observableList = FXCollections.observableList( list );
//        observableList.addListener( (ListChangeListener) change -> {
//            int k = 0;
//            while ( change.next( ) ){
//
//                System.out.println( "inside list handler " + ( k++ ) + ": " + change.getList( ) );
//            }
//        } );
//
//        // Changes to the observableList WILL be reported.
//        // This line will print out "Detected a change!"
//        observableList.add( "item one" );
//        observableList.add( "item TWO" );
//        observableList.add( "item THREEE" );
//
//        // Changes to the underlying list will NOT be reported
//        // Nothing will be printed as a result of the next line.
//        list.add( "item two" );
//
//        System.out.println( "Size: " + observableList.size( ) );



        primaryStage.setTitle("SecureChat");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

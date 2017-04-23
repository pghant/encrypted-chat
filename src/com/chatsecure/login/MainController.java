package com.chatsecure.login;


import java.io.IOException;

import com.chatsecure.client.ChatController;
import com.chatsecure.client.MessageReceiver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * @author sriramvaradharajan
 *
 */
 
public class MainController {

@FXML
private Label lblStatus;

@FXML
private  TextField txtUserName;

@FXML
private TextField txtPassword;

@FXML
private CheckBox cboxp2pcoordinator;

@FXML
private TextField hostAddress;

@FXML
private Button btnSignIn;

public void signin(ActionEvent event){
	if(txtUserName.getText().equals("user") && txtPassword.getText().equals("password")){
		lblStatus.setText("Login success");
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chatsecure/client/resources/views/chat_window.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			Stage stage = (Stage) btnSignIn.getScene().getWindow();
		    stage.setScene(scene);
		    
		    try {
		    	if(cboxp2pcoordinator.isSelected()){
		    		new MessageReceiver( 5320, loader.getController(),"user");
		    	}else{
		    		new MessageReceiver("localhost", 0, loader.getController(),"user");
		    	}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		    stage.show(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}else{
		lblStatus.setText("Login Failed");
	}
}
public void checkp2p(MouseEvent event){
    	if(cboxp2pcoordinator.isSelected()){
    		hostAddress.setVisible(false);
    	}else{
    		hostAddress.setVisible(true);
    	}

	
}
}

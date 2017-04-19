package com.chatsecure.login;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

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

public void signin(ActionEvent event){
	if(txtUserName.getText().equals("user") && txtPassword.getText().equals("password")){
		lblStatus.setText("Login success");
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

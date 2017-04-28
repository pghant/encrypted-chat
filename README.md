# encrypted-chat

## Description
This project allows users to communicate directly and securely with each other over local and wide area networks. It doesn’t use a central server to store and maintain chat state, but works completely peer-to-peer. A coordinator first joins, and other users can join the chat by entering the IP of the coordinator. The coordinator will then distribute new messages from connected clients to everyone else in the chat directly.

All communication is protected by AES-128 encryption in CTR mode with a randomly generated shared key for each chat session. The initial key exchange is done using RSA public-key cryptography. Every message sent is authenticated with a SHA-512 hash. This ensures the security and integrity of the messages being sent.

The program was developed in Java to ensure maximum compatibility among different platforms. The UI was created using JavaFX. All security algorithms were implemented from scratch.

## How to Run
1. Run the command `java -jar encrypted_chat/encrypted_chat.jar`. Alternatively, the code can be opened in a Java IDE and can be compiled and run with the com.chatsecure.login.Main.java as the starting class.

2. On the login page, login with any of the following user names: jpc, ua, pg, ca, or sv. The password is “password”. Choose to be the P2P coordinator. Note the IP address of the coordinator.

3. Start a new instance of the app and login with any of the above logins. Choose to connect to the IP of the coordinator.

4. Both chat clients should now be open to the same chat view. Messages typed in either will appear on both.

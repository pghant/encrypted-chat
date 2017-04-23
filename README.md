# encrypted-chat

## Description
This project allows users to communicate directly and securely with each other over local and wide area networks. It doesn’t use a central server to store and maintain chat state, but works completely peer-to-peer. A coordinator first joins, and other users can join the chat by entering the IP of the coordinator. The coordinator will then distribute new messages from connected clients to everyone else in the chat directly.

All communication is protected by AES-128 encryption in CTR mode with a randomly generated shared key for each chat session. The initial key exchange is done using RSA public-key cryptography. Every message sent is authenticated with a SHA-512 hash. This ensures the security and integrity of the messages being sent.

The program was developed in Java to ensure maximum compatibility among different platforms. The UI was created using JavaFX. All security algorithms were implemented from scratch.

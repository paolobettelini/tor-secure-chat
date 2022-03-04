# Tor Secure Chat

This project implements a chat over the Tor network without having to trust the server.

NOTE:
ALWAYS check the safety emojis before trusting somebody

## Registration

Registration given username and password

Generate key pair
<br>
Send username
<br>
Send publicKey
<br>
Send private key encrypted with password
<br>
Send hashed password

## Login

Login given username and password

Send username
<br>
Send hashed password
<br>
Receive key pair
<br>
Check key pair
<br>
Receive nonce to sign
<br>
Send signed nonce
<br>
Receive unread messages

## Message

Send message given receiver and message

Generate random secure password
<br>
Send message encrypted with random password
<br>
Retrieve receiver public key
<br>
Send random password encrypted with receiver's public key
<br>
Send the signature of the encrypted message

## Compute fingerprint

Compute emoji fingerprint given sender public key and receiver public key

Compute `XOR(SHA256(pubKey1), SHA256(pubKey2))`
<br>
Divide in 4 groups of bytes `v`
<br>
The emoji is given by (((v XOR v >>> 1) << 1) >>> 1) % LENGTH(emojis)


<!--
AES/CBC(left128(SHA256(password)), right128(SHA256(password)), privateKey)
SHA256(SHA256(password))

Problem 0
General security
Solution
Messages must not be persistent
Messages must be stored by the server only if the receiver is offline

Problem 1
The server knows the ip of the interlocutors
Solution:
Send everything through the Tor network

Problem 2
The server can decrypt messages using the privateKey
Solution:
Simmetrically encrypt the privateKey with the password
Problem 2.1
The server can decrypt the privateKey using the password
Solution:
Instead of sending the password, send the hash

Problem 3
Two identical password result in the same hash
Solution:
Salt the password with the username

Problem 4
Man-in-the-middle: the server could generate a key pair,
send his public key instead of request one and gain control over every message sent
Solution:
Out-of-band verification
Sender and receiver must bith compute a value using their publicKey and the other end's publicKey
such that f(publicKeyA, publicKeyB) = f(publicKeyB, publicKeyA)
This value is then converted into something readable (e.g. 4 emojis)
The interlocutors must then verify that they are the same
A user must must check if the keypair is valid

Problem 5
The exit node can sniff traffic data (e.g. sniff hash(password)):
- Can login with someone's elses account
    - Delete the user unread messages
    - Send messages pretending to be the user
    (can't read incoming messages, therefore deleting them from the server)
Solution:
The server must send the user some random data to sign to prove that he has the private key

Problem 6
The server can send messages pretending to be somebody
Solution
Every message sent must be signed with the privateKey (and then verified by the receiver)
-->
package com.shopup;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.TreeMap;

import org.zeromq.SocketType;

public class ServerNode {
    private final String serverAddress;
    private final ZContext context;
    private final ZMQ.Socket socket;
    private final ZMQ.Socket outgoingSocket;
    private final ConsistentHashing consistentHashing;
    private TreeMap<Integer,String> ring;
    private String nextNodeAddress;

    private final Utils utils = new Utils();
    
    public ServerNode(String serverAddress, String brokerAddress) {
        this.serverAddress = serverAddress;
        this.context = new ZContext();
        this.socket = context.createSocket(SocketType.REP);
        this.outgoingSocket = context.createSocket(SocketType.REQ);
        this.socket.bind(serverAddress);
        this.outgoingSocket.connect(brokerAddress);
        this.ring = null;
        try {
            this.consistentHashing = new ConsistentHashing(1);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        joinRing();
        listenForMessages();
    }

    private void joinRing() {
        ZMsg msg = new ZMsg();
        msg.addString("JOIN");
        msg.addString(serverAddress);
        msg.send(outgoingSocket);

        ZMsg response = ZMsg.recvMsg(outgoingSocket);
        if (response != null) {
            String ringState = response.popString();
            this.ring = utils.stringToTreeMap(ringState);
            System.out.println("Received ring state: " + this.ring);
            this.nextNodeAddress = this.consistentHashing.getServerAfter(this.serverAddress, ring, true);
            //send message to next node saying hello from serverAddress
            if(this.nextNodeAddress != null){
                ZMQ.Socket msgSocket = context.createSocket(SocketType.REQ);
                msgSocket.connect(this.nextNodeAddress);
                ZMsg serverMsg = new ZMsg();
                serverMsg.addString("Hello from " + serverAddress);
                serverMsg.send(msgSocket);
            }
            else{
                System.out.println("Only node in the ring, message not sent.");
            }
        }
    }

    private void listenForMessages() {
        //! ISTO DEVE SER UMA STATE MACHINE ACHO EU
        System.out.println("Ready to read messages.");
        while (!Thread.currentThread().isInterrupted()) {
            ZMsg request = ZMsg.recvMsg(socket);
            if (request != null) {
                String message = request.popString();
                System.out.println("Received message: " + message);
            }
        }
    }

    public static void main(String[] args) {
    String serverAddress;
    String brokerAddress = "tcp://127.0.0.1:5000";

    if (args.length > 0) {
        serverAddress = args[0];
    } else {
        System.out.println("Enter a valid server address (e.g., tcp://127.0.0.2:5000): ");
        Scanner scanner = new Scanner(System.in);
        serverAddress = scanner.nextLine();
        scanner.close();
    }

    ServerNode node = new ServerNode(serverAddress, brokerAddress);
    node.start();
}
}

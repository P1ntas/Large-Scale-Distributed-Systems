package com.shopup;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Poller;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;

public class ServerNode {
    private final String serverAddress;
    private final ZContext context;
    private final ZMQ.Socket socket;
    private final ZMQ.Socket pullSocket;
    private final ZMQ.Socket outgoingSocket;
    private final ZMQ.Socket pushSocket;
    private final ConsistentHashing consistentHashing;
    private TreeMap<Integer,String> ring;
    private final String brokerAddress;
    private final ExecutorService executorService;

    private final Utils utils = new Utils();
    
    public ServerNode(String serverAddress, String brokerAddress) {
        this.serverAddress = serverAddress;
        this.brokerAddress = brokerAddress;

        this.context = new ZContext();

        this.socket = context.createSocket(SocketType.REP);
        this.socket.bind(serverAddress);
        this.pullSocket = context.createSocket(SocketType.PULL);
        this.pullSocket.bind(serverAddress.substring(0, serverAddress.length() - 1) + "1");// port + 1 ()
        this.outgoingSocket = context.createSocket(SocketType.REQ);
        this.outgoingSocket.connect(brokerAddress);
        this.pushSocket = context.createSocket(SocketType.PUSH);
        this.pushSocket.connect(brokerAddress.substring(0, brokerAddress.length() - 1) + "1"); // port + 1 ()
        System.out.println("server address: " + serverAddress);
        System.out.println("PULL SOCKET: " + serverAddress.substring(0, serverAddress.length() - 1) + "1");
        this.ring = null;
        this.executorService = Executors.newFixedThreadPool(2);

        try {
            this.consistentHashing = new ConsistentHashing(1);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        joinRing();
        executorService.submit(this::checkNeighbourHeatbeat);
        executorService.submit(this::listenForMessages);
    }
    
    private void checkNeighbourHeatbeat() {
        int failedPings = 0;
        System.out.println("Checking neighbour heartbeat");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String nextNodeAddress = consistentHashing.getServerAfter(this.serverAddress, ring, true);
                System.out.println("NEXT NODE: " + nextNodeAddress);
                if (nextNodeAddress != null) {
                    boolean isAlive = sendPing(nextNodeAddress);
                    if (!isAlive) {
                        failedPings++;
                        if (failedPings >= 5) {
                            System.out.println("DETECTED DEAD NEIGHBOUR!");
                            removeDeadNeighbour(nextNodeAddress);
                            failedPings = 0;
                        }
                    } else {
                        failedPings = 0; // reset counter on successful ping
                    }
                }
                TimeUnit.SECONDS.sleep(1); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean sendPing(String address) {
        try (ZMQ.Socket pingSocket = context.createSocket(SocketType.REQ)) {
            pingSocket.connect(address);
            pingSocket.send("PING");

            pingSocket.setReceiveTimeOut(1000);

            String response = pingSocket.recvStr();
            System.out.println(response);
            return "PONG".equals(response);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    

    private void removeDeadNeighbour(String deadNeighbourAddress){
        consistentHashing.removeServer(deadNeighbourAddress, ring);
    
        for(String nodeAddress : this.ring.values()){
            if(!Objects.equals(nodeAddress, this.serverAddress)){
                try (ZMQ.Socket msgSocket = context.createSocket(SocketType.PUSH)) {
                    msgSocket.connect(nodeAddress.substring(0, nodeAddress.length() - 1) + "1"); //connect to server's pull socket
                    ZMsg serverMsg = new ZMsg();
                    serverMsg.addString("REMOVE");
                    serverMsg.addString(deadNeighbourAddress);
                    serverMsg.send(msgSocket);
                }
            }
        }

        ZMsg brokerMsg = new ZMsg();
        brokerMsg.addString("REMOVE");
        brokerMsg.addString(deadNeighbourAddress);
        brokerMsg.send(this.pushSocket);
        
    }
    

    private void joinRing() {
        ZMsg msg = new ZMsg();
        msg.addString("JOIN");
        msg.addString(this.serverAddress);
        msg.send(this.outgoingSocket);

        ZMsg response = ZMsg.recvMsg(this.outgoingSocket);
        if (response != null) {
            String ringState = response.popString();
            this.ring = utils.stringToTreeMap(ringState);
            System.out.println("Received ring state: " + this.ring);


            for(String nodeAddress : this.ring.values()){
                if(!Objects.equals(nodeAddress, this.serverAddress)){
                    try (ZMQ.Socket msgSocket = context.createSocket(SocketType.PUSH)) {
                        msgSocket.connect(nodeAddress.substring(0, nodeAddress.length() - 1) + "1"); //connect to server's pull socket
                        ZMsg serverMsg = new ZMsg();
                        serverMsg.addString("JOIN");
                        serverMsg.addString(this.serverAddress);
                        System.out.println("message sent: " + serverMsg + " to server: " + nodeAddress.substring(0, nodeAddress.length() - 1) + "1");
                        serverMsg.send(msgSocket);
                    }
                }
            }
        }
    }

    private void listenForMessages() {
        System.out.println("Listening for messages");
        Poller poller = context.createPoller(2);
        poller.register(socket, Poller.POLLIN);
        poller.register(pullSocket, Poller.POLLIN);

        while (!Thread.currentThread().isInterrupted()) {
            poller.poll(); // Wait for an event on either socket

            if (poller.pollin(0)) { // Check REP socket
                ZMsg repRequest = ZMsg.recvMsg(socket);
                if (repRequest != null) {
                    handleRepRequest(repRequest);
                }
            }

            if (poller.pollin(1)) { // Check PULL socket
                ZMsg pullRequest = ZMsg.recvMsg(pullSocket);
                if (pullRequest != null) {
                    handlePullRequest(pullRequest);
                }
            }
        }
    }

    private void handleRepRequest(ZMsg request) {
        String header = request.popString();
        String server = null;
        ZMsg response = new ZMsg();
        System.out.println("REP REQUEST: " + header);
        switch (header) {
            case "PING" -> {
                response.addString("PONG");
                response.send(socket);
            }
            default -> {
            }
            //ignore
        }
    }

    private void handlePullRequest(ZMsg request) {
        String header = request.popString();
        String server = null;
        System.out.println("PULL REQUEST: " + header);
        switch (header) {
            case "JOIN" -> {
                System.out.println("SERVER JOINED: " + ring);
                server = request.popString();
                consistentHashing.addServer(server, ring);
            }
            case "REMOVE" -> {
                System.out.println("SERVER REMOVED: " + ring);
                server = request.popString();
                consistentHashing.removeServer(server, ring);
            }
            default -> {
            }
            //ignore
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

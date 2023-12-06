package com.shopup;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Poller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;

public class ServerNode {
    private final String serverAddress;
    private final ZContext context;

    private final ZMQ.Socket socket;
    private final ZMQ.Socket dealerSocket;

    private final ConsistentHashing consistentHashing;
    private TreeMap<Integer,String> ring;
    private final String brokerAddress;
    private final ExecutorService executorService;

    private final Utils utils = new Utils();

    private Map<String, User> userDataStore = new HashMap<>();
    
    public ServerNode(String serverAddress, String brokerAddress) {
        this.serverAddress = serverAddress;
        this.brokerAddress = brokerAddress;

        this.context = new ZContext();

        this.dealerSocket = context.createSocket(SocketType.DEALER);
        this.dealerSocket.bind(serverAddress.substring(0, serverAddress.length() - 1) + "1");

        this.socket = context.createSocket(SocketType.REP);
        this.socket.bind(serverAddress);

        System.out.println("server address: " + serverAddress);
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

    public ConsistentHashing getConsistentHashing() {
        return this.consistentHashing;
    }

    public TreeMap<Integer,String> getRing() {
        return this.ring;
    }

    public String getServerAddress() {
        return this.serverAddress;
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
            return "PONG".equals(response);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    

    private void removeDeadNeighbour(String deadNeighbourAddress){
        consistentHashing.removeServer(deadNeighbourAddress, ring);

        for (String nodeAddress : this.ring.values()) {
            if (!Objects.equals(nodeAddress, this.serverAddress)) {
                ZMQ.Socket nodeDealerSocket = context.createSocket(SocketType.DEALER);
                ZMsg msg = new ZMsg();
                msg.addString("REMOVE");
                msg.addString(deadNeighbourAddress);
                String addr = nodeAddress.substring(0, nodeAddress.length() - 1) + "1";
                nodeDealerSocket.connect(addr);
                System.out.println("sending message: " + msg + " to: " + addr);
                msg.send(nodeDealerSocket);
            }
        }
        ZMsg msg = new ZMsg();
        msg.addString("REMOVE");
        msg.addString(deadNeighbourAddress);

        byte[] identity = serverAddress.getBytes();
        ZMQ.Socket brokerSocket = context.createSocket(SocketType.DEALER);
        brokerSocket.setIdentity(identity);
        brokerSocket.connect(brokerAddress);
        msg.send(brokerSocket);

        
    }
    

    private void joinRing() {
        ZMsg msg = new ZMsg();
        msg.addString("JOIN");
        msg.addString(this.serverAddress);

        byte[] identity = serverAddress.getBytes();
        ZMQ.Socket brokerSocket = context.createSocket(SocketType.DEALER);
        brokerSocket.setIdentity(identity);
        brokerSocket.connect(brokerAddress);
        msg.send(brokerSocket);

        ZMsg response = ZMsg.recvMsg(brokerSocket); //not sure if this is correct
        System.out.println("RECEIVED RESPONSE: " + response);
        if (response != null) {
            String ringState = response.popString();
            this.ring = utils.stringToTreeMap(ringState);
            System.out.println("Received ring state: " + this.ring);
            

            for (String nodeAddress : this.ring.values()) {
                if (!Objects.equals(nodeAddress, this.serverAddress)) {
                    ZMQ.Socket nodeDealerSocket = context.createSocket(SocketType.DEALER);
                    msg = new ZMsg();
                    msg.addString("JOIN");
                    msg.addString(this.serverAddress);
                    String addr = nodeAddress.substring(0, nodeAddress.length() - 1) + "1";
                    nodeDealerSocket.connect(addr);
                    System.out.println("sending message: " + msg + " to: " + addr);
                    msg.send(nodeDealerSocket);
                }
            }
        }
        brokerSocket.close();
    }

    private void listenForMessages() {
        System.out.println("Listening for messages");
        Poller poller = context.createPoller(2);
        poller.register(socket, Poller.POLLIN);
        poller.register(dealerSocket, Poller.POLLIN);

        while (!Thread.currentThread().isInterrupted()) {
            poller.poll(); // Wait for an event on either socket

            if (poller.pollin(0)) { // Check REP socket
                ZMsg repRequest = ZMsg.recvMsg(socket);
                if (repRequest != null) {
                    handleRepRequest(repRequest);
                }
            }

            if (poller.pollin(1)) { // Check PULL socket
                ZMsg dealerRequest = ZMsg.recvMsg(dealerSocket);
                if (dealerRequest != null) {
                    handleDealerRequest(dealerRequest);
                }
            }
        }
    }

    private void handleRepRequest(ZMsg request) {
        String header = request.popString();
        ZMsg response = new ZMsg();
        System.out.println("REP REQUEST: " + header);
        switch (header) {
            case "PING" -> {
                response.addString("PONG");
                response.send(socket);
            }
            case "USER_DATA" -> {
                handleUserData(request);
            }
            default -> {
                // ignore
            }
        }
    }

    private void handleDealerRequest(ZMsg request) {
        String header = request.popString();
        String server = null;
        System.out.println("DEALER REQUEST: " + header);
        switch (header) {
            case "JOIN" -> {
                server = request.popString();
                System.out.println("SERVER JOINED: " + server);
                consistentHashing.addServer(server, ring);
            }
            case "REMOVE" -> {
                server = request.popString();
                System.out.println("SERVER REMOVED: " + server);
                consistentHashing.removeServer(server, ring);
            }
            default -> {
            }
            //ignore
        }
    }

    private void handleUserData(ZMsg request) {
        String jsonData = request.popString();
        ObjectMapper mapper = new ObjectMapper();
        try {
            User user = mapper.readValue(jsonData, User.class);
            userDataStore.put(user.getUsername(), user);
            System.out.println("User data received and stored for user: " + user.getUsername());
        } catch (IOException e) {
            e.printStackTrace();
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

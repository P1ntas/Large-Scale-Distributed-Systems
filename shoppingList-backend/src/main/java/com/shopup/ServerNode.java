package com.shopup;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Poller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.zeromq.SocketType;

import static com.shopup.JSONHandler.*;

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

    private Map<String, List<ShoppingList>> shoppingLists = new HashMap<>();
    
    public ServerNode(String serverAddress, String brokerAddress) {
        this.serverAddress = serverAddress;
        this.brokerAddress = brokerAddress;

        this.context = new ZContext();

        this.dealerSocket = context.createSocket(SocketType.ROUTER);
        this.dealerSocket.bind(serverAddress.substring(0, serverAddress.length() - 1) + "1");

        this.socket = context.createSocket(SocketType.DEALER);
        this.socket.bind(serverAddress);

        System.out.println("server address: " + serverAddress);
        this.ring = null;

        this.shoppingLists.put("primary", new ArrayList<>());
        this.shoppingLists.put("replica", new ArrayList<>());

        this.executorService = Executors.newFixedThreadPool(3);


        try {
            this.consistentHashing = new ConsistentHashing(1, 2);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        System.out.println("\n Starting server: " + serverAddress);
        joinRing();
        executorService.submit(this::checkNeighbourHeatbeat);
        executorService.submit(this::listenForBeatRequest);
        executorService.submit(this::listenForDealerRequest);

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
        System.out.println("\nChecking neighbour heartbeat");
        while (!Thread.currentThread().isInterrupted()) {

            try {
                String nextNodeAddress = consistentHashing.getServerAfter(this.serverAddress, ring, true);
                if (nextNodeAddress != null) {
                    boolean isAlive = sendPing(nextNodeAddress);
                    if (!isAlive) {
                        failedPings++;
                        if (failedPings >= 3) {
                            System.out.println("\nDETECTED DEAD NEIGHBOUR!");
                            removeDeadNeighbour(nextNodeAddress);
                            failedPings = 0;
                        }
                    } else {
                        failedPings = 0; // reset counter on successful ping
                    }
                }
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean sendPing(String address) {
        try (ZMQ.Socket pingSocket = context.createSocket(SocketType.DEALER)) {
            pingSocket.connect(address);
            pingSocket.send("PING");

            pingSocket.setReceiveTimeOut(500);
            String response = pingSocket.recvStr();

            return "PONG".equals(response);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    

    private void removeDeadNeighbour(String deadNeighbourAddress){
        consistentHashing.removeServer(deadNeighbourAddress, ring);
        rebalance();
        replicate();

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
        ZMsg.recvMsg(brokerSocket);
        brokerSocket.close();
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
        System.out.println("\nRECEIVED RESPONSE: " + response);
        if (response != null) {
            String ringState = response.popString();
            this.ring = utils.stringToTreeMap(ringState);
            System.out.println("\nReceived ring state: " + this.ring);
            

            for (String nodeAddress : this.ring.values()) {
                if (!Objects.equals(nodeAddress, this.serverAddress)) {
                    ZMQ.Socket nodeDealerSocket = context.createSocket(SocketType.DEALER);
                    nodeDealerSocket.setIdentity(LocalTime.now().toString().getBytes());
                    msg = new ZMsg();
                    msg.addString("JOIN");
                    msg.addString(this.serverAddress);
                    String addr = nodeAddress.substring(0, nodeAddress.length() - 1) + "1";
                    nodeDealerSocket.connect(addr);
                    System.out.println("\nsending message: " + msg + " to: " + addr);
                    msg.send(nodeDealerSocket);
                    ZMsg.recvMsg(nodeDealerSocket);
                    nodeDealerSocket.close();

                }
            }
        }
        brokerSocket.close();
    }

    private void replicate(){
        ObjectMapper mapper = new ObjectMapper();

        for (ShoppingList item : this.shoppingLists.get("primary")){
            String id = item.getId().toString();
            String primaryNode = this.consistentHashing.getServerAfter(id, this.ring, false);

            if  (this.serverAddress.equals(primaryNode)){
                String currentServer = this.serverAddress;

                for (int i = 0; i < Math.min(this.consistentHashing.getN_replication()-1,this.ring.size()-1); i++){
                    currentServer = this.consistentHashing.getServerAfter(currentServer, this.ring, true);
                    ZMQ.Socket nodeDealerSocket = context.createSocket(SocketType.DEALER);
                    nodeDealerSocket.setIdentity(LocalTime.now().toString().getBytes());
                    ZMsg msg = new ZMsg();
                    msg.addString("REPLICATE");
                    String jsonData = null;
                    try {
                        jsonData = mapper.writeValueAsString(item);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    msg.addString(jsonData);
                    String addr = currentServer.substring(0, currentServer.length() - 1) + "1";
                    nodeDealerSocket.connect(addr);
                    msg.send(nodeDealerSocket);
                    ZMsg.recvMsg(nodeDealerSocket);
                    nodeDealerSocket.close();
                    System.out.println("\nITEM " + item.getName() + " IS BEING REPLICATED ON SERVER " + addr );

                }
            }
        }

    }

    private void rebalance(){
        ObjectMapper mapper = new ObjectMapper();
        for (ShoppingList item : this.shoppingLists.get("primary")){
            String id = item.getId().toString();
            String primaryNode = this.consistentHashing.getServerAfter(id, this.ring, false);
            String currentServer = this.serverAddress;
            if  (!this.serverAddress.equals(primaryNode)){
                //remove replicas from neighbors
                /*for (int i = 0; i < Math.min(this.consistentHashing.getN_replication()-1,this.ring.size()-1); i++){
                    System.out.println("REMOVING UNNECESSARY REPLICAS FROM NEIGHBORS");
                    currentServer = this.consistentHashing.getServerAfter(currentServer, this.ring, true);
                    ZMQ.Socket nodeDealerSocket = context.createSocket(SocketType.DEALER);
                    nodeDealerSocket.setIdentity(LocalTime.now().toString().getBytes());
                    ZMsg msg = new ZMsg();
                    msg.addString("DELETE_REPLICA");
                    String jsonData = null;
                    try {
                        jsonData = mapper.writeValueAsString(item);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    msg.addString(jsonData);
                    String addr = currentServer.substring(0, currentServer.length() - 1) + "1";
                    nodeDealerSocket.connect(addr);
                    msg.send(nodeDealerSocket);
                    ZMsg.recvMsg(nodeDealerSocket);
                    nodeDealerSocket.close();

                }*/

                ZMQ.Socket nodeDealerSocket = context.createSocket(SocketType.DEALER);
                nodeDealerSocket.setIdentity(LocalTime.now().toString().getBytes());
                ZMsg msg = new ZMsg();
                msg.addString("REBALANCE");
                String jsonData = null;
                try {
                    jsonData = mapper.writeValueAsString(item);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                msg.addString(jsonData);
                String addr = primaryNode.substring(0, primaryNode.length() - 1) + "1";
                nodeDealerSocket.connect(addr);
                msg.send(nodeDealerSocket);
                ZMsg.recvMsg(nodeDealerSocket);
                nodeDealerSocket.close();
                System.out.println("\nITEM " + item.getName() + " IS BEING SENT TO SERVER " + addr );
                this.shoppingLists.get("primary").remove(item);

            }
        }


        for (ShoppingList item : this.shoppingLists.get("replica")){
            String id = item.getId().toString();
            String primaryNode = this.consistentHashing.getServerAfter(id, this.ring, false);
            if  (this.serverAddress.equals(primaryNode)){
                System.out.println("Item " + item.getName() + " is now primary instead of replica");
                this.shoppingLists.get("primary").add(item);
                this.shoppingLists.get("replica").remove(item);
            }
        }


    }

    private void listenForBeatRequest() {

        while (!Thread.currentThread().isInterrupted()) {
            ZMsg beatRequest = ZMsg.recvMsg(socket);
            if (beatRequest != null) {
                handleBeatRequest(beatRequest);
            }
        }
    }

    private void listenForDealerRequest() {

        while (!Thread.currentThread().isInterrupted()) {
            ZMsg dealerRequest = ZMsg.recvMsg(dealerSocket);
            if (dealerRequest != null) {
                handleDealerRequest(dealerRequest);
            }
        }
    }


    private void handleBeatRequest(ZMsg request) {
        String header = request.popString();
        ZMsg response = new ZMsg();

        switch (header) {
            case "PING" -> {
                response.addString("PONG");
                response.send(socket);
            }
            default -> {
                System.out.println("UNKNOWN MESSAGE HEADER:" + header);
            }
        }
    }

    private void handleDealerRequest(ZMsg request) {
        String dealerIdentity = request.popString();
        String header = request.popString();
        String server = null;
        System.out.println("\nDEALER REQUEST: " + header);
        ZMsg routerResp = new ZMsg();
        routerResp.addString(dealerIdentity);
        switch (header) {
            case "JOIN" -> {
                server = request.popString();
                System.out.println("SERVER JOINED: " + server);
                consistentHashing.addServer(server, ring);
                System.out.println();
                routerResp.addString("ACK");
                routerResp.send(dealerSocket);
                rebalance();
                replicate();

            }
            case "REMOVE" -> {
                server = request.popString();
                System.out.println("SERVER REMOVED: " + server);
                consistentHashing.removeServer(server, ring);
                routerResp.addString("ACK");
                routerResp.send(dealerSocket);
                rebalance();
                replicate();

            }
            case "REQUEST" -> {
                ObjectMapper mapper = new ObjectMapper();
                String itemID = request.popString();
                ShoppingList requestedList = null;
                System.out.println("LIST REQUESTED: " + itemID);

                List<ShoppingList> combinedList = new ArrayList<>(shoppingLists.get("primary"));
                combinedList.addAll(shoppingLists.get("replica"));

                for (ShoppingList shoppingList : combinedList){
                    if(shoppingList.getId().toString().equals(itemID)){
                        requestedList = shoppingList;
                        break;
                    }
                }

                String jsonResponse = null;
                if(requestedList == null){
                    jsonResponse = "NULL";
                }
                else{
                    try {
                        jsonResponse = mapper.writeValueAsString(requestedList);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }

                routerResp.addString(jsonResponse);
                routerResp.send(this.dealerSocket);

            }
            case "UPDATE","REPLICATE", "REBALANCE" -> {

                ObjectMapper mapper = new ObjectMapper();
                String jsonData = request.popString();
                ShoppingList clientList, updatedList = null;

                try {
                    clientList = mapper.readValue(jsonData, ShoppingList.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                String itemID = clientList.getId().toString();
                System.out.println("LIST TO UPDATE: " + itemID);

                List<ShoppingList> combinedList = new ArrayList<>(shoppingLists.get("primary"));
                combinedList.addAll(shoppingLists.get("replica"));

                for (ShoppingList shoppingList : combinedList){
                    if(shoppingList.getId().toString().equals(itemID)){
                        shoppingList = shoppingList.merge(clientList);
                        updatedList = shoppingList;
                        break;
                    }
                }

                if(updatedList == null){
                    if(header.equals("REPLICATE")) {
                        this.shoppingLists.get("replica").add(clientList);
                    }
                    else{
                        this.shoppingLists.get("primary").add(clientList);
                    }

                    updatedList = clientList;
                }

                if(header.equals("UPDATE")){
                    String jsonResponse = null;
                    try {
                        jsonResponse = mapper.writeValueAsString(updatedList);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("response: " + jsonResponse);
                    routerResp.addString(jsonResponse);
                    routerResp.send(this.dealerSocket);
                }
                else {
                    routerResp.addString("ACK");
                    routerResp.send(this.dealerSocket);
                }

                if(!header.equals("REPLICATE")){
                    replicate();
                }

                System.out.println(this.shoppingLists);
            }

            case "DELETE_LIST","DELETE_REPLICA" -> {
                ObjectMapper mapper = new ObjectMapper();
                String jsonData = request.popString();
                ShoppingList item = null;
                try {
                    item = mapper.readValue(jsonData, ShoppingList.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                String itemID = item.getId().toString();
                System.out.println("DELETING LIST " + item.getName());
                if(header.equals("DELETE_LIST")){
                    this.shoppingLists.get("primary").removeIf(shoppingList -> shoppingList.getId().toString().equals(itemID));
                    replicate();
                }
                else{
                    this.shoppingLists.get("replica").removeIf(shoppingList -> shoppingList.getId().toString().equals(itemID));
                }
                routerResp.addString("ACK");
                routerResp.send(this.dealerSocket);
            }
            default -> {
                System.out.println("UNKNOWN MESSAGE HEADER:" + header);
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

package com.shopup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Poller;

import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;
import org.zeromq.SocketType;

public class Broker {

    private ConsistentHashing consistentHashing;
    private ZContext context;
    private ZMQ.Socket serverRoutSocket, clientRoutSocket; // REP socket for "JOIN" requests

    private TreeMap<Integer, String> ring;

    public Broker() throws NoSuchAlgorithmException {
        this.consistentHashing = new ConsistentHashing(1, 2);
        this.context = new ZContext();

        this.serverRoutSocket = context.createSocket(SocketType.ROUTER);
        this.serverRoutSocket.bind("tcp://127.0.0.1:5000");

        this.clientRoutSocket = context.createSocket(SocketType.ROUTER);
        this.clientRoutSocket.bind("tcp://127.0.0.1:5001");

        this.ring = new TreeMap<>();
    }

    public void start() {
        System.out.println("BROKER STARTED");
        Poller poller = context.createPoller(1); //! LATEr ADD ROUTER CLIENT
        poller.register(serverRoutSocket, Poller.POLLIN);
        poller.register(clientRoutSocket, Poller.POLLIN);

        while (!Thread.currentThread().isInterrupted()) {
            poller.poll();

            if (poller.pollin(0)) { 
                ZMsg serverRoutReq = ZMsg.recvMsg(serverRoutSocket);
                if (serverRoutReq != null) {
                    handleServerRoutRequest(serverRoutReq);
                }
            }
            if (poller.pollin(1)) {
                ZMsg clientRoutReq = ZMsg.recvMsg(clientRoutSocket);
                if (clientRoutReq != null) {
                    handleClientRoutRequest(clientRoutReq);
                }
            }
        }
    }

    private void handleServerRoutRequest(ZMsg request) {
        System.out.println("REQUEST: " + request);
        String dealerIdentity = request.popString();
        String header = request.popString();
        String server = null;
        ZMsg response = new ZMsg();
        System.out.println("identity: " + dealerIdentity);
        response.addString(dealerIdentity);

        switch (header) {
            case "JOIN" -> {
                server = request.popString();
                consistentHashing.addServer(server, ring);
                response.addString(ring.toString());
                System.out.println("RESPONSE SENT: " + response);
                response.send(serverRoutSocket);
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

    private void handleClientRoutRequest(ZMsg request) {
        System.out.println("REQUEST: " + request);
        String clientIdentity = request.popString();
        String header = request.popString();

        switch (header) {
            case "REQUEST" -> {
                String itemID = request.popString();
                int hashedItemID = consistentHashing.getHash(itemID);
                String targetServer = consistentHashing.getServerAfter(hashedItemID, ring, false); // Determine the target server for this request

                ZMQ.Socket serverSocket = context.createSocket(SocketType.DEALER);
                serverSocket.connect(targetServer.substring(0, targetServer.length() - 1) + "1"); // Connect to the server's DEALER socket

                // Forward the request to the server
                ZMsg serverRequest = new ZMsg();
                serverRequest.addString(header);
                serverRequest.addString(itemID);
                serverRequest.send(serverSocket);

                // Wait for the server's response
                ZMsg serverResponse = ZMsg.recvMsg(serverSocket);
                if (serverResponse != null) {
                    // Forward the response back to the client
                    ZMsg clientResponse = new ZMsg();
                    clientResponse.addString(clientIdentity);
                    clientResponse.add(serverResponse.popString());
                    clientResponse.send(clientRoutSocket);
                }
                serverSocket.close();
            }

            case "UPDATE" -> {
                ObjectMapper mapper = new ObjectMapper();
                String jsonData = request.popString();
                ShoppingList item = null;
                try {
                    item = mapper.readValue(jsonData, ShoppingList.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                String itemID = item.getId().toString();
                int hashedItemID = consistentHashing.getHash(itemID);
                String targetServer = consistentHashing.getServerAfter(hashedItemID, ring, false);

                ZMQ.Socket serverSocket = context.createSocket(SocketType.DEALER);
                serverSocket.connect(targetServer.substring(0, targetServer.length() - 1) + "1"); // Connect to the server's DEALER socket

                // Forward the request to the server
                ZMsg serverRequest = new ZMsg();
                serverRequest.addString(header);
                serverRequest.addString(jsonData);
                serverRequest.send(serverSocket);

                // Wait for the server's response
                ZMsg serverResponse = ZMsg.recvMsg(serverSocket);
                if (serverResponse != null) {
                    // Forward the response back to the client
                    ZMsg clientResponse = new ZMsg();
                    clientResponse.addString(clientIdentity);
                    clientResponse.add(serverResponse.popString());
                    clientResponse.send(clientRoutSocket);
                }
            }

            default -> {
                // Ignore other headers
            }
        }
    }


    public static void main(String[] args) {
        try {
            Broker broker = new Broker();
            broker.start();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}

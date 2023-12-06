package com.shopup;

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
    private ZMQ.Socket serverRoutSocket; // REP socket for "JOIN" requests
    private TreeMap<Integer, String> ring;

    public Broker() throws NoSuchAlgorithmException {
        this.consistentHashing = new ConsistentHashing(1);
        this.context = new ZContext();

        this.serverRoutSocket = context.createSocket(SocketType.ROUTER);
        this.serverRoutSocket.bind("tcp://127.0.0.1:5000");

        this.ring = new TreeMap<>();
    }

    public void start() {
        System.out.println("BROKER STARTED");
        Poller poller = context.createPoller(1); //! LATEr ADD ROUTER CLIENT
        poller.register(serverRoutSocket, Poller.POLLIN);

        while (!Thread.currentThread().isInterrupted()) {
            poller.poll();

            if (poller.pollin(0)) { 
                ZMsg serverRoutReq = ZMsg.recvMsg(serverRoutSocket);
                if (serverRoutReq != null) {
                    handleServerRoutRequest(serverRoutReq);
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


    public static void main(String[] args) {
        try {
            Broker broker = new Broker();
            broker.start();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}

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
    private ZMQ.Socket repSocket; // REP socket for "JOIN" requests
    private ZMQ.Socket pullSocket; // PULL socket for "REMOVE" requests
    private TreeMap<Integer, String> ring;

    public Broker() throws NoSuchAlgorithmException {
        this.consistentHashing = new ConsistentHashing(1);
        this.context = new ZContext();
        this.repSocket = context.createSocket(SocketType.REP);
        this.repSocket.bind("tcp://127.0.0.1:5000");
        this.pullSocket = context.createSocket(SocketType.PULL);
        this.pullSocket.bind("tcp://127.0.0.1:5001");
        this.ring = new TreeMap<>();
    }

    public void start() {
        Poller poller = context.createPoller(2);
        poller.register(repSocket, Poller.POLLIN);
        poller.register(pullSocket, Poller.POLLIN);

        while (!Thread.currentThread().isInterrupted()) {
            poller.poll(); // Wait for an event on either socket

            if (poller.pollin(0)) { // Check REP socket
                ZMsg repRequest = ZMsg.recvMsg(repSocket);
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
        System.out.println("REQUEST: " + request);
        switch (header) {
            case "JOIN" -> {
                System.out.println("SERVER JOINED: " + ring);
                server = request.popString();
                consistentHashing.addServer(server, ring);
                response.addString(ring.toString());
                response.send(repSocket);
            }
            default -> {
            }
            //ignore
        }
    }

    private void handlePullRequest(ZMsg request) {
        String header = request.popString();
        String server = null;
        System.out.println("REQUEST: " + request);
        switch (header) {
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

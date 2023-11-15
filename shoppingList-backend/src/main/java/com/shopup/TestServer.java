package com.shopup;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
public class TestServer {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            // Socket to talk to clients
            ZMQ.Socket socket = context.createSocket(ZMQ.REP);
            socket.bind("tcp://*:5555");

            while (!Thread.currentThread().isInterrupted()) {
                // Block until a message is received
                byte[] reply = socket.recv(0);
                System.out.println("Received: " + new String(reply, ZMQ.CHARSET));

                // Send a response
                String response = "Hello, client!";
                socket.send(response.getBytes(ZMQ.CHARSET), 0);
            }
        }
    }
}

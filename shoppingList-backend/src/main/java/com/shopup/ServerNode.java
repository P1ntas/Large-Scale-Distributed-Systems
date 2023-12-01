import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.TreeMap;

import org.zeromq.SocketType;

public class ServerNode {
    private final String serverAddress;
    private final ZContext context;
    private final ZMQ.Socket socket;
    private final ZMQ.Socket outgoingSocket;
    private final ConsistentHashing consistentHashing;
    private String nextNodeAddress;
    
    public ServerNode(String serverAddress, String brokerAddress) {
        this.serverAddress = serverAddress;
        this.context = new ZContext();
        this.socket = context.createSocket(SocketType.REP);
        this.outgoingSocket = context.createSocket(SocketType.REQ);
        this.socket.bind(serverAddress);
        this.outgoingSocket.connect(brokerAddress);
        this.consistentHashing = new ConsistentHashing(1);
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
            //!transform string into treemap
            //!ring = wtv
            System.out.println("Received ring state: " + ringState);
            this.nextNodeAddress = this.consistentHashing.getServerAfter(this.serverAddress, null, true);
            //send message to next node saying hello from serverAddress
            ZMQ.Socket msgSocket = context.createSocket(SocketType.REQ);
            msgSocket.connect(this.nextNodeAddress);
            ZMsg serverMsg = new ZMsg();
            serverMsg.addString("Hello from " + serverAddress);
            serverMsg.send(msgSocket);
        }
    }

    private void listenForMessages() {
        //! ISTO DEVE SER UMA STATE MACHINE ACHO EU
        while (!Thread.currentThread().isInterrupted()) {
            ZMsg request = ZMsg.recvMsg(socket);
            if (request != null) {
                String message = request.popString();
                System.out.println("Received message: " + message);
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "127.0.0.2:5000";
        String brokerAddress = "127.0.0.1:5000"; // Broker address
        ServerNode node = new ServerNode(serverAddress, brokerAddress);
        node.start();
    }
}

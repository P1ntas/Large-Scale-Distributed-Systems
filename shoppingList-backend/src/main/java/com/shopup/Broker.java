import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;
import com.shopup.ConsistentHashing;
import org.zeromq.SocketType;

public class Broker {

    private ConsistentHashing consistentHashing;
    private ZContext context;
    private ZMQ.Socket socket;
    private TreeMap<Integer, String> ring;

    public Broker() throws NoSuchAlgorithmException {
        this.consistentHashing = new ConsistentHashing(1);
        this.context = new ZContext();
        this.socket = context.createSocket(SocketType.REP);
        this.socket.bind("127.0.0.1:5000");
        this.ring = new TreeMap<>();
    }

    public void start() {
        while (!Thread.currentThread().isInterrupted()) {
            ZMsg request = ZMsg.recvMsg(socket);
            if (request != null) {
                handleRequest(request);
            }
        }
    }

    private void handleRequest(ZMsg request) {
        //! ISTO DEVE SER UMA STATE MACHINE ACHO EU
        String header = request.popString();
        if ("JOIN".equals(header)) {
            String server = request.popString();
            consistentHashing.addServer(server, ring);
            ZMsg response = new ZMsg();
            response.addString(ring.toString());
            response.send(socket);
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

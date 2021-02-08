package company;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

class ServerMain {
    public static void main(String[] args){
        int port = 8818;
        Server server2 = null;
        Server server = new Server(server2, port);
        server.start();
    }


}

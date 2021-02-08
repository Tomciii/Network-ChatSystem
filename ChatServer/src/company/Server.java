package company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final int serverPort;
    private final Server server;
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    public Server(Server server, int port) {
        this.server = server;
        this.serverPort = port;
    }

    public List<ServerWorker> getWorkerList(){
        return workerList;
    }

    @Override
    public void run(){
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("About to accept connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}

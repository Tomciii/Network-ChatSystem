package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();
    public ChatClient(String servername, int serverPort) {
        this.serverName = servername;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
 ChatClient client = new ChatClient("localhost", 8818);
 client.addUserStatusListener(new UserStatusListener() {
     @Override
     public void online(String login) {
         System.out.println("ONLINE: " + login);
     }

     @Override
     public void offline(String login) {
         System.out.println("OFFLINE: " + login);
     }
 });

 client.addMessageListener(new MessageListener() {
     @Override
     public void onMessage(String fromLogin, String msgBody) {
         System.out.println("You got a message from " + fromLogin + ":" + msgBody);
     }
 });

    if(!client.connect())
    {
        System.err.println("Connect failed");
    }
    else
     {
     System.err.println("Connect Successful!");
         if (client.login("guest", "guest"))
         {
           System.out.println("Login SuccessFul");

           client.msg("jim", "Hello World!");
         }
         else
         {
             System.out.println("Login failed");
         }
       // client.logoff();
     }
    }

    public void msg(String sentTo, String msgBody) throws IOException {
        String cmd = "msg " + sentTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    public void logoff()  throws IOException {
        String cmd = "logoff\n ";
        serverOut.write(cmd.getBytes());
    }

    public boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());
        String response = bufferIn.readLine();
        System.out.println("Response line: " + response);

        if ("ok login".equalsIgnoreCase(response)){
            startMessageReader();
            return true;
        }
        else
        {
            return false;
        }
    }

    private void startMessageReader() {
        Thread t = new Thread(){
            @Override
            public void run(){
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
       String line;
       while (true){
           try {
               if (!((line = bufferIn.readLine()) != null)) {
                   String[] tokens = StringUtils.split(line);
                   if (tokens != null && tokens.length > 0) {
                       String cmd = tokens[0];
                       if ("online".equalsIgnoreCase(cmd)){
                           handleOnline(tokens);
                       }
                       else if("offline".equalsIgnoreCase(cmd))
                       {
                           String[] tokensMsg= StringUtils.split(line, null, 3);
                           handleMessage(tokensMsg);
                       }
                       else if("msg".equalsIgnoreCase(cmd)){
                           handleMessage(tokens);
                       }
                   }
               }
           } catch (IOException e) {
               e.printStackTrace();
               try {
                   socket.close();
               } catch (IOException ioException) {
                   ioException.printStackTrace();
               }
           }

       }
    }

    private void handleMessage(String[] tokens) {
            String login = tokens[1];
            String msgBody = tokens[2];

            for(MessageListener listener : messageListeners){
                listener.onMessage(login,msgBody);
            }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.online(login);
        }
    }

    public boolean connect(){
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is: " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }
}
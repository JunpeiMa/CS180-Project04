import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;


    private ChatServer(int port) {
        this.port = port;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server = new ChatServer(1500);
        if (args.length == 1)
            server = new ChatServer(Integer.parseInt(args[0]));
        server.start();
    }

    private void broadcast(String message)
    {
        synchronized (this) { //Might want to check this later...
            ChatFilter cf = new ChatFilter("badwords.txt");
            String filteredMsg = cf.filter(message);
            SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
            String timeRecieved = time.format(new Date());
            String messageWithTime = timeRecieved + " " + filteredMsg;
            for (int i = 0; i < clients.size(); i++)
            {
                clients.get(i).writeMessage(messageWithTime);
            }
            System.out.println(messageWithTime);
        }
    }

    private void remove(int id)
    {
        //TODO: Implement method. Removes a client from the ArrayList.
    }

    private void close()
    {
        //TODO: Implement method. This does the same thing as logging out of the ChatClient
    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {


            while (this.socket.isConnected()) { //TODO: Issue here. When client logs off, this still loops.
                // Read the username sent to you by client
                try {
                    cm = (ChatMessage) sInput.readObject();
                    //System.out.println(username + ": Ping");
                    String message = (username + " : " + cm.getMsg());
                    broadcast(message);


                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

//                try {
//                    sOutput.writeObject("Pong");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }

        private boolean writeMessage(String msg)
        {
            if (this.socket.isConnected())
            {
                try {
                    sOutput.writeObject(msg);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                return true;
            } else
            {
                return false;
            }
        }
    }
}

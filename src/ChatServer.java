import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

final class ChatServer {
    private static int uniqueId = 0;
    public final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private final String fileName;
    private static SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");


    private ChatServer(int port, String fileName) {
        this.port = port;
        this.fileName = fileName;
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
        String bannedWordsFileName = "badwords.txt";
        ChatServer server = new ChatServer(1500, bannedWordsFileName);
        if (args.length == 1) {
            server = new ChatServer(Integer.parseInt(args[0]), bannedWordsFileName);
        } else if (args.length == 2) {
            bannedWordsFileName = args[1];
            server = new ChatServer(Integer.parseInt(args[0]), args[1]);
        }
        System.out.println("Banned Words File: " + bannedWordsFileName);
        ArrayList<String> badWords = new ArrayList<>();
        File f;
        FileReader fr;
        BufferedReader br;

        try {
            f = new File(bannedWordsFileName);
            fr = new FileReader(f);
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                badWords.add(line);
            }

            fr.close();
            br.close();
            System.out.println("Banned Words:");
            for (int i = 0; i < badWords.size(); i++) {
                System.out.println(badWords.get(i));
            }
            System.out.println();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Censored words file not found. Please restart server with a valid file.");
            return;
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        String timeReceived = time.format(new Date());
        System.out.println(timeReceived + " Server waiting for clients on port " + server.port);
        server.start();
    }

    private synchronized void broadcast(String message) {
            ChatFilter cf = new ChatFilter(this.fileName);
            String filteredMsg = cf.filter(message);
            String timeReceived = time.format(new Date());
            String messageWithTime = timeReceived + " " + filteredMsg;
            for (int i = 0; i < clients.size(); i++) {
                try {
                    clients.get(i).writeMessage(messageWithTime);
                } catch (NullPointerException e) {
                    System.out.print("");
                }
            }
            System.out.println(messageWithTime);
    }

    private boolean directMessage(String message, String recipient)
    {
        for (int i = 0; i < clients.size(); i++)
        {
            if (clients.get(i) != null && clients.get(i).username.equals(recipient))
            {
                ChatFilter cf = new ChatFilter(this.fileName);
                clients.get(i).writeMessage(cf.filter(message));
                System.out.println(cf.filter(message));
                return true;
            }
        }
        return false;
    }

    private void remove(int id) {
        synchronized (this) {
            close(clients.get(id));
            clients.set(id, null);
        }
    }

    private void close(ClientThread client) {
        try {
            client.sOutput.close();
            client.sInput.close();
            client.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            boolean start = true;
            for (int i = 0; i < clients.size() - 1; i++)
            {
                if (username.equals(clients.get(i).username))
                {
                    start = false;
                }
            }
            if (start) {
                String timeReceived = time.format(new Date());
                broadcast(username + " just connected.");
                System.out.println(timeReceived + " Server waiting for clients on port " + port + ".");
                while (clients.get(id) != null) {
                    // Read the username sent to you by client
                    try {
                        cm = (ChatMessage) sInput.readObject();
                        if (cm.getMsg().equals("/list")) {
                            String timeList = time.format(new Date());
                            System.out.println(timeList + " " + username + " used /list command.");
                            for (int i = 0; i < clients.size(); i++) {
                                if (clients.get(i) != null && !clients.get(i).username.equals(username)) {
                                    sOutput.writeObject(clients.get(i).username);
                                }
                            }
                        } else if (cm.getType() == 1) {
                            String timeClosed = time.format(new Date());
                            System.out.println(timeClosed + " " + username + " " + cm.getMsg());
                            remove(id);
                        } else if (cm.getType() == 2) {
                            String timeOfDirectMessage = time.format(new Date());
                            String dm = (timeOfDirectMessage + " " + username + " -> " + cm.getRecipient() + ": " +
                                    cm.getMsg());
                            boolean successful = directMessage(dm, cm.getRecipient()); //To recipient
                            if (successful) {
                                for (int i = 0; i < clients.size(); i++) {
                                    if (clients.get(i) != null && clients.get(i).username.equals(username)) {
                                        ChatFilter cf = new ChatFilter(fileName);
                                        clients.get(i).writeMessage(cf.filter(dm)); //To sender
                                    }
                                }
                            } else {
                                sOutput.writeObject("ERROR: User does not exist!");
                            }
                        } else {
                            //System.out.println(username + ": Ping");
                            String message = (username + " : " + cm.getMsg());
                            broadcast(message);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        remove(id);
                        String timeClosed = time.format(new Date());
                        System.out.println(timeClosed + " Connection with " + username + " has been terminated by the user.");
                    }

//                try {
//                    sOutput.writeObject("Pong");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                }
            } else
            {
                try {
                    sOutput.writeObject("ERROR: Username already exists!");
                    remove(id);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        private boolean writeMessage(String msg) {
            if (this.socket.isConnected()) {
                try {
                    sOutput.writeObject(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }
        }
    }
}

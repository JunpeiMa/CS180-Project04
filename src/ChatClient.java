import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    private ChatClient(int port, String username)
    {
        this("localhost", port, username);
    }

    private ChatClient(String username)
    {
        this(1500, username);
    }

    private ChatClient()
    {
        this("Anonymous");
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            System.out.println("ERROR: Socket could not be created. Check to make sure the server has started and the" +
                    " port number matches the server.");
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException | NullPointerException e) {
            System.out.println("ERROR: Input and Output streams could not be created because Socket is not present.");
            return false;
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            if (socket.isConnected()) {
                if (msg.getType() == 1) {
                    sOutput.writeObject(new ChatMessage("disconnected with a LOGOUT message", 1, "Server"));
                    sOutput.close();
                    sInput.close();
                    socket.close();
                } else {
                    sOutput.writeObject(msg);
                }
            } else
            {
                System.out.println("Please close the program manually.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        Scanner s = new Scanner(System.in);
        String username = "Anonymous";
        int port = 1500;
        String server = "localhost";
        boolean startConnection = false;
        while (!startConnection) {
            if (args.length <= 3)
                startConnection = true;

            if (args.length >= 1) {
                username = args[0];
                System.out.println(username);
            }

            if (args.length == 3){
                server = args[2];
            }

            try {
                if (args.length >= 2) {
                    port = Integer.parseInt(args[1]);
                }
            } catch (Exception e) {
                startConnection = false;
            }

        }
        // Create your client and start it
        ChatClient client = new ChatClient(server, port, username);
        boolean testStart = client.start();
        if (!testStart)
        {
            return;
        } else {
            // Send an empty message to the server
            if (client.server.equals("localhost")) {
                System.out.println("Connection accepted localhost/127.0.0.1:" + client.port);
            } else {
                System.out.println("Connection accepted " + client.server + ":" + client.port);
            }
            while (s.hasNextLine()) {
                String message = s.nextLine();
                if (message.equalsIgnoreCase("/logout")) {
                    client.sendMessage(new ChatMessage(message, 1, "Server"));
                    return;
                } else {
                    String[] splitMessage = message.split(" ");
                    if (splitMessage != null && splitMessage[0].equals("/msg")) {
                        if (splitMessage.length == 1) {
                            System.out.println("ERROR: Please enter a username to direct message.");
                        } else {
                            String userTo = splitMessage[1];
                            if (userTo.equals(username)) {
                                System.out.println("ERROR: You cannot direct message yourself!");
                            } else if (splitMessage.length == 2) {
                                System.out.println("ERROR: You do not have a message to send.");
                            } else {
                                message = "";
                                for (int i = 2; i < splitMessage.length; i++) {
                                    message += (splitMessage[i] + " ");
                                }
                                client.sendMessage(new ChatMessage(message, 2, splitMessage[1]));
                            }
                        }
                    } else {
                        client.sendMessage(new ChatMessage(message, 0, "All"));
                    }
                }
            }
        }
    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            try {
                while(!socket.isClosed()) {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                    System.out.println();
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Server has closed the connection. Please close the client if necessary.");
                System.exit(0); //That one cursed line.
            }
        }
    }
}

//TODO: Handle server disconnect in Client


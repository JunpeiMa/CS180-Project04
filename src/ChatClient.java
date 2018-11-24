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
            e.printStackTrace();
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
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
            sOutput.writeObject(msg);
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
            String input = s.nextLine();
            String[] splitInput = input.split(" ");
            if (splitInput.length >= 2 && splitInput.length < 6) {
                if (splitInput[0].equals("java") && splitInput[1].equals("ChatClient")) {
                    startConnection = true;
                    if (splitInput.length >= 3) {
                        username = splitInput[2];
                    }
                    if (splitInput.length >= 4) {
                        port = Integer.parseInt(splitInput[3]);
                    }
                    if (splitInput.length == 5) {
                        server = splitInput[4];
                    }
                }
            }
            //Note: I had issues with implementing args, so I have my old code running in the meantime
            /*if (args.length <= 3)
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
            } */

        }
        // Create your client and start it
        System.out.println("Client created successfully"); //Used solely for testing.
        ChatClient client = new ChatClient(server, port, username);
        client.start();
        // Send an empty message to the server
        while(s.hasNextLine()){
            String message = s.nextLine();
            if (message.equalsIgnoreCase("/logout"))
            {
                client.sendMessage(new ChatMessage(message, 1));
            } else {
                client.sendMessage(new ChatMessage(message, 0));
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
                while(true) {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}


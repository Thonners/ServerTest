package servertest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SingleThreadedServer class, code lifted directly from:
 * http://tutorials.jenkov.com/java-multithreaded-servers/singlethreaded-server.html
 * 
 * @author M Thomas
 * @since 25/12/16
 */
public class SingleThreadedServer implements Runnable {
    
    protected int          serverPort   = 8080; // Seems to be overwritten on instance creation - default must be for overridden classes
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    
    private int connectionCount = -1;
    File countFile = new File ("/home/mathonwy/ServerProjects/tempFile") ;

    /**
     * Constructor
     * @param port The port on which to listen for incoming connections
     */
    public SingleThreadedServer(int port){
        this.serverPort = port;
    }

    /**
     * The main method to be run once connected.
     * 
     * Sets the thread, and opens the ServerSocket to listen on the serverPort.
     * Then runs until closed, trying to open a connection when a client connects.
     */
    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        createCountFile() ;
        openServerSocket();
        
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            try {
                processClientRequest(clientSocket);
            } catch (IOException e) {
                //log exception and go on to next request.
            }
        }
        
        System.out.println("Server Stopped.");
    }

    /**
     * Method to manage the connection, once a client connects to the server.
     * @param clientSocket The socket of the client
     * @throws IOException 
     */
    private void processClientRequest(Socket clientSocket)
    throws IOException {
        // Increment connection count
        incrementConnectionCount();
        System.out.println("This server has been connected to " + connectionCount + " times.");
        // Input / Output data streams
        DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream dOut = new DataOutputStream(clientSocket.getOutputStream()) ;
        
        boolean done = false;
        while(!done) {
            byte messageType = dIn.readByte();

            switch(messageType) {
                case -1: //Client is a receive type client, and is therefore only after data
                    dOut.writeByte(-1);
                    dOut.writeUTF("This server session has received: " + --connectionCount + " send-type client connections."); // Reduce connection count, as we're only interested in reporting send type client connections
                    dOut.flush();
                    break ;
                case 1: // Type A
                    System.out.println("Message A: " + dIn.readUTF());
                    break;
                case 2: // Type B
                    System.out.println("Message B: " + dIn.readUTF());
                    break;
                case 3: // Type C
                    System.out.println("Message C [1]: " + dIn.readUTF());
                    System.out.println("Message C [2]: " + dIn.readUTF());
                    break;
                default:
                    done = true;
            }
        }

        System.out.print("Closing sockets... ");
        dOut.close();
        dIn.close();
        clientSocket.close();
        System.out.println("Done.");
        
    }

    /**
     * @return Whether the server is still listening, or is stopped
     */
    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    /**
     * Method to stop listening on the serverPort and to close the socket.
     */
    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    /**
     * Method to try creating an instance of a ServerSocket, attached to the
     * serverPort.
     */
    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + this.serverPort, e);
        }
    }
    
    private void createCountFile() {
        // Create the file if it doesn't already exist
        try {
            countFile.createNewFile() ;
        } catch (IOException e) {
            System.out.println("Error creating file: " + countFile.getAbsolutePath()) ;
            e.printStackTrace();
        }
    }
    
    private void incrementConnectionCount() {
        // Read and add to the previous connection count as stored in the file
        try (BufferedReader in = new BufferedReader(new FileReader(countFile))) {
            // Read the file
            String text ; 
            // Check file has content
            if ((text = in.readLine()) == null) {
                // If not, assume it's the first time the server has created this file
                connectionCount = 1 ;
            } else {
                // Otherwise, read the count
                try {
                    int countRead = Integer.parseInt(text) ;
                    System.out.println("Previous count = " + countRead) ;
                    connectionCount = ++countRead ;
                    System.out.println("Current count = " + connectionCount) ;
                } catch (Exception e) {
                    System.out.println("Error parsing int in file. Text = " + text) ;
                    e.printStackTrace();                
                }       
            }
        } catch (IOException e) {
            System.out.println("Error creating BufferedReader for : " + countFile.getAbsolutePath()) ;
            e.printStackTrace();
        } 
        
        // Write the value back for next time (try-with-resources) much shorter!
        try (BufferedWriter out = new BufferedWriter(new FileWriter(countFile)) ){
            // Write the connection count
            out.write(connectionCount + "");
            // No need to close the BW in a try-with-resources block
        }  catch (IOException e) {
            System.out.println("Error creating BufferedWriter for : " + countFile.getAbsolutePath()) ;
            e.printStackTrace();
        }       
    }
}

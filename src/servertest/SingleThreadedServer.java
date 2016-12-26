package servertest;

import java.io.DataInputStream;
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
        InputStream  input  = clientSocket.getInputStream();
        OutputStream output = clientSocket.getOutputStream();
        // Fancy way of receiving data
        DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());
        
        
        boolean done = false;
        while(!done) {
            byte messageType = dIn.readByte();

            switch(messageType) {
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

        long time = System.currentTimeMillis();

        System.out.print("Writing output... ");
        output.write(("HTTP/1.1 200 OK\n\n<html><body>" +
                "Singlethreaded Server: " +
                time +
                "</body></html>").getBytes());
        output.close();
        input.close();
        System.out.println("Request processed: " + time);
        
        
        System.out.print("Closing dIn... ");
        dIn.close();
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
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }
}

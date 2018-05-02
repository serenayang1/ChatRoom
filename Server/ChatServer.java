import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.ArrayList;

public class  ChatServer
{
	public static final int DEFAULT_PORT = 9889;

    // construct a thread pool for concurrency	
	private static final Executor exec = Executors.newCachedThreadPool();
	
	public static void main(String[] args) throws IOException {
		ServerSocket sock = null;
        ArrayList<String> usernames = new ArrayList<String>();
        ArrayList<Socket> sockets = new ArrayList<Socket>();
		
		try {
			// establish the socket
			sock = new ServerSocket(DEFAULT_PORT);
			
			while (true) {
				/**
				 * now listen for connections
				 * and service the connection in a separate thread.
				 */
				Runnable task = new Connection(sock.accept(),usernames,sockets);
				exec.execute(task);
			}
		}
		catch (IOException ioe) { System.err.println(ioe); }
		finally {
			if (sock != null)
				sock.close();
		}
	}
}


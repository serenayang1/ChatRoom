/**
 * This thread is passed a socket that it reads from. Whenever it gets input
 * it writes it to the ChatScreen text area using the displayMessage() method.
 */

import java.io.*;
import java.net.*;
import javax.swing.*;

public class ReaderThread implements Runnable
{
	Socket server;
	BufferedReader fromServer;
	ChatClient screen;

	public ReaderThread(Socket server, ChatClient screen) {
		this.server = server;
		this.screen = screen;
	}

    public int findChar(char c, int pos, String s) {
        int index = pos;
        
        while (s.charAt(pos) != c && index < s.length())
            pos++;
        
        //System.out.println("request = " + s + " pos = " + pos);
        return pos;
    }
    
    public void run() {
		try {
			fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));

			while (true) {
				
				String line = fromServer.readLine();
                String message = null;
                String status_code = null;
                String Receiver = null;
                String Sender = null;
                int length;
                String text = null;
                
				// now display it on the display area
				if (line.substring(0, 11).equals( "STATUS_CODE")){
                    int firstBlank = findChar(' ', 0, line);
                    status_code = line.substring(firstBlank+1,firstBlank+2);
                    if(status_code.equals("0")){
                        message = "Username already exist.";
                        System.exit(0);
                        server.close();
                        
                    }
                    else if(status_code.equals("1")){
                        message = "Successfully join the chatroom.";
                    }else{
                        message = "Failed to send the message. User already left";
                    }
                }
				else if (line.substring(0, 4).equals("PING")){
                    int firstBlank = findChar(' ', 0, line);
                    int secondBlank = findChar(' ', firstBlank + 1, line);
                    int thirdBlank = findChar(' ', secondBlank + 1, line);
                    int fourthBlank = findChar(' ', thirdBlank + 1, line);
                    
                    Receiver = line.substring(firstBlank+1,secondBlank);
                    Sender = line.substring(secondBlank+1,thirdBlank);
                    length = Integer.parseInt(line.substring(thirdBlank+1,fourthBlank));
                    text = line.substring(fourthBlank+1,fourthBlank+1+length);
                    
                    message = Sender+" to "+ Receiver+" : "+text;
                }
                screen.displayMessage(message);
			}
		}
		catch (IOException ioe) { System.out.println(ioe); }

	}
}

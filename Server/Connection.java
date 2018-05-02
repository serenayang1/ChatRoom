/**
 * This is the separate thread that services each
 * incoming echo client request.
 *
 * 
 */

import java.net.*;
import java.io.*;
import java.util.*;


public class Connection implements Runnable
{
	private Socket client;
    private ArrayList<String> usernames = new ArrayList<String>();
    private ArrayList<Socket> sockets = new ArrayList<Socket>();

	public Connection(Socket client,ArrayList<String> usernames,ArrayList<Socket> sockets) {
		this.client = client;
        this.usernames = usernames;
        this.sockets = sockets;
	}

    /**
     * This method runs in a separate thread.
     */	
	public void run() { 
		try {
			process(client,usernames,sockets);
		}
		catch (java.io.IOException ioe) {
			System.err.println(ioe);
		}
	}
    
    public static final int BUFFER_SIZE = 256;
    
    /**
     * Finds the first occurrence of the character c
     * beginning at the specified position in String s.
     * Returns the index of the character c.
     */
    public int findChar(char c, int pos, String s) {
        int index = pos;
        while (s.charAt(pos) != c && index < s.length())
            pos++;
        return pos;
    }

    
    /**
     * this method is invoked by a separate thread
     */
    public void process(Socket client,ArrayList<String> usernames,ArrayList<Socket> sockets) throws java.io.IOException {
        BufferedReader in = null;
        DataOutputStream out = null;
        String status_code = null;
        String username = null;
        int name_len;
        String receiver;
        int message_len;
        String message;
        
        try {
            while(true){
                // read what the client sent
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                
                // we just want the first line
                String requestLine = in.readLine();
                
                System.out.println(">>request<<" + requestLine+"<<");
                
                /*EXIT the chatroom */
                if(requestLine.substring(0,4).equals("EXIT")){
                    sockets.remove(client);
                    usernames.remove(username);
                    
                    for(int i = 0;i<sockets.size();i++){
                        out = new DataOutputStream((sockets.get(i)).getOutputStream());
                        receiver = usernames.get(i);
                        message = username+" left chatroom.";
                        message_len = message.length();
                        out.writeBytes("PING "+ "ALL " + "System "+message_len+" "+message+"\n");
                        out.flush();
                    }

                    System.out.println(">>Names:<<" + usernames);
                    client.close();
                    return;
                }

                /* JOIN the chatroom */
                else if (requestLine.substring(0,4).equals("JOIN") ) {
                    out = new DataOutputStream(client.getOutputStream());
                    int firstBlank = findChar(' ', 0, requestLine);
                    int secondBlank = findChar(' ', firstBlank + 1, requestLine);

                    name_len = Integer.parseInt(requestLine.substring(firstBlank+1, secondBlank));
                    username = requestLine.substring(secondBlank+1,secondBlank+1+name_len);
                    if(usernames.contains(username)){/*username already exist*/
                        status_code = "0";
                        out.writeBytes("STATUS_CODE "+status_code+"\n");
                    }else{/*valid username*/
                        status_code = "1";
                        out.writeBytes("STATUS_CODE "+status_code+"\n");
                        usernames.add(username);
                        sockets.add(client);
                        
                        for(int i = 0;i<sockets.size();i++){
                            out = new DataOutputStream((sockets.get(i)).getOutputStream());
                            receiver = usernames.get(i);
                            message = username+" has joined chatroom.";
                            message_len = message.length();
                            out.writeBytes("PING "+ "ALL " + "System "+message_len+" "+message+"\n");
                            out.flush();
                        }
                    }
                    out.flush();
                    System.out.println(">>Names:<<" + usernames);
                }
                /* WHISPER sending direct message */
                else if(requestLine.substring(0,7).equals("WHISPER")){
                    
                    int firstBlank = findChar(' ', 0, requestLine);
                    int secondBlank = findChar(' ', firstBlank + 1, requestLine);
                    int thirdBlank = findChar(' ',secondBlank + 1, requestLine);
                    
                    receiver = requestLine.substring(firstBlank+1, secondBlank);
                    message_len = Integer.parseInt(requestLine.substring(secondBlank+1,thirdBlank));
                    message = requestLine.substring(thirdBlank+1, thirdBlank+1+message_len);
                    if(usernames.contains(receiver)){
                        int index = usernames.indexOf(receiver);
                        out = new DataOutputStream((sockets.get(index)).getOutputStream());
                        out.writeBytes("PING "+receiver+" "+username+" "+message_len+" "+message+"\n");
                        out.flush();
                    }else{
                        out = new DataOutputStream(client.getOutputStream());
                        status_code = "2";
                        out.writeBytes("STATUS_CODE "+status_code+"\n");
                        out.flush();
                    }
                }
                /* SHOUTOUT broadcast message */
                else if(requestLine.substring(0,8).equals("SHOUTOUT")){
                    int firstBlank = findChar(' ', 0, requestLine);
                    int secondBlank = findChar(' ', firstBlank + 1, requestLine);
                    message_len = Integer.parseInt(requestLine.substring(firstBlank+1, secondBlank));
                    message = requestLine.substring(secondBlank+1, secondBlank+1+message_len);
                    for(int i = 0;i<sockets.size();i++){
                        if(sockets.get(i) != client){
                            out = new DataOutputStream((sockets.get(i)).getOutputStream());
                            receiver = usernames.get(i);
                            out.writeBytes("PING "+"ALL "+username+" "+message_len+" "+message+"\n");
                            out.flush();
                        }
                    }
                }

                
                
            }
        }
        catch (IOException ioe) {
            System.err.println(ioe);
        }
        finally {
        }
    }
}


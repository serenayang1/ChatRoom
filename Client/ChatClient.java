import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

public class ChatClient extends JFrame implements ActionListener, KeyListener{
	public static final int PORT = 9889;
	//chat screen------------
	private JTextField reciever;
	private JButton broadcastButton;
	private JButton sendButton;
	private JButton exitButton;
	private JTextField sendText;
	private static JTextArea displayArea;
	
	public static class variable{
		public static String data = null;
		public static String recieverName = null;
		public static String name = null;
		public static String header = "JOIN";
		public static String length = null;
		public static String request = null;
		public static int flag = 1;
		DataOutputStream out = null;
	}
	
	public ChatClient() {
		
		/**
		 * a panel used for placing components
		 */
		JPanel p = new JPanel();

		Border etched = BorderFactory.createEtchedBorder();
		Border titled = BorderFactory.createTitledBorder(etched, "Enter Receiver's Name & Message Here ...");
		p.setBorder(titled);

		/**
		 * set up all the components
		 */
		sendText = new JTextField(30);
		reciever = new JTextField(10);
		broadcastButton = new JButton("Boardcast");
		sendButton = new JButton("Send");
		exitButton = new JButton("Exit");

		/**
		 * register the listeners for the different button clicks
		 */
        broadcastButton.addActionListener(this);
        sendText.addKeyListener(this);
		sendButton.addActionListener(this);
		exitButton.addActionListener(this);

		/**
		 * add the components to the panel
		 */
		p.add(reciever);
		p.add(sendText);
		p.add(sendButton);
		p.add(exitButton);
		p.add(broadcastButton);

		/**
		 * add the panel to the "south" end of the container
		 */
		getContentPane().add(p,"South");

		/**
		 * add the text area for displaying output. Associate
		 * a scrollbar with this text area. Note we add the scrollpane
		 * to the container, not the text area
		 */
		displayArea = new JTextArea(15,40);
		displayArea.setEditable(false);
		displayArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

		JScrollPane scrollPane = new JScrollPane(displayArea);
		getContentPane().add(scrollPane,"Center");

		/**
		 * set the title and size of the frame
		 */
		setTitle("Chat Client");
		pack();
 
		setVisible(true);
		sendText.requestFocus();

		/** anonymous inner class to handle window closing events */
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				System.exit(0);
			}
		} );

	}
	public void displayText() {
        String message = sendText.getText().trim();
        String id = variable.name;
        StringBuffer buffer = new StringBuffer(message.length());
        buffer.append(id + ": ");
        for (int i = 0; i < message.length(); i++)
            buffer.append(message.charAt(i));
        displayArea.append(buffer.toString() + "\n");
        sendText.setText("");
        sendText.requestFocus();
        reciever.setText("");
        reciever.requestFocus();
    }
	
	public void displayMessage(String message){
		displayArea.append(message + "\n");
	}
	
	public void keyTyped(KeyEvent e) {	
	}
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
            displayText();
	}
	public void keyReleased(KeyEvent e) {
	}
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		//statements for press button actions
		if (source == sendButton){
			variable.header = "WHISPER";
			variable.data = sendText.getText().trim();
			variable.recieverName = reciever.getText().trim();
	        int temp = variable.data.length();
	        variable.length = Integer.toString(temp);
			displayText();	
			variable.request = variable.header + " " + variable.recieverName + " " + variable.length + " " + variable.data + "\n";
			variable.flag ++;
		}
		else if (source == broadcastButton){
			variable.header = "SHOUTOUT";
			variable.data = sendText.getText().trim();
	        int temp = variable.data.length();
	        variable.length = Integer.toString(temp);
			displayText();
			variable.request = variable.header + " " + variable.length + " " + variable.data + "\n";
			variable.flag ++;
		}
		else if (source == exitButton)
			variable.request = "EXIT";
	}
	//connect socket
	public static void main(String[] args) throws IOException{
		if(args.length != 2){
			System.err.println("usage: java client <ip address> <user name>");
			System.exit(0);
		}
		BufferedReader in = null;
		DataOutputStream out = null;
		Socket sock = null;	
		
		try {
			sock = new Socket(args[0], PORT);
			out = new DataOutputStream(sock.getOutputStream());
			variable.name = args[1];
			variable.length = Integer.toString(variable.name.length());
			variable.request = variable.header + " " + variable.length + " " + variable.name + "\n";
			//open gui
			ChatClient win = new ChatClient();
			Thread ReaderThread = new Thread(new ReaderThread(sock, win));
			ReaderThread.start();
			while(true){
				//only write when a button is pressed
				if(variable.flag == 1){
					out.writeBytes(variable.request);
					variable.flag--;
					out.flush();
					}
				else if (variable.request.equals("EXIT")){
					out.writeBytes(variable.request);
					out.flush();
					System.exit(0);
				}
				//update request
				System.getenv(variable.request);
			}
	}
		catch (IOException ioe) {
			System.err.println(ioe);
		}
	}
}

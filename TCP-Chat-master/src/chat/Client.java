package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	
	private int port = 25565;
	private boolean connected = false;
	
	public Client(String ip,String name) {
		
		try(Socket Server = new Socket(ip,port)) {
			DataInputStream inbox = new DataInputStream(Server.getInputStream());
			DataOutputStream outbox = new DataOutputStream(Server.getOutputStream());
			connected = true;
			
			readInbox(inbox);
			
			while(connected) {
				Scanner console = new Scanner(System.in);
				outbox.writeUTF(name + ": " + console.nextLine());
			}
		
		} catch(Exception ex) {
			System.out.println("Client write error");
			ex.printStackTrace();
		}
	}
	
	private void readInbox(DataInputStream inbox) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while(connected) {
					try {
						String message = inbox.readUTF();
						if(message.endsWith("end")) {
							connected = false;
						} else {
							System.out.println(message);
						}
					} catch(Exception ex) {
						System.out.println("Client read error");
						ex.printStackTrace();
					}
				}
			}
		}).start();

	}
}

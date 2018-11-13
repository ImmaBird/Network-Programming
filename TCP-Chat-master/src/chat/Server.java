package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/** 
 * The main job of the server is to relay data between all of the clients during the game
 * The server is also responsible for organizing the users
 */
public class Server {
	
	private int port = 25565;
	private boolean connected = false;
	
	public Server(String name) {
		
		try(ServerSocket welcomeSocket = new ServerSocket(port)) {
			Socket client = welcomeSocket.accept();
			DataInputStream inbox = new DataInputStream(client.getInputStream());
			DataOutputStream outbox = new DataOutputStream(client.getOutputStream());
			connected = true;
			
			readInbox(inbox);
			
			while(connected) {
				Scanner console = new Scanner(System.in);
				outbox.writeUTF(name + ": " + console.nextLine());
			}
		
		} catch(Exception ex) {
			System.out.println("Server write error");
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
						System.out.println("Server read error");
						ex.printStackTrace();
					}
				}
			}
		}).start();

	}
}
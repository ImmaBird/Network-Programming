package chat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/** 
 * The main job of the server is to relay data between all of the clients during the game
 * The server is also responsible for organizing the users
 */
public class Server {
	
	private boolean connected = false;
	
	public Server(String ip, String name) {
		
		try(DatagramSocket Server = new DatagramSocket()) {
			byte[] buffer = new byte[65508];
			InetAddress address = InetAddress.getByName(ip);
			DatagramPacket packet = new DatagramPacket(buffer,buffer.length,address,25564);
			connected = true;
			
			readInbox();
			
			Scanner console = new Scanner(System.in);
			while(connected) {
				packet.setData((name+": "+console.nextLine()).getBytes());
				Server.send(packet);
			}
			
		console.close();
		} catch(Exception ex) {
			System.out.println("Server write error");
			ex.printStackTrace();
		}
	}
	
	private void readInbox() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try(DatagramSocket Client = new DatagramSocket(25565)) {
					while(connected) {
						byte[] buffer = new byte[65508];
						DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
						Client.receive(packet);
						String message = new String(packet.getData()).trim();
						if(message.endsWith("end")) {
							connected = false;
						} else {
							System.out.println(message);
						}
					}
				} catch(Exception ex) {
					System.out.println("Client read error");
					ex.printStackTrace();
				}
			}
		}).start();

	}
}
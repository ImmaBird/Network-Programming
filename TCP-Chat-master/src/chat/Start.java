package chat;

import java.util.Scanner;

public class Start {

	public static void main(String[] args) {
		try(Scanner console = new Scanner(System.in)) {
			System.out.println("Hello Welcome to Chat.\nChat is a state of the art communication network that lets you talk to one person at a time!");
			System.out.println("Server or Client (s/c)?");
			switch(console.nextLine()) {
			case "s":
				System.out.println("What do you call yourself?");
				String serverName = console.nextLine();
				new Server(serverName);
				break;
			case "c":
				System.out.println("What IP?");
				String ip = console.nextLine();
				System.out.println("What do you call yourself?");
				String clientName = console.nextLine();
				new Client(ip,clientName);
				break;
			}
		}

	}

}

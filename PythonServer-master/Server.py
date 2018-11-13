import sys
import socket
import time
from threading import Thread


class Server:

    def __init__(self):
        # Flag to determine if the server is still running (False = terminated)
        self.is_active = True

        # The clients that are connected to the server
        self.clients = []

        # The port the server is bound to
        self.port = None

    def start(self, port):
        self.port = port
        Thread(target=self.server_thread).start()

    def server_thread(self):
        # Create welcome socket
        welcome_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        # Binds the welcome socket to the specified port
        # socket.gethostname() specifies that the server can be found by anyone
        welcome_socket.bind(("", self.port))

        # Specifies a buffer size of 5 that holds incoming connections while they are being processed
        welcome_socket.listen(5)

        # Accepts new clients trying to connect to the server
        while self.is_active:
            # Accepts a client
            (client_socket, address) = welcome_socket.accept()

            # Handles the client on a new thread
            Thread(target=self.handle_client, args=[client_socket]).start()

    def handle_client(self, client_socket):
        # Creates a new instance of client
        client = self.Client()
        client.client_socket = client_socket

        # Prompts the user for a name
        client.send_string("Enter your name: ")
        client.name = client.receive_string()

        # Appends the client to the list of clients
        self.clients.append(client)

        # Manages the clients inputs
        while True:
            message = client.receive_string()

            # If the message is not empty nor signals an exit then it is processed
            if message == "" or message == "/exit":
                exit_message = "/SERVER: \""+client.name+"\" has disconnected."
                print(exit_message)
                break
            elif message[0:0] == "/":
                {
                    # TODO add commands for the server
                    "/room"
                }
            else:
                print(client.name + message)


        # Once the client disconnects remove them from the list
        client_array_length = len(self.clients)
        old_clients = self.clients
        self.clients = []
        for i in range(0, client_array_length):
            if old_clients[i].id != client.id:
                self.clients.append(old_clients[i])

    class Client:

        # Ensures a unique id is given to each client
        client_id_iterator = 1

        def __init__(self):
            # Flag to mark the disposal of a client (False = delete)
            self.is_active = True

            # Sets up the clients unique id
            self.id = Server.Client.client_id_iterator
            Server.Client.client_id_iterator += 1

            # Stores the clients nickname
            self.name = None

            # Stores the clients socket
            self.client_socket = None

        def send_string(self, message):
            # Get message length
            length = len(message)

            # Error check message length
            if length > 0xffff:
                print("Message too large, returning")
                return

            # Convert length to bytes
            length_bytes = int.to_bytes(length, byteorder="big", length=2, signed=False)

            # Send message length represented by a fixed 2 byte header
            self.client_socket.sendall(length_bytes)

            # Encodes the message into utf-8 format
            message = message.encode(encoding="utf-8")

            # Sends the message to the host
            self.client_socket.sendall(message)

            # For debugging
            print("Sent length in bytes: " + str(length_bytes))
            print("Integer length representation: " + str(length))

        def receive_string(self):
            # Gets a single byte, this byte determines the size of the following message
            try:
                length_in_bytes = self.client_socket.recv(2)
            except:
                return "/exit"

            length = int.from_bytes(length_in_bytes, byteorder="big", signed=False)

            # Waits for the rest of the message to follow, hopefully tcp does its work
            message = ""
            total_received = 0
            while total_received < length:
                message += self.client_socket.recv(length - total_received).decode(encoding="utf-8")
                total_received += len(message)

            return message

server = Server()
server.start(7778)

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace ConsoleApplication1
{
    class Client
    {
        
        public static void Main()
        {
            Client c = new Client();
            c.connect("73.69.115.106", 7778);
        }

        private volatile Boolean isConnected;
        private TcpClient client;
        private volatile NetworkStream clientStream;

        public Client()
        {
            client = new TcpClient();
        }

        // Connectes the client to the server
        public void connect(String ip, int port)
        {
            isConnected = true;
            client.Connect(ip, port);
            clientStream = client.GetStream();
            new Thread(new ThreadStart(this.handleRead)).Start();
            handleInput();
        }

        private void write(String message)
        {
            // Creates a byte array to hold the message
            byte[] bytes = new byte[message.Length+2];

            // Encodes the message string
            Encoding.UTF8.GetBytes(message).CopyTo(bytes, 2);

            // Finds the message length and stores it in 2 bytes (Message cannot be large than 0xFF-FF)
            byte[] lengthInBytes = BitConverter.GetBytes((short)message.Length);

            // If the current machine is little endian flip to bits to adhear to network standards
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(lengthInBytes);
            }

            // Add the length the the beginning of the message
            lengthInBytes.CopyTo(bytes, 0);

            // Adds the message to the write buffer
            clientStream.Write(bytes, 0, message.Length+2);

            // Flushes the buffer
            clientStream.Flush();
        }

        private void handleInput()
        { 
            while (isConnected)
            {
                String input = Console.In.ReadLine();
                if (input.Substring(0, 1).Equals("/"))
                {
                    switch (input)
                    {
                        case "/help":
                            Console.Out.WriteLine("/help - Prints this list of commands.");
                            Console.Out.WriteLine("/exit - Exits the chat session.");
                            break;
                        case "/exit":
                            write("/exit");
                            isConnected = false;
                            break;
                        default:
                            Console.Out.WriteLine("Type /help for a list of commands.");
                            break;
                    }
                }
                write(input);
            }
        }

        private void handleRead()
        {
            while (isConnected)
            {
                // Reads in first two bytes to determine message size
                byte[] buffer = new byte[2];
                clientStream.Read(buffer, 0, 2);

                // Flips the bits in the buffer if on a little endian machine
                if (BitConverter.IsLittleEndian)
                {
                    Array.Reverse(buffer);
                }

                // Gets the message length
                int messageLength = BitConverter.ToInt16(buffer, 0);
                Console.Out.WriteLine(messageLength);

                // Creates a new buffer for the message
                buffer = new byte[messageLength];

                // Gets the rest of the message
                clientStream.Read(buffer, 0, messageLength);
                String message = Encoding.UTF8.GetString(buffer);

                // If the message is empty close connection
                if (message.Equals(""))
                {
                    isConnected = false;
                    clientStream.Close();
                    Console.Out.WriteLine("Disconnected from server.");
                    Console.Out.WriteLine("Press any key to exit...");
                    Console.In.Read();
                }else
                {
                    Console.Out.WriteLine(message);
                }
            }
        }
    }
}

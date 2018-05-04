package pkg;

import java.net.*;
import java.io.*;

public class CommChar {

	// Specify a file
	static void transferFile(Comms comms) throws IOException, URISyntaxException {
		// Specify the file
		URL url = CommChar.class.getResource("TEXT.txt");
		File file = new File(url.toURI());
		if (file.exists() && !file.isDirectory()) {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);

			// Get socket's output stream
			OutputStream os = comms.outStream;

			// Read File Contents into contents array
			byte[] contents;
			long fileLength = file.length();
			long current = 0;

			long start = System.nanoTime();
			while (current != fileLength) {
				int size = 10000;
				if (fileLength - current >= size)
					current += size;
				else {
					size = (int) (fileLength - current);
					current = fileLength;
				}
				contents = new byte[size];
				bis.read(contents, 0, size);
				os.write(contents);
				System.out.print("Sending file ... " + (current * 100) / fileLength + "% complete!");
			}

			os.flush();
			System.out.println("File sent succesfully!");
		} else {
			System.out.println("File TEXT.txt not exist !");
		}

	}

	static void serverApp(Comms comms) throws Exception {
		// This server App sends typed characters to client.
		InputStreamReader in = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(in);
		String sendChar;
		transferFile(comms);
		System.out.println("\nEnter characters (q to exit):");

		do {
			sendChar = br.readLine();
			// System.out.println ("\nEnter ch"+sendChar); //for debugging
			comms.writeString(sendChar);
			StringBuffer receiveChar = comms.readString();
			System.out.print(receiveChar);
		} while (!sendChar.equals("q"));
	}

	static void saveFile(Comms comms) throws IOException, URISyntaxException {
		// Initialize socket
		byte[] contents = new byte[10000];
		// Initialize the FileOutputStream to the output file's full path.
		FileOutputStream fos = new FileOutputStream(".\\src\\pkg\\TEXT.txt");
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		InputStream is = comms.inStream;

		if (is.available() > 0) {
			// No of bytes read in one read() call
			int bytesRead = 0;

			while ((bytesRead = is.read(contents)) != -1)
				bos.write(contents, 9, bytesRead);

			bos.flush();

			System.out.println("File saved successfully!");
		}
	}

	static void clientApp(Comms comms) throws Exception {
		// This client App receives the characters.
		StringBuffer receiveChar;
		
		System.out.print("\nCharacters received:\r\n");
		do {
			receiveChar = comms.readString();
			System.out.print(receiveChar);
			comms.writeString("ACK");
		} while (receiveChar.charAt(receiveChar.length() - 1) != 'q');
		saveFile(comms);
	}

	public static void main(String[] args) throws Exception {
		Comms comms;
		boolean isServer; // Server or Client?
		String serverName; // Host name of the server to connect to
		int outOfError; // Error on transfer per character is 1 : outOfError
		// Create an InputStreamReader for reading characters from byte stream
		// System.in
		InputStreamReader inStreamReader = new InputStreamReader(System.in);
		// Create a BufferedReader for reading entire lines of text from the
		// InputStreamReader
		BufferedReader inputReader = new BufferedReader(inStreamReader);
		// Ask the user if this is the server
		System.out.print("Is this the server (yes/no)? ");
		if (inputReader.readLine().equalsIgnoreCase("yes")) {
			isServer = true;
		} else {
			isServer = false;
		}
		// Ask the user for the host name of the server
		if (isServer) {
			System.out.println("The error rate is 1:N characters. N=0 will produce no errors, 2=50%, 10=10%, 20=5%");
			outOfError = Keyboard.readInt("Please enter N: ");
			System.out.println("\nWaiting for connection with client...");
			serverName = " ";
		} else {
			System.out.print("Please enter the host name of server: ");
			serverName = inputReader.readLine();
			outOfError = 0;
		}
		// Create communications object
		try {
			comms = new Comms(serverName, 2569, isServer, 9600, outOfError);
		} catch (IOException e) {
			System.out.println("An I/O error occurred.");
			comms = null;
		}
		// Establish connection
		// * If this is the server then comms.connect () waits for a client to
		// connect.
		// * If this is the client then comms.connect () tries to connect to the
		// server.
		if ((comms != null) && (comms.connect())) {
			System.out.println("Connected.");
			if (comms.isServer) {
				serverApp(comms);
			} else {
				clientApp(comms);
			}
			System.out.print("\r\nTransfer complete!");
		} else {
			System.out.println("Connection Failed.");
		}

	}
}

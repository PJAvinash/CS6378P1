import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException{
        String filePath = args[0];
        ArrayList<Integer> uidlist = new ArrayList<>();
        ArrayList<String> hostnames = new ArrayList<>();
        ArrayList<Integer> portnumbers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Ignore lines starting with #
                if (!line.startsWith("#")) {
                    String[] parts = line.trim().split("\\s+");
                    // Ensure the line has at least three parts
                    if (parts.length >= 3) {
                        try {
                            // Extract and convert UID and port to integers
                            int uid = Integer.parseInt(parts[0]);
                            int port = Integer.parseInt(parts[2]);

                            // Add UID, hostname, and port to respective lists
                            uidlist.add(uid);
                            hostnames.add(parts[1]);
                            portnumbers.add(port);
                        } catch (NumberFormatException e) {
                            // Handle parsing errors
                            System.err.println("Error parsing UID or port: " + line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int numNodes = uidlist.size();
        InetAddress ip = InetAddress.getLocalHost();
        String hostname = ip.getHostName();
        int index =  hostnames.indexOf(hostname);
        int uid = uidlist.get(index);
        int port = portnumbers.get(index);
        Node processingNode = new Node(uid,hostname,port,numNodes);
        for(int i = 0; i< numNodes ; i++){
            if(i != index){
                processingNode.addNeighbor(uidlist.get(i), hostnames.get(i), portnumbers.get(i));
            }
        }
        processingNode.runCausalBroadcast(20);
    }
}

// import java.util.Arrays;

// public class Main {
//     public static void main(String[] args) {
//         // Create test array of Message objects
//         Message[] messages = {
//             new Message(2, new int[]{101, 101, 37, 101}, MessageType.BROADCAST, "content1"),
//             new Message(2, new int[]{101, 101, 39, 101}, MessageType.BROADCAST, "content2"),
//             new Message(2, new int[]{101, 101, 38, 101}, MessageType.BROADCAST, "content3")
//         };

//         // Sort the array of messages
//         Arrays.sort(messages);

//         // Print the sorted array
//         for (Message message : messages) {
//             System.out.println(message);
//         }
//     }
// }
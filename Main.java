import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

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
        System.err.println(hostname);
        List<Node> hostProcs = new ArrayList<Node>();
        for(int i = 0; i< numNodes;i++){
            if(hostnames.get(i).equals(hostname)){
                hostProcs.add(new Node(uidlist.get(i),hostname,portnumbers.get(i),numNodes));
            }
        }
        for(int i = 0; i < numNodes; i++){
            for(Node n: hostProcs){
                if(i != n.getUID()){
                    n.addNeighbor(uidlist.get(i), hostnames.get(i), portnumbers.get(i));
                }
            }
        }
        System.out.println("starting"+hostProcs.size());
        hostProcs.forEach(t-> t.runCausalBroadcast(100));
        boolean procisactive = true;
        while(procisactive){
            procisactive = false;
            for(Node hp: hostProcs){
                if(hp.getState() == NodeState.Running){
                    procisactive = true;
                }
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        System.exit(0);
    }
}
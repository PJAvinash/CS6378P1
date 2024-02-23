
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

public class Node {
    private int numProc;
    private String hostname;
    private int uid;
    private int port;
    private NodeState state;
    private int[] vectorclock;
    private final ReadWriteLock timestamplock = new ReentrantReadWriteLock();
    private ArrayList<AdjNode> adjacentNodes = new ArrayList<AdjNode>();

    private List<Message> bufferedMessages = Collections.synchronizedList(new ArrayList<Message>());
    private List<Message> deliveredMessages = Collections.synchronizedList(new ArrayList<Message>());
    private Set<Integer> terminationFrom = Collections.synchronizedSet(new HashSet<>());

    public Node(int uid, String hostname, int port, int numProc) {
        this.uid = uid;
        this.hostname = hostname;
        this.port = port;
        this.numProc = numProc;
        this.vectorclock = new int[numProc];
        this.state = NodeState.Running;
        for (int i = 0; i < numProc; i++) {
            vectorclock[i] = 0;
        }
        this.createLogFile();
    }

    private synchronized void updateClock(int from) {
        timestamplock.writeLock().lock();
        try {
            this.vectorclock[from] = this.vectorclock[from] + 1;

        } finally {
            timestamplock.writeLock().unlock();
        }

    }

    public int[] getVectorClock() {
        timestamplock.readLock().lock();
        try {
            return vectorclock.clone(); // Return a copy to ensure immutability
        } finally {
            timestamplock.readLock().unlock();
        }
    }

    public void addNeighbor(int uid, String hostName, int port) {
        this.adjacentNodes.add(new AdjNode(uid, hostName, port));
    }

    public boolean isCausallyReady(int[] messageTimestamp) {
        timestamplock.readLock().lock();
        boolean returnval = true;
        for (int i = 0; i < this.vectorclock.length && returnval; i++) {
            if (vectorclock[i] < messageTimestamp[i]) {
                returnval = false;
            }
        }
        timestamplock.readLock().unlock();
        return returnval;
    }

    private synchronized void updateTerminationFrom(Message inputMessage){
        if(inputMessage.messageType == MessageType.TERMINATION){
            this.terminationFrom.add(inputMessage.from);
        }
    }

    private synchronized void onDelivery(Message inputMessage){
        this.deliveredMessages.add(inputMessage);
        this.bufferedMessages.remove(inputMessage);
        this.logMessage(inputMessage.toString());
        this.updateTerminationFrom(inputMessage);
    }

    public synchronized void addMessage(Message inputMessage) {
        // adding a delay here doesnt make any difference
        // Thread.sleep(random.nextInt(10));
        this.updateClock(inputMessage.from);
        if (this.isCausallyReady(inputMessage.vectortimestamp)) {
            this.onDelivery(inputMessage);
            // Retrieve deliverable messages and remove them from bufferedMessages
            List<Message> dm = this.getDeliverableMessages();
            dm.forEach(this::onDelivery);
        } else {
            this.bufferedMessages.add(inputMessage);
        }
    }


    public void createLogFile() {
        String folderName = "log";
        File folder = new File(folderName);
        if (!folder.exists() && !folder.mkdir()) {
            System.err.println("Failed to create log folder.");
            return; // Abort creating the log file
        }
        String fileName = folderName + "/deliveredmessages" + this.uid + ".txt";
        File file = new File(fileName);
        if (file.exists() && !file.delete()) {
            System.err.println("Failed to delete existing file: " + fileName);
            return; // Abort creating the file
        }
        try {
            if (!file.createNewFile()) {
                System.err.println("Failed to create new file: " + fileName);
                return; // Abort creating the file
            }
            System.out.println("Log file created successfully: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logMessage(String message) {
        String filePath = "./log/deliveredmessages" + this.uid + ".txt";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = sdf.format(new Date());
        try (FileWriter fw = new FileWriter(filePath, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
            out.println("UTC: " + formattedDate + " " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(Message broadcastMessage) {
        this.adjacentNodes.stream().forEach(t -> {
            try {
                this.sendMessageTCP(broadcastMessage, t.hostname, t.port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public List<Message> getDeliverableMessages() {
        List<Message> deliverableMessages = this.bufferedMessages.stream()
                .filter(t -> isCausallyReady(t.vectortimestamp)).collect(Collectors.toList());
        Collections.sort(deliverableMessages);
        return deliverableMessages;
    }

    private void sendTerminationMessage() {
        String messageText = "last message from " + uid;
        this.updateClock(this.uid);
        int[] sendtimestamp = this.getVectorClock();
        Message message = new Message(this.uid, sendtimestamp, MessageType.TERMINATION, messageText);
        broadcastMessage(message);
    }
    
    private void waitForTermination() {
        try {
            while (this.terminationFrom.size() != this.adjacentNodes.size()) {
                Thread.sleep(100);
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Thread was interrupted.");
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    public void startBroadcasting(int N) throws InterruptedException {
        Random random = new Random();
        for (int i = 0; i < N; i++) {
            String messageText = "m " + i + " from " + uid;
            this.updateClock(this.uid);
            int[] sendTimestamp = this.getVectorClock();
            Message message = new Message(this.uid, sendTimestamp, MessageType.BROADCAST, messageText);
            broadcastMessage(message);
            // Sleep for a random duration between 0 and 10 milliseconds
            Thread.sleep(random.nextInt(10));
        }
        this.sendTerminationMessage();
        this.waitForTermination();
        this.state = NodeState.Terminated;
    }

    public void sendMessageTCP(Message message, String host, int port) throws IOException {
        int retryInterval = 5000;
        int maxRetries = 5;
        int retries = 0;
        while (retries <= maxRetries) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), retryInterval);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(message);
                output.flush();
                return;
            } catch (IOException e) {
                retries++;
                if (retries > maxRetries) {
                    throw e; // throw the exception if max retries have been reached
                }
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void startListening() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            while (this.state == NodeState.Running) {
                Socket clientSocket = serverSocket.accept();
                executor.execute(() -> {
                    try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream())) {
                        Message message;
                        while (clientSocket.isConnected() && !clientSocket.isClosed()
                                && (message = (Message) input.readObject()) != null) {
                            this.addMessage(message);
                        }
                    } catch (EOFException e) {
                        // Stream ended normally, do nothing
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } finally {
            serverSocket.close();
            executor.shutdown();
        }
    }

    public void runCausalBroadcast(int N) {
        Thread receiverThread = new Thread(() -> {
            try {
                this.startListening();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread senderThread = new Thread(() -> {
            try {
                this.startBroadcasting(N);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        receiverThread.start();
        senderThread.start();
        if(this.state == NodeState.Terminated){
            try {
                receiverThread.join();
                senderThread.start();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

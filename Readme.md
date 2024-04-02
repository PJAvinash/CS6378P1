# Causal Order Broadcast Implementation in Java
This repository contains a Java implementation of Causal Order Broadcast using vector clocks for ordering among send/receive events.

# Algorithm Details
The algorithm utilizes vector clocks for maintaining the causal ordering of messages among different processes. Here are the key details of the algorithm:

**Causal Delivery Condition using Vector Clocks**
- For N processes, V_i[1,...N] represents the vector timestamp of the ith process.
- before send from i: 
- - V_i[i]++
- - message.timestamp = V_i

- after receiving m at j from i;
- - V_j[i]++

- Before sending a message from process i, increment V_i[i] and set message.timestamp = V_i.
- Upon receiving message m at process j from process i, increment V_j[i].
- The function isReadyForCausalDelivery(inboundMessage, Process_i) determines whether an inbound message is ready for causal delivery at process i. It checks the following condition:

# Execution
- go to /Launcher in terminal run 
```
chmod +x launcher.sh && ./launcher.sh config.txt
```

- For cleaning up processes 
```
chmod +x cleanup.sh && ./cleanup.sh config.txt
```



# Contributers
PJ Avinash











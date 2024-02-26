Here I implemented a simple program for Causally Ordered Broadcast in a distributed system.

We use vector clocks for ordering amound send/receive events
If we have N processes, 
V_i[1,...N] be the vectortime stamp of ith process. 

Algorithm details:

before send from i: 
    V_i[i]++
    message.timestamp = V_i

after receiving m at j from i;
    V_j[i]++

isReadyForCausalDelivery(inboundMessage, Process_i):
    return ( For All k != SENDER inboundMessage.vectortimestamp[k] <= V_i[k] && inboundMessage.vectortimestamp[SENDER] <= V_i[SENDER]+1 )


For execution, clone this repo https://github.com/PJAvinash/CS6378P1.git

go to /Launcher in terminal run 'chmod +x launcher.sh && ./launcher.sh config.txt'
For cleaning up processes 'chmod +x cleanup.sh && ./cleanup.sh config.txt'










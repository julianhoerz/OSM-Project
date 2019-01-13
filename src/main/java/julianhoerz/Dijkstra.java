package julianhoerz;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;




class Dijkstra implements HttpHandler {

    Double startLat;
    Double startLng;
    Double endLat;
    Double endLng;

    Integer startNode;
    Integer endNode;

    Graph graph;

    Dijkstra(Graph graph){
        this.graph = graph;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            httpExchange.sendResponseHeaders(204, -1);
            return;
        }

       // System.out.println(httpExchange.getRequestURI().toString());

        String url = httpExchange.getRequestURI().toString();
        String[] parts = url.split("\\/");

        String geojson = "no route found...";

        if(parts.length == 3){
            String[] parts2 = parts[2].split("\\,");
            if(parts2.length == 4){
                this.startLat = Double.valueOf(parts2[1]);
                this.startLng = Double.valueOf(parts2[0]);
                this.endLat = Double.valueOf(parts2[3]);
                this.endLng = Double.valueOf(parts2[2]);

                System.out.println("Start: " + this.startLat + ", " + this.startLng);
                System.out.println("End: " + this.endLat + ", " + this.endLng);
                
                geojson = startDijkstra();
                //System.out.println("" + geojson);
            }
        }

        //System.out.println("Partslength: " + parts.length);
        // URL url = httpExchange.getRequestURI().toURL();

        //System.out.println("URL: " + test);

        String response = geojson;
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }


    private String startDijkstra(){
        boolean ret = findNextNode(this.startLat,this.startLng,this.endLat,this.endLng);
        if(ret == false){
            System.out.println("Error finding node next to start or endpoint...");
            return "";
        }
        else{
            System.out.println("Found next nodes...");
        }

		PriorityQueue<Node> front = new PriorityQueue<Node>();
		int[] previousNode = new int[graph.getNodesLength()]; //Stores OSM-IDs of the nodes on the final path
		boolean[] visited = new boolean[graph.getNodesLength()];

        for (int i = 0; i < graph.getNodesLength(); i++) {
			visited[i] = false;
        }
        
        front.add(new Node(0.0, startNode, -1));

        while (!front.isEmpty()) {
			// retrieve min distance element:
			Node n = front.poll();

			if (visited[n.ID]) {
				continue;
			}
			visited[n.ID] = true;

            previousNode[n.ID] = n.previous;
            
            //System.out.println("Current Loc: " + graph.getNodeLat(n.ID)+","+graph.getNodeLng(n.ID));

			if (n.ID == endNode) {
				break;
			}

			// System.out.println("current/min is node #" + n.ID);

			// standard neighbors retrieval loop:
			int idx = graph.getNodeOffset(n.ID);
			long endFor;
			if (idx == graph.getNodesLength()) {
				endFor = graph.getEdgesLength() - 1;
			} else {
				endFor = graph.getNodeOffset(n.ID+1);
			}
			// for each neighbor
			for (int i = graph.getNodeOffset(n.ID); i < endFor; i++) {
				int neighbor = graph.getEdges(i);
				double edgeWeight = graph.getEdgesLength(i);

				// System.out.println(" - neighbor #" + neighbor + " w: " + edgeWeight);

				double newWeight = n.distance + edgeWeight;

				front.add(new Node(newWeight, neighbor, n.ID));

			}
        }
        

        ArrayList<Integer> result = new ArrayList<Integer>();
		int currentID = endNode;
		int currentIndex = endNode;
		while (previousNode[currentIndex] != -1) {
			result.add(currentID);
			currentID = previousNode[currentIndex];
			currentIndex = currentID;
		}
		result.add(currentID);
		Collections.reverse(result);
		if (result.size() == 1) {
			System.err.println("No connection found :(");
		}
		System.out.println("Done with dijkstra");

        return generateGeoJson(result);
    }

    private boolean findNextNode(Double startLat, Double startLng, Double endLat, Double endLng){

        Boolean found[] = new Boolean[2];
        found[0] = false;
        found[1] = false;

        String keylat = "" + ((int) Math.floor(startLat * 10));
        String keylng = "" + ((int) Math.floor(startLng * 10));
        while (keylat.length() < 4) {keylat = "0" + keylat;}
        while (keylng.length() < 4) {keylng = "0" + keylng;}

        Integer key1 = Integer.parseInt("" + keylat + keylng);


        keylat = "" + ((int) Math.floor(endLat * 10));
        keylng = "" + ((int) Math.floor(endLng * 10));
        while (keylat.length() < 4) {keylat = "0" + keylat;}
        while (keylng.length() < 4) {keylng = "0" + keylng;}

        Integer key2 = Integer.parseInt("" + keylat + keylng);
        Integer key1end = 0; 
        Integer key2end = 0;

        //System.out.println("Keys: " + key1 + ", " + key2);

        Integer currentKey;
        for(int i = 0; i < graph.getFramesLength(); i ++){
            currentKey = graph.getOffsetFrames(i, 0);
            if(currentKey.equals(key1)){
                found[0] = true;
                key1 = graph.getOffsetFrames(i, 1);
                if(i == graph.getFramesLength()-1){
                    key1end = graph.getNodesLength();
                }
                else{
                    key1end = graph.getOffsetFrames(i+1, 1);
                }
            }
            if(currentKey.equals(key2)){
                found[1] = true;
                key2 = graph.getOffsetFrames(i, 1);
                if(i == graph.getFramesLength()-1){
                    key2end = graph.getNodesLength();
                }
                else{
                    key2end = graph.getOffsetFrames(i+1, 1);
                }
            }
        }

        if(found[0] == false || found[1] == false){
            System.out.println("No Frame found in this map");
            System.out.println("ERROR: No route found...");
            return false;
        }

        System.out.println("Key2End: " + key2end + " Key2: " + key2);

        //Find Startnode
        double dist = 10000000;
        double distbuff = 0;
        Integer startnodeindex = -1;
        for(int i = key1 ; i < key1end; i ++){
            distbuff = calculateDistance(graph.getNodeLat(i), graph.getNodeLng(i), startLat, startLng); 
            if(distbuff < dist){
                startnodeindex = i;
                dist = distbuff;
            }
        }

        dist = 10000000;
        Integer endnodeindex = -1;
        for(int i = key2 ; i < key2end; i ++){
            distbuff = calculateDistance(graph.getNodeLat(i), graph.getNodeLng(i), endLat, endLng); 
            if(distbuff < dist){
                endnodeindex = i;
                dist = distbuff;
            }
        }

        if(startnodeindex == -1 || endnodeindex == -1){
            System.out.println("No Node found in this Frame");
            System.out.println("ERROR: No route found...");
            return false;
        }


        System.out.println("Startnode Coords: " + graph.getNodeLat(startnodeindex) + ", " + graph.getNodeLng(startnodeindex));
        System.out.println("Endnode Coords: " + graph.getNodeLat(endnodeindex) + ", " + graph.getNodeLng(endnodeindex));

        this.startNode = startnodeindex;
        this.endNode = endnodeindex;

        return true;

    }


    private double calculateDistance(double lat1, double lng1, double lat2, double lng2){
        double earthRadius = 6371000;

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        float dist = (float) (earthRadius * c);
    
        return dist;
    }

    private String generateGeoJson(ArrayList<Integer> routenodes){
        String geojson = "";

        String prestring = "";
        String poststring = "";
        String data = "";
        String position ="";

        prestring = "{\"type\": \"FeatureCollection\",\"features\": [{\"type\": \"Feature\",\"properties\": {},\"geometry\": {\"type\": \"LineString\",\"coordinates\": [";

        poststring = "]}}]}";

        for(int i = 0; i < routenodes.size(); i++){
            position = "[";
            position = position + graph.getNodeLng(routenodes.get(i)).toString();
            position = position + ",";
            position = position + graph.getNodeLat(routenodes.get(i)).toString();
            data = data + position + "]";
            if(i < routenodes.size()-1){
                data = data + ",";
            }
        }

        geojson = prestring + data + poststring;

        return geojson;
    
    }

}





// TestURL: http://localhost:3000/dijkstra/8.802853,53.077668,8.795796,53.082291
// TestURL2: http://localhost:3000/dijkstra/8.81027,53.06907,8.80797,53.06907


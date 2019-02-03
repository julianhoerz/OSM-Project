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

    double startLat;
    double startLng;
    double endLat;
    double endLng;

    int startNode;
    int endNode;

    double[] offsetCoords;

    Graph graph;

    Dijkstra(Graph graph){
        this.graph = graph;
        this.offsetCoords = new double[2];
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
        // boolean ret = findNextNode(this.startLat,this.startLng,this.endLat,this.endLng);
        int ret;

        ret = findNextStreet(this.startLat,this.startLng);
        if(ret == -1){
            System.out.println("Error occured while finding startnode...");
            return "";
        }
        this.startNode = ret;


        ret = findNextStreet(this.endLat, this.endLng);
        if(ret == -1){
            System.out.println("Error occured while finding endnode...");
            return "";
        }
        this.endNode = ret;

        System.out.println("Startnode Coords: " + graph.getNodeLat(this.startNode) + ", " + graph.getNodeLng(this.startNode));
        System.out.println("Startnode ID: " + graph.getNodeId(this.startNode));
        System.out.println("Endnode Coords: " + graph.getNodeLat(this.endNode) + ", " + graph.getNodeLng(this.endNode));
        System.out.println("Endnode ID: " + graph.getNodeId(this.endNode));

        System.out.println("Found next nodes...");
        System.out.println("Start Dijkstra...");

		PriorityQueue<Node> front = new PriorityQueue<Node>();
		int[] previousNode = new int[graph.getNodesLength()]; //Stores OSM-IDs of the nodes on the final path
		boolean[] visited = new boolean[graph.getNodesLength()];

        for (int i = 0; i < graph.getNodesLength(); i++) {
			visited[i] = false;
        }
        
        front.add(new Node(0.0, startNode, -1));

        while (!front.isEmpty()) {

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

			//int idx = graph.getNodeOffset(n.ID);
			int endFor = graph.getNodeOffset(n.ID + 1);
			// if (idx == graph.getNodesLength()) {
			// 	endFor = graph.getEdgesLength() - 1;
			// } else {
			// 	endFor = graph.getNodeOffset(n.ID+1);
			// }

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


    private int[][] findFrames(double lat, double lng){

        int[][] keys = {{0, 0}, {0, 0}, {0, 0}, 
                            {0, 0}, {0, 0}, {0, 0},
                            {0, 0}, {0, 0}, {0, 0}};

        int latint = (int) Math.floor(lat * 10);
        int lngint = (int) Math.floor(lng * 10);

        String keylat;
        String keylng;
        int cnt = 0;
        for(int i = -1; i <= 1; i ++){
            for(int p = -1; p <= 1; p ++){
                keylat = "" + ((int) latint + i );
                keylng = "" + ((int) lngint + p );
                while (keylat.length() < 4) {keylat = "0" + keylat;}
                while (keylng.length() < 4) {keylng = "0" + keylng;}
                keys[cnt][0] = Integer.parseInt("" + keylat + keylng);
                cnt ++;
            }
        }
        boolean found = false;
        int currentKey;
        for(int i = 0; i < graph.getFramesLength(); i ++){
            currentKey = graph.getOffsetFrames(i, 0);

            for(int p = 0; p < 9; p++){
                if(currentKey == keys[p][0]){
                    found = true;
                    keys[p][0] = graph.getOffsetFrames(i, 1);
                    if(i == graph.getFramesLength()-1){
                        keys[p][1] = graph.getNodesLength();
                    }
                    else{
                        keys[p][1] = graph.getOffsetFrames(i+1, 1);
                    }
                }
            }
        }
        if(found == false){
            keys[0][0] = -1;
        }
        return keys;
    }


    private double[] projection(int node1, int node2, double lat, double lng){
        double[] coords = new double[2];

        double node1lat = graph.getNodeLat(node1);
        double node1lng = graph.getNodeLng(node1);
        double node2lat = graph.getNodeLat(node2);
        double node2lng = graph.getNodeLng(node2);

        /** Using Equirectangular projection: */
        double phi = node1lat;
        double lambda = node1lng;


        double[] direction = new double[2];
        double directionnorm;
        direction[0] = (node2lat-phi);
        direction[1] = (node2lng-lambda)*Math.cos(Math.toRadians(phi));
        directionnorm = Math.sqrt(Math.pow(direction[0],2)+Math.pow(direction[1], 2));
        direction[0] /= directionnorm;
        direction[1] /= directionnorm;

        double[] vec = new double[2];
        vec[0] = lat-phi;
        vec[1] = (lng-lambda)*Math.cos(Math.toRadians(phi));

        double shift = direction[0] * vec[0] + direction[1] * vec[1];

        if(shift <= 0 || shift >= directionnorm){
            coords[0] = -1.0;
            return coords;
        }

        double[] coordsxy = new double[2];
        coordsxy[0] = shift*direction[0];
        coordsxy[1] = shift*direction[1];

        coords[0] = coordsxy[0] + phi;
        coords[1] = coordsxy[1] / Math.cos(Math.toRadians(phi)) + lambda;

        return coords;
    }


    private double[] calculateOrthogonalPoint(int nodeid, double lat, double lng){
        double[] coords = {-1.0, 0.0};

        double dist;
        dist = calculateDistance(graph.getNodeLat(nodeid), graph.getNodeLng(nodeid), lat, lng); 

        int[] successorId;
        int startindex = graph.getNodeOffset(nodeid);
        int endindex = graph.getNodeOffset(nodeid + 1);
        successorId = new int[endindex-startindex];
        if(successorId.length == 0){
            /** No successor e.g. at the pbf border or old ending onewaystreets -> Not a error */
            // System.out.println("No successor for this node...");
            // System.out.println("ERROR...");
            // System.out.println("Coords: " + graph.getNodeLat(nodeid) + "," + graph.getNodeLng(nodeid));
            // System.out.println("OSM ID: " + graph.getNodeId(nodeid));
            return coords;
        }

        double[] coordsbuff = new double[2];
        double distbuff;
        for(int i = startindex; i < endindex; i ++){
            coordsbuff = projection(nodeid,graph.getEdges(i),lat,lng);
            if(coordsbuff[0] == -1.0){
                continue;
            }
            distbuff = calculateDistance(coordsbuff[0], coordsbuff[1], lat, lng);
            if(distbuff < dist){
                dist = distbuff;
                coords = coordsbuff;
            }
        }


        return coords;
    }

    private int findNextStreet(double lat, double lng){
        double[] coords = new double[2];

        int[][] keys = findFrames(lat, lng);
        if(keys[0][0] == -1){
            System.out.println("No Frame found in this map");
            System.out.println("ERROR: No route found...");
            return -1;
        }


        double dist = Double.POSITIVE_INFINITY;
        double distbuff = 0;
        int startnodeindex = -1;
        for(int p = 1; p < 9 ; p ++){

            for(int nodeid = keys[p][0] ; nodeid < keys[p][1]; nodeid ++){

                coords = calculateOrthogonalPoint(nodeid, lat,lng);
                if(coords[0] == -1.0){
                    coords[0] = graph.getNodeLat(nodeid);
                    coords[1] = graph.getNodeLng(nodeid);
                }
                distbuff = calculateDistance(coords[0],coords[1], lat, lng);
                if(distbuff < dist){
                    startnodeindex = nodeid;
                    dist = distbuff;
                    this.offsetCoords = coords;
                }

            }
        }

        System.out.println("Orthogonalpoint: " + this.offsetCoords[0] + "," +this.offsetCoords[1]);


        return startnodeindex;
    }


    private boolean findNextNode(Double startLat, Double startLng, Double endLat, Double endLng){


        /*Find Key in Frames*/

        int[][] startKeys = findFrames(startLat, startLng);
        int[][] endKeys = findFrames(endLat, endLng);

        if(startKeys[0][0] == -1 || endKeys[0][0] == -1){
            System.out.println("No Frame found in this map");
            System.out.println("ERROR: No route found...");
            return false;
        }





        /** Start Nodesearch in the given keyspace */

        double dist = 10000000;
        double distbuff = 0;
        int startnodeindex = -1;
        for(int p = 1; p < 9 ; p ++){
            for(int i = startKeys[p][0] ; i < startKeys[p][1]; i ++){
                distbuff = calculateDistance(graph.getNodeLat(i), graph.getNodeLng(i), startLat, startLng); 
                if(distbuff < dist){
                    startnodeindex = i;
                    dist = distbuff;
                }
            }
        }

        dist = 10000000;
        int endnodeindex = -1;
        for(int p = 1; p < 9 ; p ++){
            for(int i = endKeys[p][0] ; i < endKeys[p][1]; i ++){
                distbuff = calculateDistance(graph.getNodeLat(i), graph.getNodeLng(i), endLat, endLng); 
                if(distbuff < dist){
                    endnodeindex = i;
                    dist = distbuff;
                }
            }
        }

        if(startnodeindex == -1 || endnodeindex == -1){
            System.out.println("No Node found in this Frame");
            System.out.println("ERROR: No route found...");
            return false;
        }


        System.out.println("Startnode Coords: " + graph.getNodeLat(startnodeindex) + ", " + graph.getNodeLng(startnodeindex));
        System.out.println("Startnode ID: " + graph.getNodeId(startnodeindex));
        System.out.println("Endnode Coords: " + graph.getNodeLat(endnodeindex) + ", " + graph.getNodeLng(endnodeindex));
        System.out.println("Endnode ID: " + graph.getNodeId(endnodeindex));

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
            position = position + graph.getNodeLng(routenodes.get(i)) + "";
            position = position + ",";
            position = position + graph.getNodeLat(routenodes.get(i)) + "";
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


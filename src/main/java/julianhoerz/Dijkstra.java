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
    int nodebuff;
    int secondnode;

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
        NodeProj ret;

        System.out.println("Startnode: ");
        ret = findNextStreet(this.startLat,this.startLng);
        if(ret.getN1ID() == -1){
            System.out.println("Error occured while finding startnode...");
            return "";
        }
        NodeProj startProj = new NodeProj(ret);

        

        System.out.println("Endnode: ");
        ret = findNextStreet(this.endLat, this.endLng);
        if(ret.getN1ID() == -1){
            System.out.println("Error occured while finding endnode...");
            return "";
        }
        NodeProj endProj = new NodeProj(ret);



        
        System.out.println("Start Dijkstra...");

		PriorityQueue<Node> front = new PriorityQueue<Node>();
		int[] previousNode = new int[graph.getNodesLength()]; //Stores OSM-IDs of the nodes on the final path
		boolean[] visited = new boolean[graph.getNodesLength()];

        for (int i = 0; i < graph.getNodesLength(); i++) {
            visited[i] = false;
            previousNode[i] = -1;
        }
        
        Node n;
        this.endNode = endProj.getN1ID();
        int endNode2 = this.endNode;
        if(endProj.getProjectedCoords()[0] != -1){
            endNode2 = endProj.getN2ID();
        }


        if(startProj.getProjectedCoords()[0] == -1){
            n = new Node(0.0, startProj.getN1ID(), -1);
            front.add(n);
        }
        else{
            for(int i = graph.getNodeOffset(startProj.getN1ID()); i < graph.getNodeOffset(startProj.getN1ID()+1); i++){
                if(graph.getEdges(i) == startProj.getN2ID()){
                    double dist2 = calculateDistance(startProj.getN2Coords()[0], startProj.getN2Coords()[1], startProj.getProjectedCoords()[0], startProj.getProjectedCoords()[1]);
                    Node n2 = new Node(dist2, startProj.getN2ID(), -1);
                    front.add(n2);

                }
            }
            for(int i = graph.getNodeOffset(startProj.getN2ID()); i < graph.getNodeOffset(startProj.getN2ID()+1); i++){
                if(graph.getEdges(i) == startProj.getN1ID()){
                    double dist1 = calculateDistance(startProj.getN1Coords()[0], startProj.getN1Coords()[1], startProj.getProjectedCoords()[0], startProj.getProjectedCoords()[1]);
                    Node n1 = new Node(dist1, startProj.getN1ID(), -1);
                    front.add(n1);
                }
            }
        }
        boolean foundend1 = false;
        boolean foundend2 = false;
        double distance1 = Double.POSITIVE_INFINITY;
        double distance2 = Double.POSITIVE_INFINITY;
        

        while (!front.isEmpty()) {

			n = front.poll();

			if (visited[n.ID]) {
				continue;
			}
			visited[n.ID] = true;

            previousNode[n.ID] = n.previous;

			if (n.ID == endNode) {
                foundend1 = true;
                distance1 = n.distance;
            }
            if (n.ID == endNode2) {
                foundend2 = true;
                distance2 = n.distance;
            }
            if(foundend1 && foundend2){
                break;
            }

			int endFor = graph.getNodeOffset(n.ID + 1);


			for (int i = graph.getNodeOffset(n.ID); i < endFor; i++) {
				int neighbor = graph.getEdges(i);
				double edgeWeight = graph.getEdgesLength(i);

				// System.out.println(" - neighbor #" + neighbor + " w: " + edgeWeight);

				double newWeight = n.distance + edgeWeight;

				front.add(new Node(newWeight, neighbor, n.ID));

			}
        }

        if(endNode2 != endNode){
            boolean oneway1 = true;
            boolean oneway2 = true;
            for(int i = graph.getNodeOffset(endProj.getN1ID()); i < graph.getNodeOffset(endProj.getN1ID()+1); i++){
                if(graph.getEdges(i) == endProj.getN2ID()){
                    oneway1 = false;
                    distance2 += calculateDistance(endProj.getN2Coords()[0], endProj.getN2Coords()[1], endProj.getProjectedCoords()[0], endProj.getProjectedCoords()[1]);
                }
            }
            for(int i = graph.getNodeOffset(endProj.getN2ID()); i < graph.getNodeOffset(endProj.getN2ID()+1); i++){
                if(graph.getEdges(i) == endProj.getN1ID()){
                    oneway2 = false;
                    distance1 += calculateDistance(endProj.getN1Coords()[0], endProj.getN1Coords()[1], endProj.getProjectedCoords()[0], endProj.getProjectedCoords()[1]);
                }
            }
            if(oneway1 == false && oneway2 == false){
                if(distance1 > distance2){
                    endNode = endNode2;
                }
            }else{
                if(oneway1 == true){
                    endNode = endNode2;
                }
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

        return generateGeoJson(result, startProj,endProj);
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


    /**
     * Provides an orthogonal projection of the initial coordinates of 
     * proj onto the line node1-node2. If no orthogonal projection is 
     * possible return with projection coordinates -1.
     * 
     */
    private NodeProj projection(NodeProj proj){

        double shift;

        double[] coordsxy = new double[2];
        double[] vec = new double[2];

        double node1lat = proj.getN1Coords()[0];
        double node1lng = proj.getN1Coords()[1];
        double node2lat = proj.getN2Coords()[0];
        double node2lng = proj.getN2Coords()[1];

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

        vec[0] = proj.getInitialCoords()[0]-phi;
        vec[1] = (proj.getInitialCoords()[1]-lambda)*Math.cos(Math.toRadians(phi));

        shift = direction[0] * vec[0] + direction[1] * vec[1];

        if(shift <= 0 || shift >= directionnorm){
            /** Coordinates of orthogonal projection are not between node1-node2.*/
            proj.setProjectedCoords(-1.0,-1.0);
            return proj;
        }

        coordsxy[0] = shift*direction[0];
        coordsxy[1] = shift*direction[1];

        proj.setProjectedCoords(coordsxy[0] + phi, 
                                coordsxy[1] / Math.cos(Math.toRadians(phi)) + lambda);

        return proj;
    }


    private NodeProj calculateOrthogonalPoint(NodeProj projection){
        double[] nodecoords = projection.getN1Coords();
        double dist;
        dist = calculateDistance(nodecoords[0], nodecoords[1], projection.getInitialCoords()[0], projection.getInitialCoords()[1]); 

        int[] successorId;
        int startindex = graph.getNodeOffset(projection.getN1ID());
        int endindex = graph.getNodeOffset(projection.getN1ID() + 1);
        successorId = new int[endindex-startindex];
        if(successorId.length == 0){
            projection.setProjectedCoords(-1.0,-1.0);
            return projection;
        }

        double[] coordsbuff = new double[2];
        double distbuff;
        NodeProj projbuff = new NodeProj(projection);
        int nodeid;
        for(int i = startindex; i < endindex; i ++){
            nodeid = graph.getEdges(i);
            projbuff.setN2Coords(graph.getNodeLat(nodeid), graph.getNodeLng(nodeid));
            projbuff.setN2ID(nodeid);

            projbuff = projection(projbuff);

            coordsbuff = projbuff.getProjectedCoords();

            if(coordsbuff[0] == -1.0){
                continue;
            }
            distbuff = calculateDistance(coordsbuff[0], coordsbuff[1],projbuff.getInitialCoords()[0], projbuff.getInitialCoords()[1]);
            if(distbuff < dist){
                dist = distbuff;
                projection.setN2Coords(projbuff.getN2Coords()[0], projbuff.getN2Coords()[1]);
                projection.setN2ID(projbuff.getN2ID());
                projection.setProjectedCoords(projbuff.getProjectedCoords()[0], projbuff.getProjectedCoords()[1]);
            }
        }


        return projection;
    }

    private NodeProj findNextStreet(double lat, double lng){

        int[][] keys = findFrames(lat, lng);
        if(keys[0][0] == -1){
            System.out.println("No Frame found in this map");
            System.out.println("ERROR: No route found...");
            return new NodeProj();
        }


        double dist = Double.POSITIVE_INFINITY;
        double distbuff = 0;
        int startnodeindex = -1;
        NodeProj projectionfinal = new NodeProj();
        NodeProj projection = new NodeProj();
        projection.setInitialCoords(lat, lng);
        for(int p = 1; p < 9 ; p ++){

            for(int nodeid = keys[p][0] ; nodeid < keys[p][1]; nodeid ++){

                projection.setN1Coords(graph.getNodeLat(nodeid), graph.getNodeLng(nodeid));
                projection.setN1ID(nodeid);

                projection = calculateOrthogonalPoint(projection);

                if(projection.getProjectedCoords()[0] == -1){
                    distbuff = calculateDistance(projection.getN1Coords()[0], projection.getN1Coords()[1], lat, lng);
                }
                else{
                    distbuff = calculateDistance(projection.getProjectedCoords()[0], projection.getProjectedCoords()[1], lat, lng);
                }

                if(distbuff < dist){
                    startnodeindex = nodeid;
                    dist = distbuff;
                    projectionfinal = new NodeProj(projection);
                }
            }
        }


        if(projectionfinal.getProjectedCoords()[0] != -1.0){
            System.out.println("Point not on node...");
            System.out.println("Point is on " + projectionfinal.getProjectedCoords()[0] + "," +projectionfinal.getProjectedCoords()[1]);
            System.out.println("Nextnode Coords: " + projectionfinal.getN1Coords()[0] + "," +  + projectionfinal.getN1Coords()[1]);
        }
        else{
            System.out.println("Point on node...");
            System.out.println("Node Coords: " + graph.getNodeLat(startnodeindex) + "," +  + graph.getNodeLng(startnodeindex));
        }
        
        return projectionfinal;
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

    private String generateGeoJson(ArrayList<Integer> routenodes, NodeProj startProj, NodeProj endProj){
        String geojson = "";

        String prestring = "";
        String poststring = "";
        String data = "";
        String position ="";

        prestring = "{\"type\": \"FeatureCollection\",\"features\": [{\"type\": \"Feature\",\"properties\": {},\"geometry\": {\"type\": \"LineString\",\"coordinates\": [";

        poststring = "]}}]}";

        if(startProj.getProjectedCoords()[0] != -1){
            data = "[" + startProj.getProjectedCoords()[1] + "," + startProj.getProjectedCoords()[0] + "],";
        }


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

        if(endProj.getProjectedCoords()[0] != -1){
            data = data + ",[" + endProj.getProjectedCoords()[1] + "," + endProj.getProjectedCoords()[0] + "]";
        }

        geojson = prestring + data + poststring;

        return geojson;
    
    }

}




// Mapmatching
// TestURL: http://localhost:3000/dijkstra/8.802853,53.077668,8.795796,53.082291
// TestURL2: http://localhost:3000/dijkstra/8.81027,53.06907,8.80797,53.06907


package julianhoerz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;






class Dijkstra {



    Graph graph;
    Draw draw;
    MathFunctions mathFunctions;

    Dijkstra(Graph graph){
        this.graph = graph;
        this.draw = new Draw(graph);
        this.mathFunctions = new MathFunctions();
    }

    /** 
     * Performs the Dijkstra Algorithm from StartPoint to the EndPoint
     * and returns the shortest path distance.
     */
    public double dijkstraDistance(NodeProj startPoint, NodeProj endPoint){
        double distance = 0d;
        
        ArrayList<double[]> coordinates;
        coordinates = dijkstraCoordinates(startPoint, endPoint);
        if(coordinates.size() < 2){
            return 0d;
        }
        for(int i = 0; i < coordinates.size()-1; i ++){
            distance += mathFunctions.calculateDistance(coordinates.get(i)[0], coordinates.get(i)[1], coordinates.get(i+1)[0], coordinates.get(i+1)[1]);
        }

        return distance;
    }

    /**
     * Performs the Dijkstra Algorithm from StartPoint to the EndPoint 
     * and returns all involved coordinates.
     */
    public ArrayList<double[]> dijkstraCoordinates(NodeProj startPoint, NodeProj endPoint){
        if(startPoint.getN1ID() == -1 || endPoint.getN1ID() == -1){
            System.out.println("Invalid Start and/or End-Point");
            return null;
        }
        ArrayList<double[]> coordinates = new ArrayList<double[]>();
        /** Check Onewaytag (has been computed) */
        this.checkOneWay(startPoint);
        this.checkOneWay(endPoint);
        
        


        /*Special Case: Both start and endpoint are between the same nodes or
         both start and endpoint are on the same node. */
        boolean ret = checkSpecialCase(startPoint,endPoint);
        if(ret){
            if(startPoint.getProjectedCoords()[0] == -1d){
                coordinates.add(startPoint.getN1Coords());
                coordinates.add(endPoint.getN1Coords());
            }
            else{
                coordinates.add(startPoint.getProjectedCoords());
                coordinates.add(endPoint.getProjectedCoords());
            }
            return coordinates;
        }



        /** Start Dijkstra */
        PriorityQueue<Node> front = new PriorityQueue<Node>();
		int[] previousNode = new int[graph.getNodesLength()]; //Stores OSM-IDs of the nodes on the final path
		boolean[] visited = new boolean[graph.getNodesLength()];

        for (int i = 0; i < graph.getNodesLength(); i++) {
            visited[i] = false;
            previousNode[i] = -1;
        }
        
        int endNode1, endNode2;
        endNode1 = endPoint.getN1ID();
        endNode2 = endNode1;
        if(endPoint.getProjectedCoords()[0] != -1){
            endNode2 = endPoint.getN2ID();
        }


        /** Define Startnodes */
        Node n;
        double dist = 0d;
        if(startPoint.getProjectedCoords()[0] == -1){
            n = new Node(dist, startPoint.getN1ID(), -1);
            front.add(n);
        }
        else{
            if(startPoint.getOneway() == 0 || startPoint.getOneway() == 1){
                dist = this.mathFunctions.calculateDistance(startPoint.getN2Coords(), startPoint.getProjectedCoords());
                n = new Node(dist,startPoint.getN2ID(),-1);
                front.add(n);
            }
            if(startPoint.getOneway() == 0 || startPoint.getOneway() == -1){
                dist = this.mathFunctions.calculateDistance(startPoint.getN1Coords(), startPoint.getProjectedCoords());
                n = new Node(dist,startPoint.getN1ID(),-1);
                front.add(n);
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

			if (n.ID == endNode1) {
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

        if(!foundend1 && !foundend2){
            System.out.println("No route found. End of Dijkstra...");
            return new ArrayList<double[]>();
        }

        int finalnode = endNode1;
        if(endNode2 != endNode1){
            if(endPoint.getOneway() == 1){
                finalnode = endNode1;
            }
            if(endPoint.getOneway() == -1){
                finalnode = endNode2;
            }
            if(endPoint.getOneway() == 0){
                distance1 += this.mathFunctions.calculateDistance(endPoint.getN1Coords(), endPoint.getProjectedCoords());
                distance2 += this.mathFunctions.calculateDistance(endPoint.getN2Coords(), endPoint.getProjectedCoords());
                if(distance1>distance2){
                    finalnode = endNode2;
                }
                else{
                    finalnode = endNode1;
                }
            }
        }


        if(endPoint.getProjectedCoords()[0] != -1d){
            coordinates.add(endPoint.getProjectedCoords());
        }
        
		int currentID = finalnode;
        int currentIndex = finalnode;
        double[] coords;
		while (previousNode[currentIndex] != -1) {
            coords = new double[2];
            coords[0] = graph.getNodeLat(currentID);
            coords[1] = graph.getNodeLng(currentID);
			coordinates.add(coords);
			currentID = previousNode[currentIndex];
			currentIndex = currentID;
        }
        coords = new double[2];
        coords[0] = graph.getNodeLat(currentID);
        coords[1] = graph.getNodeLng(currentID);
        coordinates.add(coords);
        
        if(startPoint.getProjectedCoords()[0] != -1d){
            coordinates.add(startPoint.getProjectedCoords());
        }

        Collections.reverse(coordinates);
        



        // System.out.println("Done with dijkstra");
        

        return coordinates;
    }



    private boolean checkSpecialCase(NodeProj startPoint, NodeProj endPoint){

        if(endPoint.getN1ID() == startPoint.getN2ID() && endPoint.getN2ID() == startPoint.getN1ID()){
            double[] projbuff = new double[2];
            projbuff[0] = endPoint.getProjectedCoords()[0];
            projbuff[1] = endPoint.getProjectedCoords()[1];
            endPoint.setN1ID(startPoint.getN1ID());
            endPoint.setN1Coords(startPoint.getN1Coords()[0], startPoint.getN1Coords()[1]);
            endPoint.setN2ID(startPoint.getN2ID());
            endPoint.setN2Coords(startPoint.getN2Coords()[0], startPoint.getN2Coords()[1]);
            endPoint.setProjectedCoords(projbuff[0], projbuff[1]);
        }
        if(endPoint.getN1ID() == startPoint.getN1ID() && endPoint.getN2ID() == startPoint.getN2ID()){

            if(startPoint.getN2ID() == -1){
                /** Start and Endpoint on the same Node */
                return true;
            }

            double dist1, dist2;
            dist1 = mathFunctions.calculateDistance(endPoint.getN1Coords(), endPoint.getProjectedCoords());
            dist2 = mathFunctions.calculateDistance(startPoint.getN1Coords(), startPoint.getProjectedCoords());
            if(dist1 > dist2){
                /** Check connection between node1 and node2 (oneway?)*/
                if(startPoint.getOneway() == 0 ||
                    startPoint.getOneway() == 1){
                    return true;
                }
            }
            else{
                /** Check connection between node2 and node1 (oneway?)*/
                if(startPoint.getOneway() == 0 ||
                startPoint.getOneway() == -1){
                    return true;
                }
            }
        }
        return false;
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

        int startindex = graph.getNodeOffset(projection.getN1ID());
        int endindex = graph.getNodeOffset(projection.getN1ID() + 1);
        if(startindex == endindex){
            projection.setProjectedCoords(-1.0,-1.0);
            return projection;
        }

        double dist = Double.POSITIVE_INFINITY;
        double distbuff;
        NodeProj projbuff = new NodeProj(projection);
        int nodeid;
        for(int i = startindex; i < endindex; i ++){
            nodeid = graph.getEdges(i);
            projbuff.setN2Coords(graph.getNodeLat(nodeid), graph.getNodeLng(nodeid));
            projbuff.setN2ID(nodeid);

            projbuff = projection(projbuff);

            if(projbuff.getProjectedCoords()[0] == -1.0){
                continue;
            }
            distbuff = this.mathFunctions.calculateDistance(projbuff.getProjectedCoords()[0], 
                                            projbuff.getProjectedCoords()[1],
                                            projbuff.getInitialCoords()[0],
                                            projbuff.getInitialCoords()[1]);
            if(distbuff < dist){
                dist = distbuff;
                projection.setN2Coords(projbuff.getN2Coords()[0], projbuff.getN2Coords()[1]);
                projection.setN2ID(projbuff.getN2ID());
                projection.setProjectedCoords(projbuff.getProjectedCoords()[0], projbuff.getProjectedCoords()[1]);
            }
        }
        return projection;
    }

    public NodeProj findNextStreet(double lat, double lng){

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
                    distbuff = this.mathFunctions.calculateDistance(projection.getN1Coords()[0], projection.getN1Coords()[1], lat, lng);
                }
                else{
                    distbuff = this.mathFunctions.calculateDistance(projection.getProjectedCoords()[0], projection.getProjectedCoords()[1], lat, lng);
                }

                if(distbuff < dist){
                    startnodeindex = nodeid;
                    dist = distbuff;
                    projectionfinal = new NodeProj(projection);
                }
            }
        }

        checkOneWay(projectionfinal);


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

    private void checkOneWay(NodeProj point){
        int startindex, endindex;
        boolean n12 = false;
        boolean n21 = false;

        startindex = graph.getNodeOffset(point.getN1ID());
        endindex = graph.getNodeOffset(point.getN1ID() + 1);
        for(int i = startindex; i < endindex; i++){
            if(graph.getEdges(i) == point.getN2ID()){
                n12 = true;
                break;
            }
        }

        startindex = graph.getNodeOffset(point.getN2ID());
        endindex = graph.getNodeOffset(point.getN2ID() + 1);
        for(int i = startindex; i < endindex; i++){
            if(graph.getEdges(i) == point.getN1ID()){
                n21 = true;
                break;
            }
        }

        if(n12 == true && n21 == true){
            point.setOneway(0);
        }

        if(n12 == true && n21 == false){
            point.setOneway(1);
        }

        if(n12 == false && n21 == true){
            point.setOneway(-1);
        }

        if(n12 == false && n21 == false){
            point.setOneway(2);
        }

    }


    public boolean findNextNode(Double startLat, Double startLng, Double endLat, Double endLng){


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
                distbuff = this.mathFunctions.calculateDistance(graph.getNodeLat(i), graph.getNodeLng(i), startLat, startLng); 
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
                distbuff = this.mathFunctions.calculateDistance(graph.getNodeLat(i), graph.getNodeLng(i), endLat, endLng); 
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

        // this.startNode = startnodeindex;
        // this.endNode = endnodeindex;

        return true;

    }

}




// Mapmatching
// TestURL: http://localhost:3000/dijkstra/8.802853,53.077668,8.795796,53.082291
// TestURL2: http://localhost:3000/dijkstra/8.81027,53.06907,8.80797,53.06907


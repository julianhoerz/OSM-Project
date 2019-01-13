
package julianhoerz;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;


import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.seq.PbfIterator;


public class PbfFileReader {

    private static String[] HighwayTagsArray = {"motorway", "trunk", "primary", "secondary", "tertiary", "unclassified","residential","	service","motorway_link",
                        "trunk_link", "primary_link", "secondary_link", "tertiary_link", "living_street"};

    private static String[] OneWayTagsArray = {"motorway", "motorway_link"};

    private static HashMap<Long, Integer> Nodes;
    private static Double[][] Node_Coords;
    private static Long[] Node_Id;

    private static Double Max_Lng;
    private static Double Min_Lng;
    private static Double Max_Lat;
    private static Double Min_Lat;

    private static Integer idx;

    private static ArrayList<Long[]> EdgesArrayList;
    private static Long[][] EdgesArray;
    private static HashMap<Integer,ArrayList<Long>> Frames;



    private static Integer[][] Offset_Frames_Final;

    private static Long[] Node_Id_Final;
    private static Double[][] Node_Coords_Final;
    private static Integer[] Offset_Edges_Final;

    private static Integer[] Edges_Final;
    private static Double[] Edges_Length_Final;

    private Graph graph;


    public PbfFileReader(Graph graph) {

        /** Init the maximal Lng and Lat */
        this.Max_Lng = 0.;
        this.Min_Lng = 180.;
        this.Max_Lat = 0.;
        this.Min_Lat = 180.;

        this.idx = 0;

        this.EdgesArrayList = new ArrayList<Long[]>();
        this.Frames = new HashMap<Integer, ArrayList<Long>>();
        this.Nodes = new HashMap<Long, Integer>();

        /** Set the graph for storing the data */
        this.graph = graph;

    }



    
    public void buildGraph(String filename) throws IOException {
        InputStream input = new BufferedInputStream(new FileInputStream(filename));
        PbfIterator iterator = new PbfIterator(input, true);

        System.out.println("Scan 1 of 2 in progress...");

        for (EntityContainer container : iterator) {
            if (container.getType() == EntityType.Way){
                OsmWay way = (OsmWay) container.getEntity();
                if(checkStreet(way)){
                    for(int i = 0; i < way.getNumberOfNodes(); i ++){
                       // Nodes.put(way.getNodeId(i),-1);
                    }
                }
            }
        }

        input.close();
        iterator = null;
        input = null;

        Node_Coords = new Double[Nodes.size()][2];
        Node_Id = new Long[Nodes.size()];

        System.out.println("Scan 2 of 2 in progress...");

        InputStream input2 = new BufferedInputStream(new FileInputStream(filename));
        PbfIterator iterator2 = new PbfIterator(input2, true);

        for (EntityContainer container : iterator2) {

            if(container.getType() == EntityType.Node){
                OsmNode node = (OsmNode) container.getEntity();
                if(Nodes.get(node.getId()) != null){

                    /** Set new index for this node */
                    Nodes.put(node.getId(), idx);

                    Node_Coords[idx][0] = node.getLatitude();
                    Node_Coords[idx][1] = node.getLongitude();

                    Node_Id[idx] = node.getId();

                    String keylat = "" + ((int) Math.floor(node.getLatitude() * 10));
                    String keylng = "" + ((int) Math.floor(node.getLongitude() * 10));
                    while (keylat.length() < 4) {keylat = "0" + keylat;}
                    while (keylng.length() < 4) {keylng = "0" + keylng;}

                    Integer key = Integer.parseInt("" + keylat + keylng);


                    ArrayList<Long> buffer;
                    if(Frames.get(key)!=null){
                        buffer = Frames.get(key);
                    }
                    else{
                        buffer = new ArrayList<Long>();
                    }
                    buffer.add(node.getId());
                    Frames.put(key, buffer);

                    if(node.getLatitude() > Max_Lat){
                        Max_Lat = node.getLatitude();
                    }
                    if(node.getLatitude() < Min_Lat){
                        Min_Lat = node.getLatitude();
                    }
                    if(node.getLongitude() > Max_Lng){
                        Max_Lng = node.getLongitude();
                    }
                    if(node.getLongitude() < Min_Lng){
                        Min_Lng = node.getLongitude();
                    }
                    idx ++;
                }
            }

            if (container.getType() == EntityType.Way){
                OsmWay way = (OsmWay) container.getEntity();
                if(checkStreet(way)){
                    for(int i = 0; i < way.getNumberOfNodes()-1; i++){
                        if(isTwoWay(way) != -1){
                            Long[] buff1 = {way.getNodeId(i), way.getNodeId(i+1)};
                            EdgesArrayList.add(buff1);
                        }
                        if(isTwoWay(way) != 0){
                            Long[] buff2 = {way.getNodeId(i+1), way.getNodeId(i)};
                            EdgesArrayList.add(buff2);
                        }
                    }
                }
            }
        }

        input2.close();


    
        System.out.println("Postprocessing in progress...");

        idx = 0;

        graph.initOffsetFrames(Frames.size());
        int FramesLength = Frames.size();
        graph.initNodes(Nodes.size());
        int NodesLength = Nodes.size();
        graph.initEdges(EdgesArrayList.size());
        int EdgesLength = EdgesArrayList.size();

       // Offset_Frames_Final = new Integer[Frames.size()][2];

        //Node_Id_Final = new Long[Nodes.size()];
        //Node_Coords_Final = new Double[Nodes.size()][2];
        // Offset_Edges_Final = new Integer[Nodes.size()];
        
        // Edges_Final = new Integer[EdgesArrayList.size()];

        Integer[] sortFrames = new Integer[Frames.size()];

        for(Integer frame_id : Frames.keySet()){
            sortFrames[idx] = frame_id;
            idx ++;
        }

        Arrays.sort(sortFrames);

        idx = 0;

        for(int i = 0 ; i < Frames.size(); i ++){
            graph.setOffsetFrames(i, sortFrames[i], idx);
            for(Long node_id : Frames.get(sortFrames[i])){
                graph.setNodeId(idx, node_id);
                Integer index = Nodes.get(node_id);
                Double lat = Node_Coords[index][0];
                Double lng = Node_Coords[index][1];
                graph.setNodeCoords(idx, lat, lng);
                Nodes.put(node_id, idx);
                idx ++;
            }
        }

        Frames = null;
        Node_Id = null;
        Node_Coords = null;

        for(Long[] edge : EdgesArrayList){
            edge[0] = (long) Nodes.get(edge[0]);
            edge[1] = (long) Nodes.get(edge[1]);
        }

        

        Nodes = null;
        System.gc();

        EdgesArray = EdgesArrayList.toArray(new Long[EdgesArrayList.size()][2]);
        EdgesArrayList = null;

        Arrays.sort(EdgesArray, new java.util.Comparator<Long[]>() {
			public int compare(Long[] a, Long[] b) {
				return Long.compare(a[0], b[0]);
			}
        });

        idx = 0;
        
        for(Integer i = 0; i < NodesLength; i ++){
            graph.setOffsetEdges(i, idx);
            // Offset_Edges_Final[i] = idx;
            while(idx < EdgesArray.length && EdgesArray[idx][0] == (long) i){
                graph.setEdges(idx, Math.toIntExact(EdgesArray[idx][1]));
                // Edges_Final[idx] = Math.toIntExact(EdgesArray[idx][1]);
                idx ++;
            }
        }

        // Edges_Length_Final = new Double[EdgesLength];
        

        calculateEdgeLength(EdgesLength);

        EdgesArray = null;


        System.out.println("Number of Frames: " + FramesLength);
        System.out.println("Number of Nodes: " + NodesLength);
        System.out.println("Number of Edges: " + EdgesLength);

        System.out.println("Pbf File Reader finished.");


        
    }


    private boolean checkStreet(OsmWay way){
        for(int i = 0; i < way.getNumberOfTags(); i ++){
            OsmTag tag = way.getTag(i);
            if(tag.getKey().equals("highway")){
                boolean ret = Arrays.stream(HighwayTagsArray).anyMatch(tag.getValue()::equals);
                return ret;
            }
        }
        return false;
    }



    private int isTwoWay(OsmWay way){

        for(int i = 0; i < way.getNumberOfTags(); i ++){
            OsmTag tag = way.getTag(i);
            if(tag.getKey().equals("highway")){
                if(Arrays.stream(OneWayTagsArray).anyMatch(tag.getValue()::equals)){
                    return 0;
                }
            }
            if(tag.getKey().equals("oneway")){
                if(tag.getValue().equals("yes")){
                    return 0;
                }
                if(tag.getValue().equals("-1")){
                    return -1;
                }
                if(tag.getValue().equals("no")){
                    return 1;
                }
            }
        }
        return 1;
    }


    private void calculateEdgeLength(int EdgesLength){

        

        for(int i = 0; i < EdgesLength; i ++){
            // double lat1 = Node_Coords_Final[Math.toIntExact(EdgesArray[i][0])][0];
            // double lng1 = Node_Coords_Final[Math.toIntExact(EdgesArray[i][0])][1];
            // double lat2 = Node_Coords_Final[Math.toIntExact(EdgesArray[i][1])][0];
            // double lng2 = Node_Coords_Final[Math.toIntExact(EdgesArray[i][1])][1];

            double lat1 = graph.getNodeLat(Math.toIntExact(EdgesArray[i][0]));
            double lng1 = graph.getNodeLng(Math.toIntExact(EdgesArray[i][0]));
            double lat2 = graph.getNodeLat(Math.toIntExact(EdgesArray[i][1]));
            double lng2 = graph.getNodeLng(Math.toIntExact(EdgesArray[i][1]));
            double dist = calculateDistance(lat1, lng1, lat2, lng2);

            graph.setEdgesLength(i, dist);
            // Edges_Length_Final[i] = dist;
        }

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


}




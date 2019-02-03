


package julianhoerz;

public class Graph {

    private int[][] Offset_Frames_Final;

    private long[] Node_Id_Final;
    private double[][] Node_Coords_Final;
    private int[] Offset_Edges_Final;

    private int[] Edges_Final;
    private double[] Edges_Length_Final;


    private int[] HighwayTagsArrayNum;
    private String[] HighwayTagsName;

    public Graph(){

    }


    /** HighwayTagsArrayNumber */
    public void setHighwayTags(int[] array){
        this.HighwayTagsArrayNum = array;
    }

    public void setHighwayTagsName(String[] array){
        this.HighwayTagsName = array;
    }

    public int[] getHighwayTags(){
        return this.HighwayTagsArrayNum;
    }

    public String[] getHighwayTagsName(){
        return this.HighwayTagsName;
    }



    /** Offset Frames */

    public void initOffsetFrames(int size){
        Offset_Frames_Final = new int[size][2];
    }

    public void setOffsetFrames(int index, int key, int offset){
        Offset_Frames_Final[index][0] = key;
        Offset_Frames_Final[index][1] = offset;
    }

    public int getOffsetFrames(int index, int select){
        return Offset_Frames_Final[index][select];
    }

    public int getFramesLength(){
        return Offset_Frames_Final.length;
    }





    /** Nodes */

    public void initNodes(int size){
        Node_Id_Final = new long[size];
        Node_Coords_Final = new double[size][2];
        Offset_Edges_Final = new int[size];
    }

    public void setNodeId(int index, long id){
        Node_Id_Final[index] = id;
    }

    public void setNodeCoords(int index, double lat, double lng){
        Node_Coords_Final[index][0] = lat;
        Node_Coords_Final[index][1] = lng;
    }

    public void setOffsetEdges(int index, int offset){
        Offset_Edges_Final[index] = offset;
    }

    public double getNodeLat(int index){
        return Node_Coords_Final[index][0];
    }

    public double getNodeLng(int index){
        return Node_Coords_Final[index][1];
    }

    public long getNodeId(int index){
        return Node_Id_Final[index];
    }

    public int getNodeOffset(int index){
        if(index >= Offset_Edges_Final.length){
            return Edges_Final.length;
        }
        return Offset_Edges_Final[index];
    }

    public int getNodesLength(){
        return Node_Id_Final.length;
    }





    /** Edges */

    public void initEdges(int size){
        Edges_Final = new int[size];
        Edges_Length_Final = new double[size];
    }

    public void setEdges(int index, int reference){
        Edges_Final[index] = reference;
    }

    public void setEdgesLength(int index, double length){
        Edges_Length_Final[index] = length;
    }

    public int getEdges(int index){
        return Edges_Final[index];
    }

    public double getEdgesLength(int index){
        return Edges_Length_Final[index];
    }

    public int getEdgesLength(){
        return Edges_Final.length;
    }




}




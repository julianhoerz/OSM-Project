


package julianhoerz;

public class Graph {

    private Integer[][] Offset_Frames_Final;

    private Long[] Node_Id_Final;
    private Double[][] Node_Coords_Final;
    private Integer[] Offset_Edges_Final;

    private Integer[] Edges_Final;
    private Double[] Edges_Length_Final;
    private Integer[] HighwayTagsArrayNum;
    private String[] HighwayTagsName;

    public Graph(){

    }


    /** HighwayTagsArrayNumber */
    public void setHighwayTags(Integer[] array){
        this.HighwayTagsArrayNum = array;
    }

    public void setHighwayTagsName(String[] array){
        this.HighwayTagsName = array;
    }

    public Integer[] getHighwayTags(){
        return this.HighwayTagsArrayNum;
    }

    public String[] getHighwayTagsName(){
        return this.HighwayTagsName;
    }



    /** Offset Frames */

    public void initOffsetFrames(int size){
        Offset_Frames_Final = new Integer[size][2];
    }

    public void setOffsetFrames(int index, int key, int offset){
        Offset_Frames_Final[index][0] = key;
        Offset_Frames_Final[index][1] = offset;
    }

    public Integer getOffsetFrames(int index, int select){
        return Offset_Frames_Final[index][select];
    }

    public Integer getFramesLength(){
        return Offset_Frames_Final.length;
    }





    /** Nodes */

    public void initNodes(int size){
        Node_Id_Final = new Long[size];
        Node_Coords_Final = new Double[size][2];
        Offset_Edges_Final = new Integer[size];
    }

    public void setNodeId(int index, Long id){
        Node_Id_Final[index] = id;
    }

    public void setNodeCoords(int index, Double lat, Double lng){
        Node_Coords_Final[index][0] = lat;
        Node_Coords_Final[index][1] = lng;
    }

    public void setOffsetEdges(int index, int offset){
        Offset_Edges_Final[index] = offset;
    }

    public Double getNodeLat(int index){
        return Node_Coords_Final[index][0];
    }

    public Double getNodeLng(int index){
        return Node_Coords_Final[index][1];
    }

    public Long getNodeId(int index){
        return Node_Id_Final[index];
    }

    public Integer getNodeOffset(int index){
        return Offset_Edges_Final[index];
    }

    public Integer getNodesLength(){
        return Node_Id_Final.length;
    }





    /** Edges */

    public void initEdges(int size){
        Edges_Final = new Integer[size];
        Edges_Length_Final = new Double[size];
    }

    public void setEdges(int index, int reference){
        Edges_Final[index] = reference;
    }

    public void setEdgesLength(int index, Double length){
        Edges_Length_Final[index] = length;
    }

    public Integer getEdges(int index){
        return Edges_Final[index];
    }

    public Double getEdgesLength(int index){
        return Edges_Length_Final[index];
    }

    public Integer getEdgesLength(){
        return Edges_Final.length;
    }




}




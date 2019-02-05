package julianhoerz;

import java.util.ArrayList;

public class Draw {

    private Graph graph;

    Draw(Graph graph){
        this.graph = graph;
    }


    public String buildGeoJson(ArrayList<double[]> coordinates, double startLat, double startLng, double endLat, double endLng){
        String geojson = "";

        String prestring = "";
        String poststring = "";
        String data = "";
        String position ="";

        prestring = "{\"type\": \"FeatureCollection\",\"features\": [{\"type\": \"Feature\",\"properties\": {},\"geometry\": {\"type\": \"LineString\",\"coordinates\": [";

        poststring = "]}}]}";


        for(int i = 0; i < coordinates.size(); i++){
            position = "[";
            position = position + coordinates.get(i)[1] + "";
            position = position + ",";
            position = position + coordinates.get(i)[0] + "";
            data = data + position + "]";
            if(i < coordinates.size()-1){
                data = data + ",";
            }
        }

        geojson = prestring + data + poststring;

        return geojson;
    }




    

    // public String generateGeoJson(ArrayList<Integer> routenodes,NodeProj startProj, NodeProj endProj,double startLat, double startLng, double endLat, double endLng){
    public String generateGeoJson(ArrayList<Integer> routenodes,NodeProj startProj, NodeProj endProj){
        String geojson = "";

        String prestring = "";
        String poststring = "";
        String data = "";
        String position ="";

        prestring = "{\"type\": \"FeatureCollection\",\"features\": [{\"type\": \"Feature\",\"properties\": {},\"geometry\": {\"type\": \"LineString\",\"coordinates\": [";

        poststring = "]}}]}";

        if(startProj.getProjectedCoords()[0] != -1){
            data = "[" + startProj.getProjectedCoords()[1] + "," + startProj.getProjectedCoords()[0] + "]";
        }
        if(routenodes.size() > 0){
            data = data + ",";
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
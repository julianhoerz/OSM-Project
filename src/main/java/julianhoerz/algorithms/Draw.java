package julianhoerz.algorithms;

import java.util.ArrayList;

public class Draw {


    public Draw(){
    }


    /**
     * Builds up a geojson including all points in the coordinates array.
     * 
     * @param coordinates
     * @param startLat
     * @param startLng
     * @param endLat
     * @param endLng
     * @return
     */
    public String buildGeoJson(ArrayList<double[]> coordinates, double startLat, double startLng, double endLat, double endLng){
        /**
         * Right now: Just coordinates array is used.
         * Nice to have: include also the initial coordinates and draw a dotted line
         */
        


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

}
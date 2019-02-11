package julianhoerz.algorithms;

public class MathFunctions{



    public MathFunctions(){}


    /**
     * Returns True if all entries of booleanArray
     * are false.
     * @param booleanArray
     * @return
     */
    public boolean checkFalse(boolean[] booleanArray){
        boolean result = true;
        for(int i = 0; i < booleanArray.length; i ++){
            if(booleanArray[i] == true){
                result = false;
                break;
            }
        }
        return result;
    }


    /**
     * Returns True if all entries of booleanArray
     * are true.
     * @param booleanArray
     * @return
     */
    public boolean checkTrue(boolean[] booleanArray){
        boolean result = true;
        for(int i = 0; i < booleanArray.length; i ++){
            if(booleanArray[i] == false){
                result = false;
                break;
            }
        }
        return result;
    }

        /**
     * Provides an orthogonal projection of the initial coordinates of 
     * proj onto the line node1-node2. If no orthogonal projection is 
     * possible return with projection coordinates -1.
     * 
     */
    public NodeProj projection(NodeProj proj){

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

    /**
     * Calculate Distance in meters between two coordinates
     * @param coord1
     * @param coord2
     * @return
     */
    public double calculateDistance(double[] coord1, double[] coord2){
        return calculateDistance(coord1[0], coord1[1], coord2[0], coord2[1]);
    }

    /**
     * Calculate Distance in meters between two coordinates
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2){
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
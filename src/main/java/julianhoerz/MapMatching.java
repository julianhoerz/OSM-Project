
package julianhoerz;

import java.util.ArrayList;

public class MapMatching{

    private Graph graph;
    private double sigma;
    private double betha;
    private double discDistance; //Discard Distance for looking around GPS Data
    private MathFunctions mathFunctions;


    MapMatching(Graph graph){
        this.graph = graph;
        this.sigma = 4; //Proposed by Paper
        this.betha = 4; //Proposed by Paper
        this.discDistance = 200; //Proposed by Paper
        this.mathFunctions = new MathFunctions();
    }


    public void startMapMatching(ArrayList<double[]> rawData){
        // Check data...
        //if(rawData)
        System.out.println("Start MapMatching");
        int observationsNumber = rawData.size();
        ArrayList<Observation> observations = new ArrayList<Observation>();
        
        for(int i = 0; i < observationsNumber; i ++){
            observations.add(new Observation(rawData.get(i)[0],rawData.get(i)[1]));
        }

        System.out.println("Find Candidates...");
        for(int i = 0; i < observationsNumber; i ++){
            findCandidates(observations.get(i));
        }
    }



    private void findCandidates(Observation observation){
        int[][] keys = this.graph.findFrames(observation.getLat(),observation.getLng());

        if(keys[0][0] == -1){
            System.out.println("No Frame found in this map");
            System.out.println("ERROR: No route found...");
        }

        double dist;
        NodeProj projection = new NodeProj();

        for(int p = 1; p < 9 ; p ++){
            for(int nodeid = keys[p][0] ; nodeid < keys[p][1]; nodeid ++){

                projection.setN1Coords(graph.getNodeLat(nodeid), graph.getNodeLng(nodeid));
                projection.setN1ID(nodeid);
                dist = calculateDistance(projection.getN1Coords()[0], projection.getN1Coords()[1], observation.getLat(), observation.getLng());

                if(dist < this.discDistance){
                    double probability = calcCandidateProbability(dist);
                    observation.addCandidate(new Candidate(new NodeProj(projection), probability));
                }

                int startindex = graph.getNodeOffset(nodeid); 
                int endindex = graph.getNodeOffset(nodeid+1);
                for(int i = startindex; i < endindex; i ++){
                    int nodeid2 = graph.getEdges(i);
                    projection.setN2Coords(graph.getNodeLat(nodeid2), graph.getNodeLng(nodeid2));
                    projection.setN2ID(nodeid2);
                    projection = mathFunctions.projection(projection);
                    if(projection.getProjectedCoords()[0] != -1d){
                        dist = calculateDistance(projection.getProjectedCoords()[0], projection.getProjectedCoords()[1], observation.getLat(), observation.getLng());
                        if(dist < this.discDistance){
                            double probability = calcCandidateProbability(dist);
                            observation.addCandidate(new Candidate(new NodeProj(projection), probability));
                        }
                    }
                }
            }
        }
    }

    private double calcCandidateProbability(double distance){

        return 1/(Math.sqrt(2*Math.PI)*this.sigma)*Math.exp(-0.5*(distance/this.sigma));
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
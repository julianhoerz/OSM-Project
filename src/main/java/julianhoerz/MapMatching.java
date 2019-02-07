
package julianhoerz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.text.html.HTMLDocument.BlockElement;

public class MapMatching{

    private Graph graph;
    private double sigma;
    private double betha;
    private double discDistance; //Discard Distance for looking around GPS Data
    private MathFunctions mathFunctions;
    private Dijkstra dijkstra;


    MapMatching(Graph graph){
        this.graph = graph;
        this.sigma = 4; //Proposed by Paper
        this.betha = 4; //Proposed by Paper
        this.discDistance = 200; //Proposed by Paper
        this.mathFunctions = new MathFunctions();
        this.dijkstra = new Dijkstra(graph);
    }


    public ArrayList<double[]> startMapMatching(ArrayList<double[]> rawData){
        // Check data...
        //if(rawData)

        System.out.println("Start MapMatching");

        rawData = preProcessing(rawData);

        int observationsNumber = rawData.size();
        ArrayList<Observation> observations = new ArrayList<Observation>();
        
        for(int i = 0; i < observationsNumber; i ++){
            observations.add(new Observation(rawData.get(i)[0],rawData.get(i)[1]));
        }

        System.out.println("Find Candidates...");
        for(int i = 0; i < observationsNumber; i ++){
            findCandidates(observations.get(i));
        }

        System.out.println("Calc Transistion Probabilities...");
        ArrayList<double[][]> transitionMatrices = new ArrayList<double[][]>();
        for(int i = 0; i < observationsNumber-1; i++){
            double[][] transitionMatrix;
            transitionMatrix = calcTransitionProbability(observations.get(i),observations.get(i+1));
            transitionMatrices.add(transitionMatrix);
        }

        System.out.println("Start Viterbi Algorithm...");
        ArrayList<double[][]> viterbiReferences = new ArrayList<double[][]>();
        /** Init ViterbiReferences with start candidates and their probabilities*/
        int candidatesNumber = observations.get(0).getCandidatesNumber();
        double[][] elem = new double[candidatesNumber][2];
        for(int i = 0; i < candidatesNumber; i ++){
            elem[i][0] = observations.get(0).getCandidate(i).getProbability();
            elem[i][1] = -1;
        }
        viterbiReferences.add(elem);

        /** Start Viterbi... */
        for(int i = 0; i < observationsNumber-1; i++){
            double[][] finalcost = viterbiAlgorithmFwd(viterbiReferences.get(i),observations.get(i+1),transitionMatrices.get(i));
            viterbiReferences.add(finalcost);
        }

        ArrayList<double[]> coordinates = viterbiAlgorithmBwd(viterbiReferences,observations);

        System.out.println("Map Matching Finished");
    
        return coordinates;

    }


    private ArrayList<double[]> viterbiAlgorithmBwd(ArrayList<double[][]> refs, ArrayList<Observation> observations){
        int coordsNumber = refs.size();
        ArrayList<double[]> coordinates = new ArrayList<double[]>();
        double[] coordinate;
        double max = 0;
        int startindex = -1;
        double[][] elem = refs.get(coordsNumber-1);
        for(int i = 0; i < elem.length; i ++){
            if(elem[i][0] > max){
                max = elem[i][0];
                startindex = i;
            }
        }

        int nextindex;
        for(int i = coordsNumber-1; i >= 0; i--){
            nextindex = (int) refs.get(i)[startindex][1];
            coordinate= observations.get(i).getCandidate(startindex).getPosition().getCoordinates();
            coordinates.add(coordinate);
            startindex = nextindex;
        }


        Collections.reverse(coordinates);
        return coordinates;
    }


    private double[][] viterbiAlgorithmFwd(double[][] initialCost, Observation observation, double[][] transitionMatrix){
        int initNumber = initialCost.length;
        int candidatesNumber = observation.getCandidatesNumber();
        double[][] finalCost = new double[candidatesNumber][2];

        double probability;
        for(int i = 0; i < candidatesNumber; i ++){
            finalCost[i][0] = 0;
            for(int p = 0; p < initNumber; p ++){
                probability = initialCost[p][0] + transitionMatrix[p][i];
                if(probability>finalCost[i][0]){
                    finalCost[i][0] = probability;
                    finalCost[i][1] = p; 
                }
            }
            finalCost[i][0] += observation.getCandidate(i).getProbability();
        }

        return finalCost;
    }


    private double[][] calcTransitionProbability(Observation observe1, Observation observe2){
        int candidates1 = observe1.getCandidatesNumber();
        int candidates2 = observe2.getCandidatesNumber();

        double[][] matrix = new double[candidates1][candidates2];
        double distance, directDistance, pathDistance;
        for(int i = 0 ; i < candidates1 ; i ++){
            for(int p = 0 ; p < candidates2 ; p++){
                directDistance = calculateDistance(observe1.getLat(), observe1.getLng(), observe2.getLat(), observe2.getLng());
                pathDistance = this.dijkstra.dijkstraDistance(observe1.getCandidate(i).getPosition(),observe2.getCandidate(p).getPosition());
                distance = Math.abs(directDistance - pathDistance);
                matrix[i][p] = 1/this.betha * Math.exp(-distance/this.betha);
            }
        }

        return matrix;
    }



    private void findCandidates(Observation observation){
        int[][] keys = this.graph.findFrames(observation.getLat(),observation.getLng());

        if(keys[0][0] == -1){
            System.out.println("No Frame found in this map");
            System.out.println("ERROR: No route found...");
        }

        double dist;
        NodeProj projection = new NodeProj();
        projection.setInitialCoords(observation.getLat(), observation.getLng());

        HashMap<Integer,Integer[]> checked = new HashMap<Integer,Integer[]>();

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
                    
                    // String multipleid = nodeid2 + nodeid + "";
                    if(checked.containsKey(nodeid2)){
                        Boolean done = false;
                        Integer[] test = checked.get(nodeid2);
                        for(int r = 0; r < test.length; r++){
                            if(test[r] == nodeid){
                                done = true;
                            }
                        }
                        if(done){
                            continue;
                        }
                    }
                    // multipleid = nodeid + nodeid2 + "";
                    // checked.put(Integer.parseInt(multipleid), true);
                    projection.setN2Coords(graph.getNodeLat(nodeid2), graph.getNodeLng(nodeid2));
                    projection.setN2ID(nodeid2);
                    projection = mathFunctions.projection(projection);
                    if(projection.getProjectedCoords()[0] != -1d){
                        dist = calculateDistance(projection.getProjectedCoords()[0], projection.getProjectedCoords()[1], observation.getLat(), observation.getLng());
                        if(dist < this.discDistance){
                            System.out.println("Candidate Coords: " + projection.getProjectedCoords()[0] + "," + projection.getProjectedCoords()[1]);
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


    private ArrayList<double[]> preProcessing(ArrayList<double[]> rawData){
        ArrayList<double[]> processedData = new ArrayList<double[]>();
        double dist;
        processedData.add(rawData.get(0));
        for(int i= 0; i < rawData.size()-1; i++){
            dist = this.mathFunctions.calculateDistance(rawData.get(i), rawData.get(i+1));
            if(dist > 2*this.sigma){
                processedData.add(rawData.get(i+1));
            }
        }

        return processedData;
    }

}
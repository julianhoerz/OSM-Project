
package julianhoerz.algorithms;

import julianhoerz.datastructure.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MapMatching{

    private Graph graph;
    private double sigma;
    private double betha;
    private double discDistance; //Discard Distance for looking around GPS Data
    private MathFunctions mathFunctions;
    private Dijkstra dijkstra;
    private int kNN;

    /** Constructor for standard parameters */
    public MapMatching(Graph graph){
        this.graph = graph;
        this.sigma = 4; //Proposed by Paper
        this.betha = 4; //Proposed by Paper
        this.discDistance = 200; //Proposed by Paper
        this.mathFunctions = new MathFunctions();
        this.dijkstra = new Dijkstra(graph);
        this.kNN = 5;
    }

    /** Constructor for userspecific parameters */
    public MapMatching(Graph graph, int sigma, int beta, int kNN){
        this.graph = graph;
        this.sigma = sigma; //Proposed by Paper
        this.betha = beta; //Proposed by Paper
        this.kNN = kNN;
        this.discDistance = 200; //Proposed by Paper
        this.mathFunctions = new MathFunctions();
        this.dijkstra = new Dijkstra(graph);
    }


    /**
     * Mapmatching Process containing the following steps:
     * - Preprocessing
     * - Read Observations (rawData)
     * - Find Candidates
     * - Calculate Candidate Probabilities
     * - Calculate Transistion Probabilities
     * - Viterbi to find most probable route
     */
    public ArrayList<double[]> startMapMatching(ArrayList<double[]> rawData){
        // Check data...
        //if(rawData)

        System.out.println("Start MapMatching");
        System.out.println("Parameters: Sigma:" + this.sigma + " Beta: " + this.betha + " kNN: " + this.kNN);

        rawData = preProcessing(rawData);

        int observationsNumber = rawData.size();
        ArrayList<Observation> observations = new ArrayList<Observation>();
        
        for(int i = 0; i < observationsNumber; i ++){
            observations.add(new Observation(rawData.get(i)[0],rawData.get(i)[1],this.kNN));
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

        System.out.println("Start Viterbi Fwd-Algorithm...");
        ArrayList<double[][]> viterbiReferences = new ArrayList<double[][]>();
        /** Init ViterbiReferences with start candidates and their probabilities*/
        int candidatesNumber = observations.get(0).getCandidatesNumber();
        double[][] elem = new double[candidatesNumber][2];
        double maxprob = 0;
        for(int i = 0; i < candidatesNumber; i ++){
            elem[i][0] = observations.get(0).getCandidate(i).getProbability();
            elem[i][1] = -1;
            if(elem[i][0] > maxprob){
                maxprob = elem[i][0];
            }
        }
        for(int i = 0; i < candidatesNumber; i ++){
            elem[i][0] /=  maxprob;
        }
        viterbiReferences.add(elem);

        /** Start Viterbi... */
        for(int i = 0; i < observationsNumber-1; i++){
            double[][] finalcost = viterbiAlgorithmFwd(viterbiReferences.get(i),observations.get(i+1),transitionMatrices.get(i));
            viterbiReferences.add(finalcost);
        }


        System.out.println("Start Viterbi Bwd-Algorithm...");
        ArrayList<NodeProj> coordinates = viterbiAlgorithmBwd(viterbiReferences,observations);

        System.out.println("Compose Route...");
        ArrayList<double[]> composedRoute = new ArrayList<double[]>();
        ArrayList<double[]> buff;
        for(int i = 0; i < coordinates.size()-1; i++){
            buff = this.dijkstra.oneToOneDijkstra(coordinates.get(i), coordinates.get(i+1));
            for(int p = 0; p < buff.size(); p ++){
                composedRoute.add(buff.get(p));
            }
        }

        System.out.println("Map Matching Finished");
    
        return composedRoute;

    }

    /**
     * Reads out the most probable candidate of each observation
     * @param refs
     * @param observations
     * @return
     */
    private ArrayList<NodeProj> viterbiAlgorithmBwd(ArrayList<double[][]> refs, ArrayList<Observation> observations){
        int coordsNumber = refs.size();
        ArrayList<NodeProj> coordinates = new ArrayList<NodeProj>();
        NodeProj coordinate;
        double max = 0;
        int startindex = -1;
        double[][] elem = refs.get(coordsNumber-1);
        for(int i = 0; i < elem.length; i ++){
            if(elem[i][0] > max){
                max = elem[i][0];
                startindex = i;
            }
        }
        if(startindex == -1){
            System.out.println("No Matched Route found...");
        }

        int nextindex;
        for(int i = coordsNumber-1; i >= 0; i--){
            nextindex = (int) refs.get(i)[startindex][1];
            coordinate= observations.get(i).getCandidate(startindex).getPosition();
            coordinates.add(coordinate);
            startindex = nextindex;
        }


        Collections.reverse(coordinates);
        return coordinates;
    }

    /**
     * Calculates the probability 
     * @param initialCost
     * @param observation
     * @param transitionMatrix
     * @return
     */
    private double[][] viterbiAlgorithmFwd(double[][] initialCost, Observation observation, double[][] transitionMatrix){
        int initNumber = initialCost.length;
        int candidatesNumber = observation.getCandidatesNumber();
        double[][] finalCost = new double[candidatesNumber][2];
        double maxprob = 0;
        double probability;
        for(int i = 0; i < candidatesNumber; i ++){
            finalCost[i][0] = 0;
            for(int p = 0; p < initNumber; p ++){
                probability = initialCost[p][0] * transitionMatrix[p][i];
                if(probability>finalCost[i][0]){
                    finalCost[i][0] = probability;
                    finalCost[i][1] = p; 
                }
            }
            finalCost[i][0] *= observation.getCandidate(i).getProbability();
            if(finalCost[i][0] > maxprob){
                maxprob = finalCost[i][0];
            }
        }
        for(int i = 0; i < candidatesNumber; i ++){
            finalCost[i][0] /= maxprob;
        }

        return finalCost;
    }

    /**
     * Calculate all Transisiton Probabilities between the candidates of 
     * two observations and returns the transition matrix. 
     * @param observe1
     * @param observe2
     * @return
     */
    private double[][] calcTransitionProbability(Observation observe1, Observation observe2){
        int candidates1 = observe1.getCandidatesNumber();
        int candidates2 = observe2.getCandidatesNumber();

        double[][] matrix = new double[candidates1][candidates2];
        double distance, directDistance;
        double[] pathDistance;
        ArrayList<NodeProj> endPoints;
        directDistance = this.mathFunctions.calculateDistance(observe1.getLat(), observe1.getLng(), observe2.getLat(), observe2.getLng());
        for(int i = 0 ; i < candidates1 ; i ++){
            endPoints = new ArrayList<NodeProj>();
            for(int p = 0 ; p < candidates2 ; p++){
                endPoints.add(observe2.getCandidate(p).getPosition());
            }
            pathDistance = this.dijkstra.dijkstraDistance(observe1.getCandidate(i).getPosition(),endPoints);
            for(int p = 0 ; p < candidates2 ; p++){
                distance = Math.abs(directDistance - pathDistance[p]);
                matrix[i][p] = 1/this.betha * Math.exp(-distance/this.betha);
            }
        }

        return matrix;
    }


    /**
     * Find the k nearest neighbors of an observation. This can be 
     * a node (e.g. intersection) or an orthogonal projection on a street.
     * It stores the k nearest neighbors in the observation datastructure
     * @param observation
     */
    private void findCandidates(Observation observation){
        int[][] keys = this.graph.findFrames(observation.getLat(),observation.getLng());

        if(keys[0][0] == -1){
            System.out.println("No Frame found in this map");
            System.out.println("ERROR: No route found...");
        }

        double dist;
        NodeProj projection = new NodeProj();
        projection.setInitialCoords(observation.getLat(), observation.getLng());

        HashMap<Integer,ArrayList<Integer>> checked = new HashMap<Integer,ArrayList<Integer>>();

        for(int p = 1; p < 9 ; p ++){
            for(int nodeid = keys[p][0] ; nodeid < keys[p][1]; nodeid ++){
                
                projection.setN1Coords(graph.getNodeLat(nodeid), graph.getNodeLng(nodeid));
                projection.setN1ID(nodeid);
                dist = this.mathFunctions.calculateDistance(projection.getN1Coords()[0], projection.getN1Coords()[1], observation.getLat(), observation.getLng());
                if(dist < this.discDistance){
                    double probability = calcCandidateProbability(dist);
                    observation.replaceCandidate(new Candidate(new NodeProj(projection), probability,dist));    
                }
                int startindex = graph.getNodeOffset(nodeid); 
                int endindex = graph.getNodeOffset(nodeid+1);
                ArrayList<Integer> paths = new ArrayList<Integer>();
                for(int i = startindex; i < endindex; i ++){
                    int nodeid2 = graph.getEdges(i);
                    paths.add(nodeid2);
                    if(checked.containsKey(nodeid2)){
                        if(checked.get(nodeid2).contains(nodeid)){
                            continue;
                        }
                    }

                    projection.setN2Coords(graph.getNodeLat(nodeid2), graph.getNodeLng(nodeid2));
                    projection.setN2ID(nodeid2);
                    projection = mathFunctions.projection(projection);
                    if(projection.getProjectedCoords()[0] != -1d){
                        dist = this.mathFunctions.calculateDistance(projection.getProjectedCoords()[0], projection.getProjectedCoords()[1], observation.getLat(), observation.getLng());
                        if(dist < this.discDistance){
                            //System.out.println("Candidate Coords: " + projection.getProjectedCoords()[0] + "," + projection.getProjectedCoords()[1]);
                            // if(observation.getHighestCandidateDistance() > dist){

                            // }
                            double probability = calcCandidateProbability(dist);
                            observation.replaceCandidate(new Candidate(new NodeProj(projection), probability,dist));
                        }
                    }
                }
                checked.put(nodeid, paths);
            }
        }
        // System.out.println("Candidatenumber: " + observation.getCandidatesNumber());
    }


    /**
     * Probability of an Candidate with respect to the distance to its observation
     * @param distance
     * @return
     */
    private double calcCandidateProbability(double distance){

        return 1/(Math.sqrt(2*Math.PI)*this.sigma)*Math.exp(-0.5*(distance/this.sigma));
    }


    /**
     * Preprocessing removes all observations that are closer than 2*sigma.
     * This only occurs when a high sampling rate is used.
     * @param rawData
     * @return
     */
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









/*

53.08005722583211, 8.73013973236084
53.08079837100151, 8.727795481681824
53.08243206768091, 8.72239351272583
53.09223294579872, 8.693103790283203



{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "properties": {},
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [
            8.73013973236084,
            53.08005722583211
          ],
          [
            8.727795481681824,
            53.08079837100151
          ],
          [
            8.72239351272583,
            53.08243206768091
          ],
          [
            8.693103790283203,
            53.09223294579872
          ]
        ]
      }
    }
  ]
}



*/
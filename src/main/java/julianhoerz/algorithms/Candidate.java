package julianhoerz.algorithms;


/**
 * Datastructure for a possible Candidate of the Mapmatching process
 * One candidate has:
 * Position: Candidate is always on the road
 * Probability: Depends on the distance to its observation
 * Distance: Distance to its observation
 */
public class Candidate implements Comparable<Candidate>{
    private NodeProj position;
    private double probability;
    private double distance;

    public Candidate(NodeProj position, double probability,double distance){
        this.position = new NodeProj(position);
        this.probability = probability;
        this.distance = distance;
    }

    /**
     * Sort Candidates by distance to get the k-Nearest Neighbors.
     */
    public int compareTo(Candidate o) {
		if(this.distance < o.distance) {
			return -1;
		}
		return 1;
	}

    public void setPosition(NodeProj position){
        this.position = position;
    }

    public NodeProj getPosition(){
        return this.position;
    }

    public void setDistance(double distance){
        this.distance = distance;
    }

    public double getDistance(){
        return this.distance;
    }

    public void setProbability(double probability){
        this.probability = probability;
    }

    public double getProbability(){
        return this.probability;
    }
}
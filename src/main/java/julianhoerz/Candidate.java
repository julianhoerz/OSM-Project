package julianhoerz;


public class Candidate{
    private NodeProj position;
    private double probability;

    Candidate(NodeProj position, double probability){
        this.position = new NodeProj(position);
        this.probability = probability;
    }

    public void setPosition(NodeProj position){
        this.position = position;
    }

    public NodeProj getPosition(){
        return this.position;
    }

    public void setProbability(double probability){
        this.probability = probability;
    }

    public double getProbability(){
        return this.probability;
    }
}

package julianhoerz;

import java.util.ArrayList;

public class Observation {
    private double latitude;
    private double longitude;

    private int candidatesNumber;
    private ArrayList<Candidate> candidates;

    Observation(double lat, double lng){
        this.latitude = lat;
        this.longitude = lng;
        this.candidatesNumber = 0;
        candidates = new ArrayList<Candidate>();
    }

    public void setLat(double lat){
        this.latitude = lat;
    }

    public double getLat(){
        return this.latitude;
    }

    public int getCandidatesNumber(){
        return this.candidatesNumber;
    }

    public void setLng(double lng){
        this.longitude = lng;
    }

    public double getLng(){
        return this.longitude;
    }

    public void addCandidate(Candidate candidate){
        this.candidates.add(candidate);
        this.candidatesNumber = this.candidates.size();
    }

    public Candidate getCandidate(int i){
        if(i >= this.candidates.size()){
            return null;
        }
        return this.candidates.get(i);
    }

}
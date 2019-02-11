
package julianhoerz.algorithms;

import java.util.ArrayList;
import java.util.Collections;

public class Observation {
    private double latitude;
    private double longitude;

    private int candidatesNumber;
    private int maxCandidatesNumber;
    private ArrayList<Candidate> candidates;

    public Observation(double lat, double lng){
        this.latitude = lat;
        this.longitude = lng;
        this.candidatesNumber = 0;
        this.maxCandidatesNumber = 5;
        candidates = new ArrayList<Candidate>();
    }

    public Observation(double lat, double lng, int maxCandidatesNumber){
        this.latitude = lat;
        this.longitude = lng;
        this.candidatesNumber = 0;
        this.maxCandidatesNumber = maxCandidatesNumber;
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

    public void replaceCandidate(Candidate candidate){
        this.candidates.add(candidate);
        this.candidatesNumber = this.candidates.size();
        if(this.candidatesNumber > this.maxCandidatesNumber){
            Collections.sort(this.candidates);
            this.candidates.remove(this.candidatesNumber-1);
            this.candidatesNumber = this.candidates.size();
        }
    }

    public double getHighestCandidateDistance(){
        
        if(this.candidatesNumber == 0){
            return Double.POSITIVE_INFINITY;
        }
        Collections.sort(this.candidates);
        return candidates.get(this.candidatesNumber-1).getDistance();
    }

}
package julianhoerz.algorithms;

public class NodeProj{
    double[] initialCoords;
    double[] projectedCoords;

    double[] n1Coords;
    double[] n2Coords;

    int n1ID;
    int n2ID;

    /**
     * onewaytag: 
     * 1: n1 -> n2
     * -1: n2 -> n1
     * 0: twoway
     * 2: undefined
     */

    int oneway;

    public NodeProj() {
        this.initialCoords = new double[2];
        this.initialCoords[0] = -1.0;
        this.initialCoords[1] = -1.0;

        this.projectedCoords = new double[2];
        this.projectedCoords[0] = -1.0;
        this.projectedCoords[1] = -1.0;

        this.n1Coords = new double[2];
        this.n1Coords[0] = -1.0;
        this.n1Coords[1] = -1.0;

        this.n2Coords = new double[2];
        this.n2Coords[0] = -1.0;
        this.n2Coords[1] = -1.0;

        this.n1ID = -1;
        this.n2ID = -1;
        this.oneway = 2;
    }
    
    public NodeProj(NodeProj proj) {
        this.initialCoords = new double[2];
        this.initialCoords[0] = proj.getInitialCoords()[0];
        this.initialCoords[1] = proj.getInitialCoords()[1];

        this.projectedCoords = new double[2];
        this.projectedCoords[0] = proj.getProjectedCoords()[0];
        this.projectedCoords[1] = proj.getProjectedCoords()[1];

        this.n1Coords = new double[2];
        this.n1Coords[0] = proj.getN1Coords()[0];
        this.n1Coords[1] = proj.getN1Coords()[1];

        this.n2Coords = new double[2];
        this.n2Coords[0] = proj.getN2Coords()[0];
        this.n2Coords[1] = proj.getN2Coords()[1];

        this.n1ID = proj.getN1ID();
        this.n2ID = proj.getN2ID();
    }
    
    public double[] getCoordinates(){
        if(this.projectedCoords[0] == -1){
            return this.n1Coords;
        }
        else{
            return this.projectedCoords;
        } 
    }
	

    public void setInitialCoords(double initialLat, double initialLng){
        this.initialCoords[0] = initialLat;
        this.initialCoords[1] = initialLng;
        this.setProjectedCoords(-1.0, -1.0);
    }

    public double[] getInitialCoords(){
        return this.initialCoords;
    }

    public void setProjectedCoords(double projectedLat, double projectedLng){
        this.projectedCoords[0] = projectedLat;
        this.projectedCoords[1] = projectedLng;
    }

    public double[] getProjectedCoords(){
        return this.projectedCoords;
    }

    public void setN1Coords(double n1Lat, double n1Lng){
        this.n1Coords[0] = n1Lat;
        this.n1Coords[1] = n1Lng;
        this.setProjectedCoords(-1.0, -1.0);
        this.oneway = 2;
    }

    public double[] getN1Coords(){
        return this.n1Coords;
    }

    public void setN2Coords(double n2Lat, double n2Lng){
        this.n2Coords[0] = n2Lat;
        this.n2Coords[1] = n2Lng;
        this.setProjectedCoords(-1.0, -1.0);
        this.oneway = 2;
    }

    public double[] getN2Coords(){
        return this.n2Coords;
    }

    public void setN1ID(int n1ID){
        this.n1ID = n1ID;
    }

    public int getN1ID(){
        return this.n1ID;
    }

    public void setN2ID(int n2ID){
        this.n2ID = n2ID;
    }

    public int getN2ID(){
        return this.n2ID;
    }

    public int getOneway(){
        return this.oneway;
    }

    public void setOneway(int oneway){
        this.oneway = oneway;
    }

	
}
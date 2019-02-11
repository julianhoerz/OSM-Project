package julianhoerz.algorithms;

public class Node implements Comparable<Node>{
	double distance;
	int ID;
	int previous;
	
	public Node(double distance, int ID, int previous) {
		this.distance = distance;
		this.ID = ID;
		this.previous = previous;
	}

	public int compareTo(Node o) {
		if(this.distance < o.distance) {
			return -1;
		}
		return 1;
	}
	
}
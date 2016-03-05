package graph;

import interfaces.ILocation;
import element.Location;

import java.util.ArrayList;

/**
 * Created by Dimitri on 21/10/2015.
 */
public class Vertex implements ILocation, Comparable<Vertex> {
	int x, y;
	private Vertex previous;
	private double minDistance = Double.POSITIVE_INFINITY;
	private ArrayList<Edge> adjacencies;

	public Vertex(int x, int y) {
		this.x = x;
		this.y = y;
		adjacencies = new ArrayList<>();
	}

	public Vertex(Vertex vertex) {
		this.x = vertex.getX();
		this.y = vertex.getY();
		this.previous = vertex.getPrevious();
		this.minDistance = vertex.getMinDistance();
		this.adjacencies = vertex.getAdjacencies();
	}

	public ArrayList<Edge> getAdjacencies(){
		return adjacencies;
	}

	public void setMinDistance(double minDistance) {
		this.minDistance = minDistance;
	}

	public double getMinDistance() {
		return minDistance;
	}

	public void setPrevious(Vertex previous) {
		this.previous = previous;
	}

	public Vertex getPrevious() {
		return previous;
	}

	public Location getLocation(){
		return new Location(x, y);
	}

	@Override
    public int compareTo(Vertex vertex) {
		return Double.compare(minDistance, vertex.minDistance);
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}
}
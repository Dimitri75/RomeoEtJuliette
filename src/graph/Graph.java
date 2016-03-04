package graph;

import element.Location;
import element.MapElement;
import enumerations.EnumGraph;
import list.CircularQueue;

import java.util.*;

/**
 * Created by Dimitri on 21/10/2015.
 */
public class Graph {
    private int pace;
    private int width;
    private int height;
    private List<Vertex> listVertex;
    private List<MapElement> obstaclesList;
    private List<Edge> listEdges;

    public Graph(int width, int height, List<MapElement> obstaclesList, int pace) {
        this.width = width;
        this.height = height;
        this.obstaclesList = obstaclesList;
        this.pace = pace;

        listVertex = new ArrayList<>();
        listEdges = new ArrayList<>();

        init();
    }

    public List<Vertex> getListVertex() {
        return listVertex;
    }

    public List<Edge> getListEdges() {
        return listEdges;
    }

    /**
     * Adds an edge to the graph
     * @param source
     * @param target
     * @param movementSpeed
     * @return
     */
    private Edge addEdge(Vertex source, Vertex target, EnumGraph movementSpeed) {
        Edge edge = new Edge(source, target, movementSpeed);
        listEdges.add(edge);
        return edge;
    }

    /**
     * Adds a vertex to the graph
     * @param x
     * @param y
     * @return
     */
    private Vertex addVertex(int x, int y) {
        Vertex vertex = new Vertex(x, y);
        listVertex.add(vertex);
        return vertex;
    }

    /**
     * Returns the vertex corresponding at the given coordinates
     * @param x
     * @param y
     * @return
     */
    public Vertex getVertexByLocation(int x, int y) {
        return getVertexByLocation(new Location(x, y));
    }

    /**
     * Returns the vertex at the given location
     * @param location
     * @return
     */
    public Vertex getVertexByLocation(Location location) {
        for (Vertex vertex : listVertex)
            if (vertex.getX() == location.getX() && vertex.getY() == location.getY())
                return vertex;

        return null;
    }

    /**
     * Initialize the graph according to the map and the obstacles
     */
    public void init() {
        boolean noObstacles = true;
        for (int y = 0; y < height; y += pace) {
            Vertex leftVertex = null;
            for (int x = 0; x < width; x += pace) {
                noObstacles = true;
                for (MapElement obstacle : obstaclesList)
                    if (obstacle.getX() == x && obstacle.getY() == y)
                        noObstacles = false;

                Vertex tmpVertex = null;
                if (noObstacles) {
                    tmpVertex = addVertex(x, y);

                    if (leftVertex != null) {
                        addEdge(leftVertex, tmpVertex, EnumGraph.SPEED_NORMAL);
                        addEdge(tmpVertex, leftVertex, EnumGraph.SPEED_NORMAL);
                    }

                    Vertex upVertex;
                    if (y != 0 && (upVertex = getVertexByLocation(x, y - pace)) != null) {
                        addEdge(upVertex, tmpVertex, EnumGraph.SPEED_NORMAL);
                        addEdge(tmpVertex, upVertex, EnumGraph.SPEED_NORMAL);
                    }
                }
                leftVertex = tmpVertex;
            }
        }
    }

    /**
     * Reinitializes vertices distance and previous attribute
     */
    private void reinitVertices(){
        for (Vertex vertex : listVertex) {
            vertex.setMinDistance(Double.POSITIVE_INFINITY);
            vertex.setPrevious(null);
        }
    }

    /**
     * Returns the shortest path in the graph between two vertices using dijkstra algorithm
     * @param start
     * @param destination
     * @return
     */
    public List<Vertex> dijkstra(Vertex start, Vertex destination) {
        reinitVertices();

        // ComputePaths
        start.setMinDistance(0.);
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
        vertexQueue.add(start);
        while (!vertexQueue.isEmpty()) {
            Vertex current = vertexQueue.poll();
            for (Edge e : current.getAdjacencies()) {
                Vertex targetVertex = e.getTarget();
                double weight = e.getWeight();
                double distanceThroughCurrent = current.getMinDistance()
                        + weight;
                if (distanceThroughCurrent < targetVertex.getMinDistance()) {
                    vertexQueue.remove(targetVertex);
                    targetVertex.setMinDistance(distanceThroughCurrent);
                    targetVertex.setPrevious(current);
                    vertexQueue.add(targetVertex);
                }
            }
        }
        vertexQueue.clear();

        // GetShortestPath
        List<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex = destination; vertex != null; vertex = vertex
                .getPrevious())
            path.add(vertex);
        Collections.reverse(path);
        return path;
    }

    /**
     * Browses the graph using BFS method
     * @param start
     * @return
     */
    public CircularQueue browseBFS(Vertex start){
        List<Vertex> allVertices = new ArrayList<>(listVertex);

        LinkedList<Vertex> queue = new LinkedList<>();
        CircularQueue<Vertex> circularQueue = new CircularQueue<>(listVertex.size());

        queue.add(start);
        allVertices.remove(start);

        Vertex current, neighbor;
        while (!queue.isEmpty()) {
            current = circularQueue.addAndReturn(queue.poll());

            for (Edge edge : current.getAdjacencies()) {
                neighbor = edge.getTarget();
                if (allVertices.contains(neighbor)) {
                    queue.add(neighbor);
                    allVertices.remove(neighbor);
                }
            }
        }
        return circularQueue;
    }

    /**
     * Browses the graph recursively using DFS
     * @param currentVertex
     * @param allVertices
     */
    private void browseDFS(Vertex currentVertex, List<Vertex> allVertices, CircularQueue circularQueue){
        circularQueue.addAndReturn(currentVertex);
        allVertices.remove(currentVertex);

        if (allVertices.isEmpty())
            return;

        for (Edge e : currentVertex.getAdjacencies()) {
            if (allVertices.contains(e.getTarget())) {
                browseDFS(e.getTarget(), allVertices, circularQueue);
            }
        }
    }

    /**
     * Uses the DFS method to return a filled CircularQueue
     * @param start
     * @return
     */
    public CircularQueue browseDFS(Vertex start){
        List<Vertex> allVertices = new ArrayList<>(listVertex);
        CircularQueue<Vertex> circularQueue = new CircularQueue<>(listVertex.size());
        browseDFS(start, allVertices, circularQueue);
        return circularQueue;
    }

    /**
     * Returns a random vertex
     * @return
     */
    public Vertex getRandomVertex(){
        Random random = new Random();
        int randIndex = random.nextInt(listVertex.size());
        return listVertex.get(randIndex);
    }
}
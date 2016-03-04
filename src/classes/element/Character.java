package classes.element;

import classes.enumerations.Image;
import classes.enumerations.Position;
import classes.graph.Graph;
import classes.graph.Vertex;
import javafx.application.Platform;

import java.util.List;

/**
 * Created by Dimitri on 21/10/2015.
 */
public class Character extends MapElement implements Runnable {
    private Position position;
    private List<Vertex> path;
    private boolean actionDone;

    public Character(int x, int y, int shapeSize, Image image) {
        super(x, y, shapeSize, image);
        position = Position.RIGHT;
        actionDone = true;
    }

    public boolean isActionDone() {
        return actionDone;
    }

    public void setActionDone(boolean actionDone) {
        this.actionDone = actionDone;
    }

    public void changePosition() {
        if (position.equals(Position.LEFT)) {
            position = Position.RIGHT;
        } else {
            position = Position.LEFT;
        }
    }

    public Position getPosition() {
        return position;
    }

    public void initPath(Graph graph, Vertex start, Vertex destination) {
        actionDone = false;

        if (path != null)
            path.clear();
        path = graph.dijkstra(start, destination);
    }

    public void setLocation(Location location){
        setX(location.getX());
        setY(location.getY());
    }

    @Override
    public void setX(int x) {
        if (x < getX() && position.equals(Position.RIGHT)) {
            changePosition();
        } else if (x > getX() && position.equals(Position.LEFT)) {
            changePosition();
        }

        super.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
    }

    @Override
    public void run() {
        if (path != null) {
            actionDone = false;
            for (Vertex vertex : path) {
                if (!actionDone) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    Platform.runLater(() -> {
                        setX(vertex.getX());
                        setY(vertex.getY());
                    });
                }
            }
            actionDone = true;


     }
    }
}

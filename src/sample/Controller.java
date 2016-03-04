package sample;

import classes.enumerations.Sprite;
import classes.graph.Edge;
import classes.list.CircularQueue;
import classes.utils.*;
import classes.enumerations.Image;
import classes.enumerations.Position;
import classes.graph.Graph;
import classes.graph.Vertex;
import classes.utils.Character;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.*;

public class Controller {
    @FXML
    private Slider slider_size;
    @FXML
    private Button button_start, button_start_q0, button_start_q1, button_start_q2;
    @FXML
    private Label label_error;
    @FXML
    private AnchorPane anchorPane;

    private Integer PACE;

    private Timer julietteTimer, timerQ0, timerQ1, timerQ2;
    private AnimationHandler julietteAnimation, romeoAnimation;
    private Graph graph;
    private Character panda, raccoon;
    private Thread romeoThread, julietteThread;
    private CircularQueue<Vertex> path;
    private List<MapElement> obstaclesList = new ArrayList<>();

    public Controller() {
    }

    @FXML
    public void start() {
        clearAll();
        initMap();
        displayButtons(true);
    }

    @FXML
    public void start_q0() {
        displayButtons(false);
        romeoRunTheShortestPathToVertex(graph.getVertexByLocation(raccoon.getX(), raccoon.getY()));
    }

    @FXML
    public void start_q1() {
        displayButtons(false);
        romeoAndJulietteFindEachOther();
    }

    @FXML
    public void start_q2() {
        displayButtons(false);
        romeoLooksForJuliette();
    }

    @FXML
    void button_start_onKeyEvent(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            clearAll();
            button_start.fire();
        } else
            anchorPane.requestFocus();
    }

    public void initMap() {
        slider_size.setFocusTraversable(false);
        button_start.setFocusTraversable(false);
        PACE = (slider_size.getValue() < 50) ? 50 : (int) slider_size.getValue();

        initObstacles();
        initRomeoAndJuliette();
        initGraph();
    }

    public void initObstacles() {
        MapElement obstacle;
        int maxX = (int) (anchorPane.getPrefWidth() / PACE);
        int maxY = (int) (anchorPane.getPrefHeight() / PACE) - 1;
        for (int x = 2; x < maxX; x *= 2) {
            for (int y = 3; y < maxY; y++) {
                if (y % x != 0) {
                    obstacle = new MapElement(x * PACE, y * PACE, PACE, Image.OBSTACLE);
                    anchorPane.getChildren().add(obstacle.getShape());
                    obstaclesList.add(obstacle);
                }
            }
        }
        for (int y = 2; y < maxY; y *= 2) {
            for (int x = 3; x < maxX; x++) {
                if (x % 5 == 0 || x % 5 == 1) {
                    obstacle = new MapElement(x * PACE, y * PACE, PACE, Image.OBSTACLE);
                    anchorPane.getChildren().add(obstacle.getShape());
                    obstaclesList.add(obstacle);
                }
            }
        }
    }

    public void initRomeoAndJuliette() {
        try {
            panda = new Character(0, 0, PACE, Image.PANDA);
            raccoon = new Character(0, 0, PACE, Image.RACCOON);

            initCharacter(panda);
            initCharacter(raccoon);
        } catch (Exception e) {
            label_error.setText("Bravo ! Maintenant c'est cassé. :(");
        }
    }

    public void initCharacter(Character character) {
        Random random = new Random();

        int randX = -1;
        int randY = -1;

        while (randX + character.getShape().getWidth() > anchorPane.getWidth() ||
                randY + character.getShape().getHeight() > anchorPane.getHeight() ||
                !checkIfNoObstacles(randX, randY) ||
                randX < 0 || randY < 0) {
            randX = random.nextInt((int) anchorPane.getWidth());
            randY = random.nextInt((int) anchorPane.getHeight());

            randX -= randX % PACE;
            randY -= randY % PACE;
        }

        character.setX(randX);
        character.setY(randY);

        anchorPane.getChildren().add(character.getShape());
    }

    public void initGraph() {
        graph = new Graph((int) anchorPane.getWidth(), (int) anchorPane.getHeight(), obstaclesList, PACE);   // TODO : vérifier ce qui est généré
    }

    public void clearAll() {
        cancelTimer(timerQ0, timerQ1, timerQ2, julietteTimer);

        if (julietteTimer != null) julietteTimer.cancel();

        label_error.setText("");

        for (MapElement obstacle : obstaclesList)
            anchorPane.getChildren().remove(obstacle.getShape());
        obstaclesList.clear();

        stopMovement();

        if (panda != null) {
            anchorPane.getChildren().remove(panda.getShape());
            panda = null;
        }

        if (raccoon != null) {
            anchorPane.getChildren().remove(raccoon.getShape());
            raccoon = null;
        }
    }

    public void romeoRunTheShortestPathToVertex(Vertex destination){
        stopMovement();
        startTimerQ0();

        Vertex romeoVertex = graph.getVertexByLocation(panda.getX(), panda.getY());

        panda.initPath(graph, romeoVertex, destination);

        romeoThread = new Thread(panda);

        romeoThread.start();

        animateRomeo();
    }

    public void romeoAndJulietteFindEachOther() {
        try {
            stopMovement();
            startTimerQ1();

            Vertex romeoVertex = graph.getVertexByLocation(panda.getX(), panda.getY());
            Vertex julietteVertex = graph.getVertexByLocation(raccoon.getX(), raccoon.getY());

            Vertex destination = getDestinationBetweenVertexes(romeoVertex, julietteVertex);

            panda.initPath(graph, romeoVertex, destination);
            raccoon.initPath(graph, julietteVertex, destination);

            romeoThread = new Thread(panda);
            julietteThread = new Thread(raccoon);

            romeoThread.start();
            julietteThread.start();

            animateJuliette();
            animateRomeo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void romeoLooksForJuliette(){
        try {
            stopMovement();
            getDFSPath(panda);

            startJulietteTimer();
            startTimerQ2();

//            Vertex romeoVertex = graph.getVertexByLocation(panda.getX(), panda.getY());
//            Vertex julietteVertex = graph.getVertexByLocation(raccoon.getX(), raccoon.getY());
//            panda.initPath(graph, romeoVertex, julietteVertex);
//
//            romeoThread = new Thread(panda);
//            romeoThread.start();

            animateJuliette();
            animateRomeo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMovement() {
        if (romeoThread != null)
            romeoThread.interrupt();
        romeoThread = null;

        if (julietteThread != null)
            julietteThread.interrupt();
        julietteThread = null;

        if (romeoAnimation != null){
            romeoAnimation.purge();
            romeoAnimation.cancel();
        }

        if (julietteAnimation != null){
            julietteAnimation.purge();
            julietteAnimation.cancel();
        }
    }

    public void startTimerQ0() {
        cancelTimer(timerQ0);

        timerQ0 = new Timer();
        timerQ0.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (panda.isActionDone())
                            cancelTimer(timerQ0);
                    }
                });
            }
        }, 0, 300);
    }

    public void startTimerQ1() {
        cancelTimer(timerQ1);

        timerQ1 = new Timer();
        timerQ1.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (panda.isActionDone() && raccoon.isActionDone())
                            cancelTimer(timerQ1);
                    }
                });
            }
        }, 0, 300);
    }

    public void startJulietteTimer() {
        cancelTimer(julietteTimer);

        julietteTimer = new Timer();
        julietteTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> walkRandomly(raccoon));
            }
        }, 0, 600);
    }

    public void startTimerQ2() {
        cancelTimer(timerQ2);

        timerQ2 = new Timer();
        timerQ2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (areLocationsClose(panda.getLocation(), raccoon.getLocation())){
//                        cancelTimer(julietteTimer, timerQ2);
//                        romeoRunTheShortestPathToVertex();
                    }

                    if (panda.isActionDone()){
                        Location location = path.popFirstAndRepushAtTheEnd().getLocation();

                        if (((location.getX() - panda.getX() == PACE || location.getX() - panda.getX() == -PACE)
                                && location.getY() == panda.getY()) ||
                                ((location.getY() - panda.getY() == PACE || location.getY() - panda.getY() == -PACE)
                                        && location.getX() == panda.getX())){
                            panda.setLocation(location);
                        }
                        else {

                        }

                        Rectangle r = new Rectangle(PACE, PACE);
                        r.setFill(Color.BURLYWOOD);
                        r.setX(panda.getX());
                        r.setY(panda.getY());
                        r.setOpacity(0.4);
                        anchorPane.getChildren().add(r);
                    }
                });
            }
        }, 0, 300);
    }

    public void cancelTimer(Timer... timers){
        for (Timer timer : timers) {
            if (timer != null) {
                timer.purge();
                timer.cancel();
            }
        }

        stopMovement();
    }

    public void walkRandomly(Character character) {
        int x = character.getX();
        int y = character.getY();

        HashMap<Integer, Location> movementsDictionnary = new HashMap<>();
        movementsDictionnary.put(Position.LEFT.toInteger(), new Location(x - PACE, y));
        movementsDictionnary.put(Position.RIGHT.toInteger(), new Location(x + PACE, y));
        movementsDictionnary.put(Position.UP.toInteger(), new Location(x, y + PACE));
        movementsDictionnary.put(Position.DOWN.toInteger(), new Location(x, y - PACE));

        Random random = new Random();

        boolean hasMoved = false;
        while (!hasMoved) {
            int position = random.nextInt(4);
            Location location = movementsDictionnary.get(position);

            if (checkIfNoObstacles(location.getX(), location.getY())) {
                character.setX(location.getX());
                character.setY(location.getY());
                hasMoved = true;
            }
        }
    }

    public Vertex getDestinationBetweenVertexes(Vertex v1, Vertex v2){
        List<Vertex> path = graph.dijkstra(v1, v2);
        return path.get(path.size() / 2);
    }

    public boolean checkIfNoObstacles(int x, int y) {
        for (MapElement obstacle : obstaclesList)
            if (obstacle.getX() == x && obstacle.getY() == y ||
                    x < 0 || y < 0 || x >= anchorPane.getWidth() || y >= anchorPane.getHeight())
                return false;
        return true;
    }

    public boolean areLocationsClose(Location location1, Location location2) {
        if ((location1.getX() == location2.getX() && location1.getY() == location2.getY()) ||
                (location1.getX() == location2.getX() + PACE && location1.getY() == location2.getY()) ||
                (location1.getX() == location2.getX() - PACE && location1.getY() == location2.getY()) ||
                (location1.getX() == location2.getX() && location1.getY() == location2.getY() + PACE) ||
                (location1.getX() == location2.getX() && location1.getY() == location2.getY() - PACE) ||
                (location1.getX() == location2.getX() + 2*PACE && location1.getY() == location2.getY()) ||
                (location1.getX() == location2.getX() - 2*PACE && location1.getY() == location2.getY()) ||
                (location1.getX() == location2.getX() && location1.getY() == location2.getY() + 2*PACE) ||
                (location1.getX() == location2.getX() && location1.getY() == location2.getY() - 2*PACE)) {
            return true;
        }
        return false;
    }

    public void animateRomeo(){
        if (romeoAnimation != null) {
            romeoAnimation.purge();
            romeoAnimation.cancel();
        }

        romeoAnimation = new AnimationHandler(panda, Sprite.PANDA_SPRITE);
        romeoAnimation.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> romeoAnimation.changeFrame());
            }
        }, 0, 150);
    }

    public void animateJuliette(){
        if (julietteAnimation != null) {
            julietteAnimation.purge();
            julietteAnimation.cancel();
        }

        julietteAnimation = new AnimationHandler(raccoon, Sprite.RACCOON_SPRITE);
        julietteAnimation.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        julietteAnimation.changeFrame();
                    }
                });
            }
        }, 0, 150);
    }

    public void displayButtons(boolean bool){
        button_start_q0.setVisible(bool);
        button_start_q1.setVisible(bool);
        button_start_q2.setVisible(bool);
    }


    public void DFS(Vertex currentVertex, List<Vertex> allVertices){
        System.out.println(currentVertex.getLocation());
        path.push(currentVertex);
        allVertices.remove(currentVertex);

        if (allVertices.isEmpty())
            return;

        for (Edge e : currentVertex.getAdjacencies()) {
            if (allVertices.contains(e.getTarget())) {
                DFS(e.getTarget(), allVertices);
            }
        }
    }

    public void getDFSPath(Character character) {
        System.out.println(character);

        Vertex start = graph.getVertexByLocation(character.getLocation());
        path = new CircularQueue<>(graph.getListVertex().size());
        DFS(start, new ArrayList<>(graph.getListVertex()));
    }
}
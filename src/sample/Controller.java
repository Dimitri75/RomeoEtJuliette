package sample;

import classes.element.Location;
import classes.element.MapElement;
import classes.enumerations.Sprite;
import classes.graph.Edge;
import classes.list.CircularQueue;
import classes.utils.*;
import classes.enumerations.Image;
import classes.enumerations.Position;
import classes.graph.Graph;
import classes.graph.Vertex;
import classes.element.Character;
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

    private Timer julietteTimer, timer, timerBrowser;
    private AnimationHandler julietteAnimation, romeoAnimation;
    private Graph graph;
    private Character panda, raccoon;
    private Thread romeoThread, julietteThread;
    private CircularQueue<Vertex> path;
    private List<Rectangle> markedLocations;
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

    /**
     * Initializes the map
     */
    public void initMap() {
        slider_size.setFocusTraversable(false);
        button_start.setFocusTraversable(false);
        PACE = (slider_size.getValue() < 50) ? 50 : (int) slider_size.getValue();

        initObstacles();
        initRomeoAndJuliette();
        initGraph();
    }

    /**
     * Place obstacles around the map according to the size of it
     */
    public void initObstacles() {
        MapElement obstacle;
        int maxX = (int) (anchorPane.getPrefWidth() / PACE);
        int maxY = (int) (anchorPane.getPrefHeight() / PACE) - 1;
        for (int x = 2; x < maxX; x *= 2) {
            for (int y = 3; y < maxY; y++) {
                if (y % x != 0) {
                    obstacle = new MapElement(x * PACE, y * PACE, PACE, ResourcesUtils.getInstance().getObstacle());
                    anchorPane.getChildren().add(obstacle.getShape());
                    obstaclesList.add(obstacle);
                }
            }
        }
        for (int y = 2; y < maxY; y *= 2) {
            for (int x = 3; x < maxX; x++) {
                if (x % 5 == 0 || x % 5 == 1) {
                    obstacle = new MapElement(x * PACE, y * PACE, PACE, ResourcesUtils.getInstance().getObstacle());
                    anchorPane.getChildren().add(obstacle.getShape());
                    obstaclesList.add(obstacle);
                }
            }
        }
    }

    /**
     * Handles the initialization of both Romeo and Juliette
     */
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

    /**
     * Generic method to init a character at a random position
     * @param character
     */
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

    /**
     * Initializes the graph using obstacles and the size of the window
     */
    public void initGraph() {
        graph = new Graph((int) anchorPane.getWidth(), (int) anchorPane.getHeight(), obstaclesList, PACE);
    }

    /**
     * Clears the map and remove elements
     */
    public void clearAll() {
        clearLocations();
        cancelTimer(timer, timer, timerBrowser, julietteTimer);

        if (julietteTimer != null) julietteTimer.cancel();

        label_error.setText("");

        for (MapElement obstacle : obstaclesList)
            anchorPane.getChildren().remove(obstacle.getShape());
        obstaclesList.clear();

        stopMovements();

        if (panda != null) {
            anchorPane.getChildren().remove(panda.getShape());
            panda = null;
        }

        if (raccoon != null) {
            anchorPane.getChildren().remove(raccoon.getShape());
            raccoon = null;
        }
    }

    /**
     * Starts simulation where Romeo run the shortest path to the given destination
     * @param destination
     */
    public void romeoRunTheShortestPathToVertex(Vertex destination){
        stopRomeo();

        Vertex romeoVertex = graph.getVertexByLocation(panda.getX(), panda.getY());
        panda.initPath(graph, romeoVertex, destination);
        romeoThread = new Thread(panda);

        startGlobalTimer();
        romeoThread.start();

        animateRomeo();
    }

    /**
     * Starts simulation where Romeo and Juliette get to each other through the shortest path in the graph
     */
    public void romeoAndJulietteFindEachOther() {
        try {
            stopMovements();

            Vertex romeoVertex = graph.getVertexByLocation(panda.getX(), panda.getY());
            Vertex julietteVertex = graph.getVertexByLocation(raccoon.getX(), raccoon.getY());

            Vertex destination = getDestinationBetweenVertexes(romeoVertex, julietteVertex);

            panda.initPath(graph, romeoVertex, destination);
            raccoon.initPath(graph, julietteVertex, destination);

            romeoThread = new Thread(panda);
            julietteThread = new Thread(raccoon);

            startGlobalTimer();
            romeoThread.start();
            julietteThread.start();

            animateJuliette();
            animateRomeo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts simulation where Romeo tries to find Juliette without knowing her exact position
     */
    public void romeoLooksForJuliette(){
        try {
            stopMovements();
            initBrowsingPathFrom(raccoon);

            startJulietteTimer();
            startTimerBrowser();

            animateJuliette();
            animateRomeo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancels both Romeo and Juliette
     */
    public void stopMovements(){
        stopRomeo();
        stopJuliette();
    }

    /**
     * Handles cancelation of Romeo's animations and thread
     */
    public void stopRomeo() {
        if (romeoThread != null)
            romeoThread.interrupt();
        romeoThread = null;

        cancelTimer(romeoAnimation);
    }

    /**
     * Handles cancelation of Juliette's animations and thread
     */
    public void stopJuliette() {
        cancelTimer(julietteTimer, julietteAnimation);

        if (julietteThread != null)
            julietteThread.interrupt();
        julietteThread = null;
    }

    /**
     * Global timer which stops animation when Romeo and Juliette are done
     */
    public void startGlobalTimer() {
        cancelTimer(timer);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (panda.isActionDone() && raccoon.isActionDone())
                        cancelTimer(romeoAnimation, julietteAnimation, timer);
                });
            }
        }, 0, 300);
    }

    /**
     * Timer which handles the random walk of Juliette
     */
    public void startJulietteTimer() {
        cancelTimer(julietteTimer);

        julietteTimer = new Timer();
        julietteTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    animateJuliette();
                    walkRandomly(raccoon);
                });
            }
        }, 0, 300);
    }

    /**
     * Global timer which handles the movements of Romeo trying to find Juliette
     */
    public void startTimerBrowser() {
        cancelTimer(timerBrowser);

        timerBrowser = new Timer();
        timerBrowser.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (areLocationsClose(panda.getLocation(), raccoon.getLocation())){
                        stopJuliette();
                        romeoRunTheShortestPathToVertex(graph.getVertexByLocation(raccoon.getLocation()));
                        cancelTimer(timerBrowser);
                    }

                    if (panda.isActionDone()){
                        Location location = path.popFirstAndRepushAtTheEnd().getLocation();

                        if (((location.getX() - panda.getX() == PACE || location.getX() - panda.getX() == -PACE)
                                && location.getY() == panda.getY()) ||
                                ((location.getY() - panda.getY() == PACE || location.getY() - panda.getY() == -PACE)
                                        && location.getX() == panda.getX())){
                            animateRomeo();
                            panda.setLocation(location);
                        }
                        else {
                            romeoRunTheShortestPathToVertex(graph.getVertexByLocation(location));
                        }
                    }
                });
            }
        }, 0, 300);
    }

    /**
     * Handle timers cancelations
     * @param timers
     */
    public void cancelTimer(Timer... timers){
        for (Timer timer : timers) {
            if (timer != null) {
                timer.purge();
                timer.cancel();
            }
        }
    }

    /**
     * Randomly moves a character up, down, left or right
     * @param character
     */
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

    /**
     * Returns the vertex between two other vertices
     * @param v1
     * @param v2
     * @return
     */
    public Vertex getDestinationBetweenVertexes(Vertex v1, Vertex v2){
        List<Vertex> path = graph.dijkstra(v1, v2);
        return path.get(path.size() / 2);
    }

    /**
     * Returns true if an obstacle is found at the given position
     * @param x
     * @param y
     * @return
     */
    public boolean checkIfNoObstacles(int x, int y) {
        for (MapElement obstacle : obstaclesList)
            if (obstacle.getX() == x && obstacle.getY() == y ||
                    x < 0 || y < 0 || x >= anchorPane.getWidth() || y >= anchorPane.getHeight())
                return false;
        return true;
    }

    /**
     * Returns true if two locations have been found close
     * @param location1
     * @param location2
     * @return
     */
    public boolean areLocationsClose(Location location1, Location location2) {
        if ((location1.getX() == location2.getX() && location1.getY() == location2.getY()) ||
                (location1.getX() == location2.getX() + PACE && location1.getY() == location2.getY()) ||
                (location1.getX() == location2.getX() - PACE && location1.getY() == location2.getY()) ||
                (location1.getX() == location2.getX() && location1.getY() == location2.getY() + PACE) ||
                (location1.getX() == location2.getX() && location1.getY() == location2.getY() - PACE) /*||
                (location1.getX() == location2.getX() + 2*PACE && location1.getY() == location2.getY()) ||
                (location1.getX() == location2.getX() - 2*PACE && location1.getY() == location2.getY()) ||
                (location1.getX() == location2.getX() && location1.getY() == location2.getY() + 2*PACE) ||
                (location1.getX() == location2.getX() && location1.getY() == location2.getY() - 2*PACE)*/) {
            return true;
        }
        return false;
    }

    /**
     * COntrols romeo's animation
     */
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

    /**
     * Controls juliette's animation
     */
    public void animateJuliette(){
        if (julietteAnimation != null) {
            julietteAnimation.purge();
            julietteAnimation.cancel();
        }

        julietteAnimation = new AnimationHandler(raccoon, Sprite.RACCOON_SPRITE);
        julietteAnimation.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> julietteAnimation.changeFrame());
            }
        }, 0, 150);
    }

    /**
     * Controls the buttons visibility
     * @param bool
     */
    public void displayButtons(boolean bool){
        button_start_q0.setVisible(bool);
        button_start_q1.setVisible(bool);
        button_start_q2.setVisible(bool);
    }

    /**
     * Initializes a path to browse the graph
     * @param character
     */
    public void initBrowsingPathFrom(Character character) {
        Vertex start = graph.getVertexByLocation(character.getLocation());
        //path = graph.browseBFS(start);
        path = graph.browseDFS(start);
    }



    /**
     * Places a marker at the given position to display a character's walk or anything which needs attention
     * @param location
     * @param color
     */
    public void markLocation(Location location, Color color){
        if (markedLocations == null)
            markedLocations = new ArrayList<>();

        Rectangle rectangle = new Rectangle(PACE, PACE);
        rectangle.setX(location.getX());
        rectangle.setY(location.getY());
        rectangle.setFill(color);
        rectangle.setOpacity(0.5);
        anchorPane.getChildren().add(rectangle);
        markedLocations.add(rectangle);
    }

    /**
     * Clears previously marked locations
     */
    public void clearLocations(){
        if (markedLocations != null && !markedLocations.isEmpty()){
            for (Rectangle rectangle : markedLocations){
                anchorPane.getChildren().remove(rectangle);
            }
            markedLocations.clear();
        }
    }
}
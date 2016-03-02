package sample;

import classes.Character;
import classes.MapElement;
import classes.enumerations.Image;
import classes.enumerations.Position;
import classes.graph.Graph;
import classes.graph.Vertex;
import classes.utils.Location;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.util.*;

public class Controller {
    @FXML
    private Slider slider_size;
    @FXML
    private Button button_start;
    @FXML
    private Label label_error;
    @FXML
    private AnchorPane anchorPane;

    private Integer PACE;

    private Timer timer, timer3;
    private Boolean started = false;
    private Graph graph;
    private Character romeo, juliette;
    private Thread romeoThread, julietteThread;
    private List<MapElement> obstaclesList = new ArrayList<>();

    public Controller() {

    }

    public boolean areVertexesTooClose(Vertex vertex1, Vertex vertex2) {
        if ((vertex1.getX() == vertex2.getX() && vertex1.getY() == vertex2.getY()) ||
                (vertex1.getX() == vertex2.getX() + PACE && vertex1.getY() == vertex2.getY()) ||
                (vertex1.getX() == vertex2.getX() - PACE && vertex1.getY() == vertex2.getY()) ||
                (vertex1.getX() == vertex2.getX() && vertex1.getY() == vertex2.getY() + PACE) ||
                (vertex1.getX() == vertex2.getX() && vertex1.getY() == vertex2.getY() - PACE) /*||
                (vertex1.getX() == vertex2.getX() + 2 * PACE && vertex1.getY() == vertex2.getY()) ||
                (vertex1.getX() == vertex2.getX() - 2 * PACE && vertex1.getY() == vertex2.getY()) ||
                (vertex1.getX() == vertex2.getX() && vertex1.getY() == vertex2.getY() + 2 * PACE) ||
                (vertex1.getX() == vertex2.getX() && vertex1.getY() == vertex2.getY() - 2 * PACE)*/) {
            return true;
        }
        return false;
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

    public void initRomeoAndJuliette() {
        try {
            romeo = new Character(0, 0, PACE);
            juliette = new Character(0, 0, PACE);

            initCharacter(romeo);
            initCharacter(juliette);
        } catch (Exception e) {
            label_error.setText("Bravo ! Maintenant c'est cassé. :(");
        }
    }

    public void initGraph() {
        graph = new Graph((int) anchorPane.getWidth(), (int) anchorPane.getHeight(), obstaclesList, PACE);   // TODO : vérifier ce qui est généré
    }

    @FXML
    public void start() {
        clearAll();
        slider_size.setFocusTraversable(false);
        button_start.setFocusTraversable(false);
        PACE = (slider_size.getValue() < 10) ? 10 : (int) slider_size.getValue();
        initObstacles();
        initRomeoAndJuliette();
        initGraph();
        started = true;
    }

    @FXML
    public void start_q1() {
        romeoAndJulietteFindEachOther();
    }

    @FXML
    public void start_q2() {
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

    public void clearAll() {
        if (timer != null)timer.cancel();

        label_error.setText("");

        for (MapElement obstacle : obstaclesList)
            anchorPane.getChildren().remove(obstacle.getShape());
        obstaclesList.clear();

        stopMovement();

        if (romeo != null) {
            anchorPane.getChildren().remove(romeo.getShape());
            romeo = null;
        }

        if (juliette != null) {
            anchorPane.getChildren().remove(juliette.getShape());
            juliette = null;
        }
    }

    public void romeoLooksForJuliette(){
        try {
            stopMovement();
            startJulietteTimer();
            startTimerQ2();

            Vertex romeoVertex = graph.getVertexByLocation(romeo.getX(), romeo.getY());
            Vertex julietteVertex = graph.getVertexByLocation(juliette.getX(), juliette.getY());

            romeo.initPath(graph, romeoVertex, julietteVertex);

            romeoThread = new Thread(romeo);

            romeoThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void romeoAndJulietteFindEachOther() {
        try {
            stopMovement();
            startTimerQ1();

            Vertex romeoVertex = graph.getVertexByLocation(romeo.getX(), romeo.getY());
            Vertex julietteVertex = graph.getVertexByLocation(juliette.getX(), juliette.getY());
            Vertex destination = getDestination();

            romeo.initPath(graph, romeoVertex, destination);
            juliette.initPath(graph, julietteVertex, destination);


            romeoThread = new Thread(romeo);
            julietteThread = new Thread(juliette);

            romeoThread.start();
            julietteThread.start();
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
    }

    public boolean checkIfNoObstacles(int x, int y) {
        for (MapElement obstacle : obstaclesList)
            if (obstacle.getX() == x && obstacle.getY() == y ||
                    x < 0 || y < 0 || x >= anchorPane.getWidth() || y >= anchorPane.getHeight())
                return false;
        return true;
    }

    public void startTimerQ1() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (romeo.isActionDone() && juliette.isActionDone())
                            timer.cancel();
                        else if (romeo.isActionDone() || juliette.isActionDone())
                            romeoAndJulietteFindEachOther();
                    }
                });
            }
        }, 0, 250);
    }

    public void startJulietteTimer() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        walkRandomly(juliette);
                    }
                });
            }
        }, 0, 200);
    }

    public void startTimerQ2() {
        if (timer3 != null) {
            timer3.purge();
            timer3.cancel();
        }

        timer3 = new Timer();
        timer3.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Vertex romeoVertex = graph.getVertexByLocation(romeo.getX(), romeo.getY());
                        Vertex julietteVertex = graph.getVertexByLocation(juliette.getX(), juliette.getY());

                        if (areVertexesTooClose(romeoVertex, julietteVertex)){
                            romeoThread.interrupt();
                            timer3.cancel();
                            timer.cancel();
                        }

                        if (romeo.isActionDone())
                            walkRandomly(romeo);
                    }
                });
            }
        }, 0, 100);
    }

    public Vertex getDestination(){
        int x, y;
        if (romeo.getX() > juliette.getX())
            x = juliette.getX() + ((romeo.getX() - juliette.getX()) / 2);
        else
            x = romeo.getX() + ((juliette.getX() - romeo.getX()) / 2);

        if (romeo.getY() > juliette.getY())
            y = juliette.getY() + ((romeo.getY() - juliette.getY()) / 2);
        else
            y = romeo.getY() + ((juliette.getY() - romeo.getY()) / 2);

        x = x - (x % PACE);
        y = y - (y % PACE);

        while (!checkIfNoObstacles(x, y))
            x -= PACE;

        return graph.getVertexByLocation(x, y);
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

    public void romeoLooksForSomething(Vertex destination){
        if (romeoThread != null)
            romeoThread.interrupt();

        if (checkIfNoObstacles(destination.getX(), destination.getY())) {
            Vertex romeoVertex = graph.getVertexByLocation(romeo.getX(), romeo.getY());
            romeo.initPath(graph, romeoVertex, destination);

            romeoThread = new Thread(romeo);

            romeoThread.start();
        }
    }
}
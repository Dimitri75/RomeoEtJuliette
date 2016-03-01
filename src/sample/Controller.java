package sample;

import classes.Character;
import classes.MapElement;
import classes.enumerations.Image;
import classes.graph.Graph;
import classes.graph.Vertex;
import javafx.animation.AnimationTimer;
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

    private Timer timer;
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
                (vertex1.getX() == vertex2.getX() && vertex1.getY() == vertex2.getY() - PACE) ||
                (vertex1.getX() == vertex2.getX() + 2 * PACE && vertex1.getY() == vertex2.getY()) ||
                (vertex1.getX() == vertex2.getX() - 2 * PACE && vertex1.getY() == vertex2.getY()) ||
                (vertex1.getX() == vertex2.getX() && vertex1.getY() == vertex2.getY() + 2 * PACE) ||
                (vertex1.getX() == vertex2.getX() && vertex1.getY() == vertex2.getY() - 2 * PACE)) {
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
        startChasing();
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

    public void startChasing() {
        try {
            stopMovement();
            startTimer();

            Vertex romeoVertex = graph.getVertexByLocation(romeo.getX(), romeo.getY());
            Vertex julietteVertex = graph.getVertexByLocation(juliette.getX(), juliette.getY());
            Vertex destination = graph.getRandomVertex();

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
        if (romeoThread != null && romeoThread.isAlive())
            romeoThread.interrupt();
        romeoThread = null;

        if (julietteThread != null && julietteThread.isAlive())
            julietteThread.interrupt();
        julietteThread = null;
    }

    public boolean checkIfNoObstacles(int x, int y) {
        for (MapElement obstacle : obstaclesList)
            if (obstacle.getX() == x && obstacle.getY() == y)
                return false;
        return true;
    }

    public void startTimer() {
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
                        if (romeo.isActionDone() && juliette.isActionDone()) {
                            stopMovement();

                            romeo.setActionDone(true);
                            juliette.setActionDone(true);
                            timer.cancel();
                        }
                    }
                });
            }
        }, 0, 250);
    }
}
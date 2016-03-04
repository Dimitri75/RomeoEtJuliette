package classes.utils;

import classes.enumerations.Sprite;
import classes.list.CircularQueue;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Dimitri on 01/11/2015.
 */
public class ResourcesUtils {
    public static ResourcesUtils INSTANCE = null;
    private HashMap<Integer, Image> obstaclesDictionnary;

    public static ResourcesUtils getInstance(){
        if (INSTANCE == null)
            INSTANCE = new ResourcesUtils();

        return INSTANCE;
    }

    private ResourcesUtils(){
        obstaclesDictionnary = new HashMap<>();

        obstaclesDictionnary.put(0, new Image(classes.enumerations.Image.OBSTACLE1.toString()));
        obstaclesDictionnary.put(1, new Image(classes.enumerations.Image.OBSTACLE2.toString()));
    }

    /**
     * Returns a random obstacle element
     * @return
     */
    public Image getObstacle(){
        Random random = new Random();
        return obstaclesDictionnary.get(random.nextInt(2) % 2);
    }

    /**
     * Gets all the frames from the given Sprite enumeration
     * @param sprite
     * @return
     */
    public AbstractMap.SimpleEntry<CircularQueue<ImagePattern>, CircularQueue<ImagePattern>> getFrames(Sprite sprite){
        File spriteDirectory = new File("src", sprite.toString());
        File leftFrames = new File(spriteDirectory, "LEFT");
        File rightFrames = new File(spriteDirectory, "RIGHT");

        if (!spriteDirectory.exists() || !leftFrames.exists() || !rightFrames.exists())
            return null;

        return new AbstractMap.SimpleEntry(getFilledQueue(leftFrames), getFilledQueue(rightFrames));
    }

    /**
     * Returns a CircularQueue containing all the images in the given folder
     * @param directory
     * @return
     */
    public CircularQueue<ImagePattern> getFilledQueue(File directory){
        if (!directory.exists()) return null;

        CircularQueue<ImagePattern> circularQueue = new CircularQueue<>(directory.listFiles().length);
        String path;
        for (File file : directory.listFiles()){
            path = file.getPath().replace("src\\", "");
            circularQueue.addAndReturn(new ImagePattern(new javafx.scene.image.Image(path)));
        }
        return circularQueue;
    }
}

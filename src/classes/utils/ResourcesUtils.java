package classes.utils;

import classes.enumerations.Sprite;
import classes.list.CircularQueue;
import javafx.scene.paint.ImagePattern;

import java.io.File;
import java.util.AbstractMap;

/**
 * Created by Dimitri on 01/11/2015.
 */
public class ResourcesUtils {
    public static ResourcesUtils INSTANCE = null;

    public static ResourcesUtils getInstance(){
        if (INSTANCE == null)
            INSTANCE = new ResourcesUtils();

        return INSTANCE;
    }

    private ResourcesUtils(){
    }

    public AbstractMap.SimpleEntry<CircularQueue<ImagePattern>, CircularQueue<ImagePattern>> getFrames(Sprite sprite){
        File spriteDirectory = new File("src", sprite.toString());
        File leftFrames = new File(spriteDirectory, "LEFT");
        File rightFrames = new File(spriteDirectory, "RIGHT");

        if (!spriteDirectory.exists() || !leftFrames.exists() || !rightFrames.exists())
            return null;

        return new AbstractMap.SimpleEntry(getFilledQueue(leftFrames), getFilledQueue(rightFrames));
    }


    public CircularQueue<ImagePattern> getFilledQueue(File directory){
        if (!directory.exists()) return null;

        CircularQueue<ImagePattern> circularQueue = new CircularQueue<>(directory.listFiles().length);
        String path;
        for (File file : directory.listFiles()){
            path = file.getPath().replace("src\\", "");
            circularQueue.push(new ImagePattern(new javafx.scene.image.Image(path)));
        }
        return circularQueue;
    }
}

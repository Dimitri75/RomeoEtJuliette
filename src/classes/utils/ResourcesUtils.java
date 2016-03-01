package classes.utils;

import classes.enumerations.Image;
import classes.enumerations.Position;
import javafx.scene.paint.ImagePattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Dimitri on 01/11/2015.
 */
public class ResourcesUtils {
    public static ResourcesUtils INSTANCE = null;

    private Map<Integer, Image> left_charactersDictionnary;
    private Map<Integer, Image> right_charactersDictionnary;

    public static ResourcesUtils getInstance(){
        if (INSTANCE == null)
            INSTANCE = new ResourcesUtils();

        return INSTANCE;
    }

    private ResourcesUtils(){
        left_charactersDictionnary = new HashMap<>();
        left_charactersDictionnary.put(0, Image.LEFT_ROMEO);
        left_charactersDictionnary.put(1, Image.LEFT_JULIETTE);

        right_charactersDictionnary = new HashMap<>();
        right_charactersDictionnary.put(0, Image.RIGHT_ROMEO);
        right_charactersDictionnary.put(1, Image.RIGHT_JULIETTE);
    }

    public Integer getRandomCharacterIndex(){
        Random ran = new Random();
        return ran.nextInt(left_charactersDictionnary.size());
    }

    public ImagePattern getCharacter(Integer index, Position position){
        javafx.scene.image.Image image;

        if (position.equals(Position.LEFT))
            image = new javafx.scene.image.Image(left_charactersDictionnary.get(index).toString());
        else
            image = new javafx.scene.image.Image(right_charactersDictionnary.get(index).toString());

        return new ImagePattern(image);
    }
}

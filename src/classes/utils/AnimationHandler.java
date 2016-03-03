package classes.utils;

import classes.enumerations.Position;
import classes.enumerations.Sprite;
import classes.list.CircularQueue;
import javafx.scene.paint.ImagePattern;

import java.util.Timer;

/**
 * Created by Dimitri on 03/03/2016.
 */
public class AnimationHandler extends Timer {
    Character character;
    CircularQueue<ImagePattern> left_circularQueue, right_circularQueue;

    public AnimationHandler(Character character, Sprite sprite){
        this.character = character;
        left_circularQueue = ResourcesUtils.getInstance().getFrames(sprite).getKey();
        right_circularQueue = ResourcesUtils.getInstance().getFrames(sprite).getValue();
    }

    public void changeFrame(){
        if (character.getPosition().equals(Position.LEFT) && !left_circularQueue.isEmpty())
            character.getShape().setFill(left_circularQueue.popFirstAndRepushAtTheEnd());
        else
            character.getShape().setFill(right_circularQueue.popFirstAndRepushAtTheEnd());
    }
}

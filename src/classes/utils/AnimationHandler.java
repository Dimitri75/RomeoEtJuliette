package classes.utils;

import classes.element.Character;
import classes.enumerations.EnumPosition;
import classes.enumerations.EnumSprite;
import classes.list.CircularQueue;
import javafx.scene.paint.ImagePattern;

import java.util.Timer;

/**
 * Created by Dimitri on 03/03/2016.
 */
public class AnimationHandler extends Timer {
    Character character;
    CircularQueue<ImagePattern> left_circularQueue, right_circularQueue;

    public AnimationHandler(Character character, EnumSprite sprite){
        this.character = character;
        left_circularQueue = ResourcesUtils.getInstance().getFrames(sprite).getKey();
        right_circularQueue = ResourcesUtils.getInstance().getFrames(sprite).getValue();
    }

    /**
     * Changes frame according to the character's position to create animation
     */
    public void changeFrame(){
        if (character.getEnumPosition().equals(EnumPosition.LEFT) && !left_circularQueue.isEmpty())
            character.getShape().setFill(left_circularQueue.popFirstAndRepushAtTheEnd());
        else
            character.getShape().setFill(right_circularQueue.popFirstAndRepushAtTheEnd());
    }
}

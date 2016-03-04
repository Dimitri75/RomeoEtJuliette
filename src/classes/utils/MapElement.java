package classes.utils;

import classes.enumerations.Image;
import classes.interfaces.ILocation;
import javafx.animation.TranslateTransition;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Created by Dimitri on 05/11/2015.
 */
public class MapElement implements ILocation {
    int x, y;
    private Rectangle shape;

    public MapElement(int x, int y, int shapeSize) {
        shape = new Rectangle(shapeSize, shapeSize);
        this.x = x;
        this.y = y;
        shape.setX(x);
        shape.setY(y);
    }

    public MapElement(int x, int y, int shapeSize, Image image) {
        shape = new Rectangle(shapeSize, shapeSize);
        this.x = x;
        this.y = y;
        shape.setX(x);
        shape.setY(y);
        getShape().setFill(new ImagePattern(new javafx.scene.image.Image(image.toString())));
    }

    public MapElement(int x, int y, int shapeSize, javafx.scene.image.Image image) {
        shape = new Rectangle(shapeSize, shapeSize);
        this.x = x;
        this.y = y;
        shape.setX(x);
        shape.setY(y);
        getShape().setFill(new ImagePattern(image));
    }

    public Rectangle getShape() {
        return shape;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;

        TranslateTransition transition = new TranslateTransition();
        transition.setToX(x);

        transition.setDuration(Duration.millis(300));
        transition.setNode(getShape());
        transition.play();
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;

        TranslateTransition transition = new TranslateTransition();
        transition.setToY(y);

        transition.setDuration(Duration.millis(300));
        transition.setNode(getShape());
        transition.play();
    }

    @Override
    public boolean equals(Object other){
        if (other == null)
            return false;

        if (getX() == ((MapElement) other).getX() && getY() == ((MapElement) other).getY())
            return true;
        else
            return false;
    }
}

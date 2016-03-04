package classes.element;

import classes.enumerations.Image;
import classes.interfaces.ILocation;
import javafx.animation.TranslateTransition;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Created by Dimitri on 05/11/2015.
 */
public class MapElement extends Location {
    private Rectangle shape;

    public MapElement(int x, int y, int shapeSize) {
        super(x, y);
        shape = new Rectangle(shapeSize, shapeSize);
        shape.setX(x);
        shape.setY(y);
    }

    public MapElement(int x, int y, int shapeSize, Image image) {
        super(x, y);
        shape = new Rectangle(shapeSize, shapeSize);
        shape.setX(x);
        shape.setY(y);
        getShape().setFill(new ImagePattern(new javafx.scene.image.Image(image.toString())));
    }

    public MapElement(int x, int y, int shapeSize, javafx.scene.image.Image image) {
        super(x, y);
        shape = new Rectangle(shapeSize, shapeSize);
        shape.setX(x);
        shape.setY(y);
        getShape().setFill(new ImagePattern(image));
    }

    public Rectangle getShape() {
        return shape;
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
        if (other != null && getX() == ((MapElement) other).getX() && getY() == ((MapElement) other).getY())
            return true;
        return false;
    }
}

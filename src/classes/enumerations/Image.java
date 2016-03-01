package classes.enumerations;

/**
 * Created by Dimitri on 01/11/2015.
 */
public enum Image {
    RIGHT_ROMEO("res/images/right_romeo.png"),
    RIGHT_JULIETTE("res/images/right_juliette.png"),

    LEFT_ROMEO("res/images/left_romeo.png"),
    LEFT_JULIETTE("res/images/left_juliette.png"),

    OBSTACLE("res/images/obstacle.png");

    private String name = "";

    Image (String name){
        this.name = name;
    }

    public String toString(){
        return name;
    }
}

package classes.enumerations;

/**
 * Created by Dimitri on 01/11/2015.
 */
public enum Image {
    PANDA("res/images/panda.png"),
    RACCOON("res/images/raccoon.png"),

    OBSTACLE("res/images/obstacle.png"),
    OBSTACLE1("res/images/obstacle1.png"),
    OBSTACLE2("res/images/obstacle2.png");

    private String name = "";

    Image (String name){
        this.name = name;
    }

    public String toString(){
        return name;
    }
}

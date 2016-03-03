package classes.enumerations;

/**
 * Created by Dimitri on 03/03/2016.
 */
public enum Sprite {
    RACCOON_SPRITE("res/images/sprite_raccoon"),
    PANDA_SPRITE("res/images/sprite_panda");


    private String name = "";

    Sprite(String name){
        this.name = name;
    }

    public String toString(){
        return name;
    }
}

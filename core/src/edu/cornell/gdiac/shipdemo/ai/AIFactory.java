package edu.cornell.gdiac.shipdemo.ai;

import edu.cornell.gdiac.shipdemo.Fruit;
import edu.cornell.gdiac.shipdemo.GameMode;

public class AIFactory {
    public static AIController makeAI(Fruit myFruit, GameMode game){

        switch (myFruit.getType()){
            case ORANGE:
                return new OrangeController(myFruit, game);
            case APPLE:
                return new AppleController(myFruit, game);
            case STRAWBERRY:
                return new StrawberryController(myFruit, game);
        }
        return null;
    }
}

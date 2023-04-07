package edu.cornell.gdiac.shipdemo.ai;

import edu.cornell.gdiac.shipdemo.Fruit;
import edu.cornell.gdiac.shipdemo.GameMode;
import edu.cornell.gdiac.shipdemo.ai.AIController;

public class AppleController extends AIController {
    public AppleController(Fruit myFruit, GameMode game){
        super(myFruit, game);
    }

    @Override
    public void update(int frameCount) {
        if (myFruit.isStunned){
            myFruit.ang += 10;
            myFruit.ang %= 360;
            myFruit.stunTimer -= frameCount;
            myFruit.stunTimer = Math.max(myFruit.stunTimer, 0);
            myFruit.isStunned = (myFruit.stunTimer != 0);
            return;
        }

        runFromDest(gameMode.playerMonkey.getPosition(), 500);
    }
}

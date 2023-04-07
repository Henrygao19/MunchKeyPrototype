package edu.cornell.gdiac.shipdemo.ai;

import edu.cornell.gdiac.shipdemo.Fruit;
import edu.cornell.gdiac.shipdemo.GameMode;

public class StrawberryController extends AIController {
    enum Action {
        SHOOT,
        RUN,
        STAY,
        EDGE,
    }

    private Action act = Action.STAY;


    private int phaseFrames = 0;

    public StrawberryController(Fruit myFruit, GameMode game){
        super(myFruit, game);
        this.dest = gameMode.playerMonkey.getPosition().cpy();
        this.MAX_SPEED = 0.8f;
        this.ACCEL = 0.025f;
    }

    private void switchState(Action a){
        act = a;
        phaseFrames = 0;
    }

    private void updateFSM(){
        if(atBounds()){
            switchState(Action.EDGE);
            return;
        }

        if(getDist(gameMode.playerMonkey.getPosition()) > 600) {
            switchState(Action.STAY);
            return;
        }

        phaseFrames++;
        switch (act){
            case STAY:
                if(getDist(gameMode.playerMonkey.getPosition()) < 500) {
                    switchState(Action.RUN);
                }
                break;
            case RUN:
                if(phaseFrames > 100){
                    switchState(Action.SHOOT);
                }
                break;
            case SHOOT:
                if(phaseFrames > 200){
                    switchState(Action.RUN);
                }
                break;
            case EDGE:
                if(phaseFrames > 50){
                    switchState(Action.SHOOT);
                }
                break;
        }
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

        updateFSM();

        switch (act){
            case STAY:
                break;
            case RUN:
                setFire(false);
                speed = Math.min(speed + 0.05f, MAX_SPEED);
                dest = gameMode.playerMonkey.getPosition().cpy();
                runFromDest(dest, 500);
                break;
            case SHOOT:
                dest = gameMode.playerMonkey.getPosition().cpy();
                if(speed <= 0.01) {
                    dest = dest.sub(myFruit.getPosition());
                    normalize(dest);
                    dest.add(myFruit.getPosition());
                    setFire(true);
                    goToDest(dest);
                }else{
                    runFromDest(dest, 0);
                }
                break;
            case EDGE:
                speed = Math.min(speed + 0.005f, MAX_SPEED);
                boundsDest();
                setFire(false);
                runFromDest(dest, 100000);

        }
    }
}

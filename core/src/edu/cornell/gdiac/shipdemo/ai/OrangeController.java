//
//
//package edu.cornell.gdiac.shipdemo.ai;
//import edu.cornell.gdiac.shipdemo.Fruit;
//import edu.cornell.gdiac.shipdemo.GameMode;
//public class OrangeController extends AIController {
//    public OrangeController(Fruit myFruit, GameMode game){
//        super(myFruit, game);
//    }
//
//    @Override
//    public void update(int frameCount) {
//        if (myFruit.isStunned){
//            myFruit.ang += 10;
//            myFruit.ang %= 360;
//            myFruit.stunTimer -= frameCount;
//            myFruit.stunTimer = Math.max(myFruit.stunTimer, 0);
//            myFruit.isStunned = (myFruit.stunTimer != 0);
//            return;
//        }
//        if(getDist(dest) < 5){
//            dest.sub(mag);
//            mag.scl(-1);
//        }else{
//            goToDest(dest);
//        }
//    }
//}
package edu.cornell.gdiac.shipdemo.ai;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.shipdemo.Fruit;
import edu.cornell.gdiac.shipdemo.GameMode;

public class OrangeController extends AIController {



    private enum FSMState {
        /** The ship just spawned */
        SPAWN,
        WANDER,
        /** The ship has a target, but must get closer */
        CHASE,
        /** The ship has a target and is attacking it */
        ATTACK
    }
    /** The ship's current state in the FSM */
    private FSMState state;
    private Vector2 distance;
    private int wait;
    private int cooldown_wait;
    private boolean waiting=false;
    private boolean cooldown;
    private int time;

    public OrangeController(Fruit myFruit, GameMode game){
        super(myFruit, game);
        this.state=FSMState.SPAWN;
        this.speed=1.5f;
        this.cooldown=false;
        this.wait=0;
        this.cooldown_wait=0;
        this.time=0;
        this.ACCEL = 0.25f;
        this.MAX_SPEED = 1.2f;
    }


    public void setWaitTime(int seconds){
        waiting = true;
        wait=seconds*60;
    }

    public void changeState() {
        switch (state) {
            case SPAWN:
                state=FSMState.WANDER;
                break;
            case CHASE:
                state = FSMState.ATTACK;
                break;
            case WANDER:
                state = FSMState.CHASE;
                break;
            case ATTACK:
                state = FSMState.SPAWN;
                break;
        }
    }
        public void update (int frameCount){

            if (myFruit.isStunned){
                myFruit.ang += 10;
                myFruit.ang %= 360;
                myFruit.stunTimer -= frameCount;
                myFruit.stunTimer = Math.max(myFruit.stunTimer, 0);
                myFruit.isStunned = (myFruit.stunTimer != 0);
                return;
            }

            // distance to monkey
            distance = myFruit.getPosition().cpy().sub(this.dest);
            float d = distance.len();
            if(cooldown_wait>300){
                cooldown=false;
            }
            float m=time/60;
//            System.out.println("_____________________");
//            System.out.println(state + " "+ d);
            switch (state) {
                case SPAWN:
                    myFruit.setSpiked(false);
                    this.dest = gameMode.playerMonkey.getPosition().cpy();
                    speed = 0f;
                    wait=0;
                    if (d>100||cooldown){
                        state= FSMState.WANDER;
                        myFruit.setSpiked(false);
                    }
                    else{
                        myFruit.setSpiked(true);
                        state = FSMState.CHASE;
                        time=0;
                    }
                    break;
                case WANDER:
                    MAX_SPEED = 0.5f;
                    this.dest = gameMode.playerMonkey.getPosition().cpy();
                    myFruit.setSpiked(false);
                    runFromDest(gameMode.playerMonkey.getPosition().cpy(),500);
                     if(d<=500 &&!cooldown){
                      wait=0;
                      state=FSMState.CHASE;
                      time=0;
                      myFruit.setSpiked(true);
                     }
                  break;
                case CHASE:
                    MAX_SPEED = 1.2f;
                    cooldown_wait=0;
                    if(!myFruit.isSpiked||d<50){
                        cooldown=true;
                        state=FSMState.ATTACK;
                        myFruit.setSpiked(false);
                        wait=0;
                        break;
                    }
                    goToDest(this.dest);
                    break;

                case ATTACK:
                    if(wait>=30){
                        state=FSMState.SPAWN;
                        myFruit.setSpiked(false);
                    }
                    break;

            }
            cooldown_wait++;
            wait++;
            time++;
//            System.out.println(state + " "+ d);

        }

}

package edu.cornell.gdiac.shipdemo.ai;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.shipdemo.Fruit;
import edu.cornell.gdiac.shipdemo.FruitType;
import edu.cornell.gdiac.shipdemo.GameMode;

import java.util.Random;

public abstract class AIController {
    /** The ship this controller is associated with*/
    Fruit myFruit;

    /** Reference to GameMode*/
    GameMode gameMode;

    /** Whether the ship wishes to fire*/
    boolean pressingFire;

    Random rand = new Random();
    float speed;

    protected float ACCEL = 0.025f;

    Vector2 dest;
    Vector2 mag;

    protected float MAX_SPEED;

    public AIController(Fruit myFruit, GameMode gameMode){
        this.myFruit = myFruit;
        this.gameMode = gameMode;
        this.speed = 0;
        this.MAX_SPEED = 0.2f + rand.nextFloat() * 0.55f;

        int trackdist = 1000 + rand.nextInt(100);

        switch (rand.nextInt(2)) {
            case 0:
                this.mag = new Vector2(trackdist, 0);
                break;
            case 1:
                this.mag = new Vector2(0, trackdist);
                break;
        }
        this.dest = myFruit.pos.cpy().add(mag);
        this.dest.x = Math.min(this.dest.x, gameMode.BOUND_X);
        this.dest.y = Math.min(this.dest.y, gameMode.BOUND_Y);
    }

    /**
     * Normalizes a vector v to the unit vector
     * @param v the unit vector
     */
    protected void normalize(Vector2 v) {

        // sets length to 1
        //
        double length = Math.sqrt(v.x*v.x + v.y*v.y);

        if (length != 0.0) {
            float s = 1.0f / (float)length;
            v.x *= s;
            v.y *= s;
        }
    }

    protected void boundsDest(){
        if (myFruit.getPosition().x <= -gameMode.BOUND_X + 20f) {
            dest.x = -gameMode.BOUND_X - 100;
        } else if (myFruit.getPosition().x >= gameMode.BOUND_X - 20f) {
            dest.x = gameMode.BOUND_X + 100;
        }

        if (myFruit.getPosition().y <= -gameMode.BOUND_Y + 20f) {
            dest.y = -gameMode.BOUND_Y - 100;
        } else if (myFruit.getPosition().y >= gameMode.BOUND_Y - 20f) {
            dest.y = gameMode.BOUND_Y + 100;
        }
    }

    protected boolean atBounds(){
        return myFruit.getPosition().x <= -gameMode.BOUND_X + 20f ||
                myFruit.getPosition().x >= gameMode.BOUND_X - 20f||
                myFruit.getPosition().y <= -gameMode.BOUND_Y + 20f ||
                myFruit.getPosition().y >= gameMode.BOUND_Y - 20f;
    }

    protected float getDist(Vector2 pos){
        return (float) Math.sqrt(myFruit.getPosition().cpy().sub(pos.cpy()).len2());
    }

    protected void goToDest(Vector2 dest){
        Vector2 newPos = dest.cpy().sub(myFruit.getPosition().cpy());
        float angle = -newPos.angleRad() + 0.5f;
        normalize(newPos);
        if(getDist(dest) >= MAX_SPEED/0.025f) {
            speed = Math.min(speed + ACCEL, MAX_SPEED);
        }else {
            speed = Math.max(speed - ACCEL, 0.001f);
        }
        newPos.scl(speed);

        myFruit.move(newPos.x, newPos.y, angle);
    }

//    protected void goToDestTurn(){
//        Vector2 newPos = dest.cpy().sub(myFruit.getPosition().cpy());
//        normalize(newPos);
//        newPos.scl(speed);
//
//        float fruitAngle = (float) Math.toRadians(myFruit.getAngle());
//
//        System.out.println("fruit: " + fruitAngle);
//        System.out.println("dest: " + newPos.angleRad());
//        float angleDiff = Math.signum(-newPos.angleRad() + fruitAngle);
//
//
//        myFruit.move(0, 0, -newPos.angleRad());
//    }

    protected void runFromDest(Vector2 dest, int range){
        Vector2 newPos = myFruit.getPosition().cpy().sub(dest.cpy());
        normalize(newPos);
        if(getDist(dest) <= range) {
            speed = Math.min(speed + ACCEL, MAX_SPEED);
        } else{
            speed = Math.max(speed - ACCEL, 0.001f);
        }
        newPos.scl(speed);
        myFruit.move(newPos.x, newPos.y, -newPos.angleRad());
    }

    /**
     * Update the enemy ship (by one tick)
     *
     * @param frameCount how many frames have elapsed
     */

    public void update(int frameCount){
//        if (myFruit.isStunned){
//            myFruit.stunTimer -= frameCount;
//            myFruit.stunTimer = Math.max(myFruit.stunTimer, 0);
//            myFruit.isStunned = (myFruit.stunTimer != 0);
//            return;
//        }
        //Shoot if very close to player (mimics close range combat)
        if(getDist(gameMode.playerMonkey.getPosition()) < 300){
            setFire(true);
        }else{
            setFire(false);
        }
     }

    /**
     * Set whether or not this ship wants to fire
     * @param toFire whether to fire
     */
    protected void setFire(boolean toFire) {
        pressingFire = toFire;
    }

    /**
     * Whether the ship wishes to fire
     *
     * @return whether to fire
     */
    public boolean didPressFire() {
        return pressingFire;
    }


    /**
     * Returns the ship associated with this AI Controller
     *
     * @return ship
     */
    public Fruit getFruit() {
        return myFruit;
    }

    /**
     * Returns the ID of the ship associated with this AI Controller
     *
     * @return id
     */
    public int getID() {
        return myFruit.getId();
    }
}

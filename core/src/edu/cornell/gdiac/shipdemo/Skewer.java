package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.Queue;

/**
 * Model class representing a skewer.
 *
 */
public class Skewer {
    /** Standard skewer size */
    public static final int SKEWER_SIZE = 40;
    /** skewer mass */
    public static final float SKEWER_MASS = 0.05f;
    /** skewer distance from player center */
    public static final float SKEWER_DIST = 40;

    // Private constants to avoid use of "magic numbers"
    /** Fixed velocity for a skewer */
    private static final float SKEWER_VELOCITY = 6.5f;
    /** Number of animation frames a skewer lives before deleted */
    public static final int MAX_AGE = 20;

    /** Graphic asset representing a single skewer. */
    private static Texture texture;
    private Texture appleTexture;
    //    private Texture watermelonTexture;
//    private Texture pineappleTexture;
    private Texture strawberryTexture;
    private Texture orangeTexture;
    private Texture emptyTexture;


    /** Graphic asset representing a single skewer. */
    private static Texture punchTexture;


    public int damage;
    /** X-coordinate of skewer position */
    public float x;
    /** Y-coordinate of skewer position */
    public float y;
    /** X-coordinate of skewer position */
    public float xp;
    /** Y-coordinate of skewer position */
    public float yp;
    /** X-coordinate of skewer velocity */
    public float vx;
    /** X-coordinate of skewer velocity */
    public float vy;
    /** Age for the skewer in frames (for decay) */
    public int age;
    /** Relative size of this skewer*/
    public float size;
    private float ang;

    /* Fruit queue*/
    private Queue<FruitType> skewers;
    private int numSkewers;

    /**
     * Creates a new skewer
     */
    public Skewer() {
        this.x  = 0.0f; this.y  = 0.0f;
        this.vx = 0.0f; this.vy = 0.0f;
        this.age = 0;
        this.damage = 0;
    }
    public Skewer(int num_skewers){
        this.x  = 0.0f; this.y  = 0.0f;
        this.vx = 0.0f; this.vy = 0.0f;
        this.age = 0;
        this.damage = 0;
        this.numSkewers = num_skewers;
        this.skewers=new Queue<>();

    }

    private Texture getEnemy(FruitType i) {

        switch (i) {
            case EMPTY:
                return emptyTexture;
            case APPLE:
                return appleTexture;
            case STRAWBERRY:
                return strawberryTexture;
            case ORANGE:
                return orangeTexture;
        }
        return appleTexture;

    }
    public void setEnemyTexture(Texture strawberryTexture, Texture appleTexture, Texture orangeTexture, Texture emptyTexture){
        this.strawberryTexture = strawberryTexture;
        this.appleTexture = appleTexture;
        this.orangeTexture = orangeTexture;
        this.emptyTexture = emptyTexture;
    }
    public Queue<FruitType> getQueue(){
        return this.skewers;
    }
    public void makeEmpty(){
       while(skewers.notEmpty()){
           skewers.removeLast();
       }
    }
    public void acceptIngredient(FruitType type){
        int fruits_per_skewer=3;
        while (skewers.size >=fruits_per_skewer) {
            skewers.removeFirst();
        }
        skewers.addLast(type);
    }
    public FruitType getFruitAt(int i){
        return this.skewers.get(i);
    }

    /**
     * Moves the skewer within the given boundary.
     *
     * If the skewer goes outside of bounds, it bounces off the edge.
     * This method also advances the age of the skewer.
     */
    public void move() {
        x += vx;
        y += vy;


        // Finally, advance the age of the skewer.
        age++;
    }


    /**
     * Return this skewer's diameter
     */
    public float getDiameter() {
        return SKEWER_SIZE * size;
    }

    public Vector2 getPosition() {
        return new Vector2(x, y);
    }

    public Vector2 getTipPosition() {
        float xoffset = (float) (Math.cos(Math.toRadians(ang-90)) * texture.getHeight());
        float yoffset = (float) (Math.sin(Math.toRadians(ang-90)) * texture.getHeight());
        return new Vector2(x-xoffset, y-yoffset);
    }

    public Vector2 getPunchPosition() {
        float xoffset = (float) (Math.cos(Math.toRadians(ang-90)) * punchTexture.getHeight()/3);
        float yoffset = (float) (Math.sin(Math.toRadians(ang-90)) * punchTexture.getHeight()/3);
        return new Vector2(xp-xoffset, yp-yoffset);
    }

    public Vector2 getVelocity() {
        return new Vector2(vx, vy);
    }

    public float getMass() {
        return 0.5f;
    }

    public void setVelocity(Vector2 v){
        vx = v.x;
        vy = v.y;
    }

    public void setPosition(Vector2 v){
        x = v.x;
        y = v.y;
    }

    public void setPunchPosition(Vector2 v){
        xp = v.x;
        yp = v.y;
    }

    /**
     * Returns the image for a single skewer; reused by all skewers.
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * @return the image for a single skewer; reused by all skewers.
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * Sets the image for a single skewer; reused by all skewers.
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * @param value the image for a single skewer; reused by all skewers.
     */
    public void setTexture(Texture value) {
        texture = value;
    }

    /**
     * Sets the image for a single skewer; reused by all skewers.
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * @param value the image for a single skewer; reused by all skewers.
     */
    public void setPunchTexture(Texture value) {
        punchTexture = value;
    }


    /**
     * Process all skewer collisions against fruits
     *
     * @param fruit 				The ship to check collisions against
     * @param physicsController	The collision controller instance
     */
    public void fruitSkewerCollisions(Fruit fruit, CollisionController physicsController) {
        physicsController.checkForCollision(fruit, this);
//        physicsController.checkForStunCollision(fruit, this);


//        for (int i = 0; i < size; i++) {
//            // Find the position of this skewer.
//            int idx = ((head+i) % MAX_skewerS);
//            //TODO
//
//			/*
//			// Ensure skewers don't collide with their original ship
//			if((queue[idx].type == Ship.TYPE_PLAYER && fruit.getType() != Ship.TYPE_PLAYER) ||
//					(queue[idx].type != Ship.TYPE_PLAYER && fruit.getType() == Ship.TYPE_PLAYER)) {
//				// Compute collision
//				physicsController.checkForCollision(fruit, queue[idx]);
//			}
//
//			 */
//        }
    }

    public void fruitPunchCollisions(Fruit s, CollisionController physicsController) {
        physicsController.checkForStunCollision(s, this);
    }

    /**
     * Draws the skewers to the drawing canvas.
     *
     * This method uses additive blending, which is set before this method is
     * called (in GameMode).
     *
     * @param canvas The drawing canvas.
     */

    public void draw(GameCanvas canvas, float scale, OrderController orderController) {

        if (texture == null) {
            return;
        }
        Queue<FruitType> q= orderController.getSkewer().getQueue();

        // Get skewer texture origin
        float ox = texture.getWidth()/2.0f;
        float oy = texture.getHeight()/2.0f;

        canvas.draw(texture,Color.WHITE,ox,oy,x,y,this.ang,scale,scale);

        canvas.draw(punchTexture,Color.WHITE,ox,oy,xp,yp,this.ang,scale/3,scale/3);

        canvas.draw(texture,Color.WHITE,ox,oy,x,y,this.ang,scale,scale*2);
        for (int i=0; i<q.size; i++){
            Color c = Color.WHITE;
            if(orderController.getSkewer().getFruitAt(i)==FruitType.EMPTY) {
                c = new Color(0, 0, 0, 1);
            }
            float dist;
            if(i==2){
                dist=30;
            } else if (i==1) {
                dist=15;
            }
            else{
                dist=0;
            }
            Texture enemy = getEnemy(orderController.getSkewer().getFruitAt(i));
            canvas.draw(enemy,Color.WHITE,enemy.getWidth()/2f,enemy.getHeight()/2f,
                    x + -(float)Math.cos((ang - 90) * Math.PI / 180) * dist,
                    y + -(float)Math.sin((ang - 90) * Math.PI / 180) * dist,
                    ang,scale/3,scale/3);
        }
    }

    public void setOrientation(float ang) {
        this.ang = ang;
    }


}

/*
 * monkey.java
 *
 * This class tracks all of the state (position, velocity, rotation) of a
 * single monkey. In order to obey the separation of the model-view-controller
 * pattern, controller specific code (such as reading the keyboard) is not
 * present in this class.
 *
 * Looking through this code you will notice certain optimizations. We want
 * to eliminate as many "new" statements as possible in the draw loop. In
 * game programming, it is considered bad form to have "new" statements in
 * an update or a graphics loop if you can easily avoid it.  Each "new" is
 * a potentially  expensive memory allocation.
 *
 * To get around this, we have predeclared some Vector2 objects.  These are
 * used by the draw method to position the objects on the screen. As we know
 * we will need that memory animation frame, it is better to have them
 * declared ahead of time (even though we are not taking state across frame
 * boundaries).
 *
 * Author: Walker M. White
 * Based on original GameX monkey Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/3/2015
 */
package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.util.*;

import static edu.cornell.gdiac.shipdemo.Skewer.SKEWER_DIST;

/**
 * Model class representing a monkey.
 *
 * Note that the graphics resources in this class are static.  That
 * is because all monkeys share the same image file, and it would waste
 * memory to load the same image file for each monkey.
 */
public class Monkey extends CharacterModel {
    public static final int MONKEY_HP = 10;

    // Private constants to avoid use of "magic numbers"
    /** The size of the monkey in pixels (image is square) */
    public static final int MONKEY_SIZE  = 70;
    /** Amount to adjust forward movement from input */
    public static final float THRUST_FACTOR   = 7f;
    /** Highest speed allowed */
    public static final float BANK_FACTOR     = 0.5f;
    /** Maximum turning/banking speed */
    public static final float MAXIMUM_BANK    = 10.0f;
    /** Amount to decay forward thrust over time */
    public static final float FORWARD_DAMPING = 0.95f;
    /** Amount to angular movement over time */
    public static final float ANGULAR_DAMPING = 0.875f;
    /** The number of frames until we can fire again */
    public static final int   RELOAD_RATE = 30;

    // Modify this as part of the lab
    /** Amount to scale the monkey size */
    public static final float DEFAULT_SCALE = 1.0f;

    /** Boost cooldown length in frames */
    public static final int BOOST_COOLDOWN_LENGTH = 70;
    /** Amount of velocity the boost adds */
    public static final int BOOST_FACTOR = 2;
    private int hp;

    /** Position of the monkey */
    public Vector2 pos;
    /** Velocity of the monkey */
    private Vector2 vel;
    /** Color to tint this monkey (red or blue) */
    public Color  tint;
    /** Color of the monkeys shadow (cached) */
    public Color stint;

    /** Mass/weight of the monkey. Used in collisions. */
    public float mass;

    // The following are protected, because they have no accessors
    /** Offset of the monkeys target */
    public Vector2 tofs;
    /** Current angle of the monkey */
    public float ang;
    /** Accumulator variable to turn faster as key is held down */
    public float dang;
    /** Countdown to limit refire rate */
    public int refire;
    /** Countdown to limit refire rate */
    public int refirePunch;
    /** Size of the monkey scaled relative to MONKEY_SIZE */
    public float size;
    /** monkey type */
    public int type;
    /** Frames left until boost is available */
    public int boostCooldown;
    /** Player's skewer */
    public Skewer skewer;

    // Asset references.  These should be set by GameMode
    /** Texture for the monkey */
    private Texture monkeyTexture;
    /** Texture for the heart */
    private Texture heartTexture;
    /** Reference to ship's sprite for drawing */
    private FilmStrip monkeySprite;

    // ACCESSORS

    /**
     * Returns the image texture for the skewer
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * @return the image texture for the skewer
     */
    public Texture getSkewerTexture() {
        return skewer.getTexture();
    }

    /**
     * Sets the image texture for the skewer
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * param value the image texture for the skewer
     */
    public void setSkewerTexture(Texture value, Texture strawberrySkTexture,Texture appleSkTexture, Texture orangeSkTexture, Texture emptySkTexture) {
        skewer.setTexture(value);
        skewer.setEnemyTexture(strawberrySkTexture, appleSkTexture, orangeSkTexture, emptySkTexture);

    }

    /**
     * Sets the image texture for the skewer
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * param value the image texture for the skewer
     */
    public void setPunchTexture(Texture value) {
        skewer.setPunchTexture(value);
    }

    /**
     * Sets the image heart texture
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * param value the image heart texture
     */
    public void setHeartTexture(Texture value) {
        heartTexture = value;
    }

    /**
     * Sets the image monkey texture
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * param value the image monkey texture
     */
    public void setMonkeyTexture(Texture value) {
        monkeyTexture = value;
    }

    /**
     * Sets the image texture for this ship
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * param value the image texture for this ship
     */
    public void setFilmStrip(FilmStrip value) {
        monkeySprite = value;
        monkeySprite.setFrame(0);
    }

    /**
     * Returns the position of this monkey.
     *
     * This is location of the center pixel of the monkey on the screen.
     *
     * @return the position of this monkey
     */
    public Vector2 getPosition() {
        return pos;
    }

    /**
     * Sets the position of this monkey.
     *
     * This is location of the center pixel of the monkey on the screen.
     *
     * @param value the position of this monkey
     */
    public void setPosition(Vector2 value) {
        pos.set(value);
    }

    /**
     * Returns the velocity of this monkey.
     *
     * This value is necessary to control momementum in monkey movement.
     *
     * @return the velocity of this monkey
     */
    public Vector2 getVelocity() {
        return vel;
    }

    /**
     * Sets the velocity of this monkey.
     *
     * This value is necessary to control momementum in monkey movement.
     *
     * @param value the velocity of this monkey
     */
    public void setVelocity(Vector2 value) {
        vel.set(value);
    }

    /**
     * Returns the angle that this monkey is facing.
     *
     * The angle is specified in degrees, not radians.
     *
     * @return the angle of the monkey
     */
    public float getAngle() {
        return ang;
    }

    /**
     * Sets the angle that this monkey is facing.
     *
     * The angle is specified in degrees, not radians.
     *
     * @param value the angle of the monkey
     */
    public void setAngle(float value) {
        ang = value;
    }

    /**
     * Returns the tint color for this monkey.
     *
     * We can change how an image looks without loading a new image by
     * tinting it differently.
     *
     * @return the tint color
     */
    public Color getColor() {
        return tint;
    }

    /**
     * Sets the tint color for this monkey.
     *
     * We can change how an image looks without loading a new image by
     * tinting it differently.
     *
     * @param value the tint color
     */
    public void setColor(Color value) {
        tint.set(value);
    }

    /**
     * Returns true if the monkey can fire its weapon
     *
     * Weapon fire is subjected to a cooldown.  You can modify this
     * to see what happens if RELOAD_RATE is faster or slower.
     *
     * @return true if the monkey can fire
     */
    public boolean canFireWeapon() {
        return (refire > RELOAD_RATE);
    }

    /**
     * Returns true if the monkey can fire its weapon
     *
     * Weapon fire is subjected to a cooldown.  You can modify this
     * to see what happens if RELOAD_RATE is faster or slower.
     *
     * @return true if the monkey can fire
     */
    public boolean canPunch() {
        return (refirePunch > RELOAD_RATE);
    }

    /**
     * Resets the reload counter so the monkey cannot fire again immediately.
     *
     * The monkey must wait RELOAD_RATE steps before it can fire.
     */
    public void reloadWeapon() {
        refire = 0;
    }

    /**
     * Resets the reload counter so the monkey cannot fire again immediately.
     *
     * The monkey must wait RELOAD_RATE steps before it can fire.
     */
    public void reloadPunch() {
        refirePunch = 0;
    }

    /**
     * Returns the mass of the monkey.
     *
     * This value is necessary to resolve collisions.
     *
     * @return the monkey mass
     */
    public float getMass() {
        return mass;
    }

    /**
     * Returns the diameter of the monkey monkey.
     *
     * This value is necessary to resolve collisions.
     *
     * @return the monkey diameter
     */
    public float getDiameter() {
        return MONKEY_SIZE * size;
    }

    /**
     * Returns the type of the monkey.
     *
     * @return the monkey type
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the HP of the monkey.
     *
     * @return the monkey type
     */
    public int getHP() {
        return hp;
    }

    /**
     * Decreases the health of the monkey by damage param
     * HP lower bounded by 0
     *
     * @param damage the damage taken by the monkey
     */
    public void takeDamage(int damage) {
        this.hp -= damage;
        this.hp = Math.max(this.hp, 0);
    }

    /**
     * Creates a new monkey at the given location with the given facing.
     *
     * @param x The initial x-coordinate of the center
     * @param y The initial y-coordinate of the center
     * @param ang The initial angle of rotation
     * @param size The size scaling of this monkey
     */
    public Monkey(float x, float y, float ang, float size) {
        // Set the position of this monkey.
        this.pos = new Vector2(x,y);
        this.ang = ang;
        this.size = size;

        // We start at rest.
        vel = new Vector2();
        dang = 0.0f;
        mass = 1.0f;

        // Currently no target sited.
        tofs = new Vector2();
        refire = RELOAD_RATE;
        refirePunch = RELOAD_RATE;

        //Set current monkey image
        tint  = new Color(Color.WHITE);
        stint = new Color(0.0f,0.0f,0.0f,0.5f);
        skewer = new Skewer();
        //this.setColor(Color.WHITE);

        this.hp = MONKEY_HP;
    }

    /**
     * Moves the monkey by the specified amount.
     *
     * Forward is the amount to move forward, while turn is the angle to turn the monkey
     * (used for the "banking" animation. This method performs no collision detection.
     * Collisions are resolved afterwards.
     *
     * @param upward	Amount to move up
     * @param rightward		Amount to move right
     * @param angle     Angle to face
     */
    public void move(float upward, float rightward, float angle) {
        // Process the monkey turning.
        setAngle(angle * 360 / ((float)Math.PI * 2));

        pos.add(upward * THRUST_FACTOR,rightward * THRUST_FACTOR);
        // Gradually slow the monkey down
        vel.scl(FORWARD_DAMPING);


        // Move the monkey, updating it.
        // Adjust the angle by the change in angle
        ang += dang;  // INVARIANT: -360 < ang < 720
        if (ang > 360)
            ang -= 360;
        if (ang < 0)
            ang += 360;

        // Move the monkey position by the monkey velocity
        pos.add(vel);

        //Increment the refire readiness counter
        if (refire <= RELOAD_RATE) {
            refire++;
        }

        if (refirePunch <= RELOAD_RATE) {
            refirePunch++;
        }

        // Progress boost cooldown
        if(boostCooldown > 0)
            boostCooldown--;
        float rotate = -(180+ang);
        tofs.x = pos.x + (float) (Math.cos(Math.toRadians(rotate-180)) * 30); tofs.y = pos.y + (float) (Math.sin(Math.toRadians(rotate-180)) * 30);
        float xoffset = (float) (Math.cos(Math.toRadians(rotate-180)) * 100);
        float yoffset = (float) (Math.sin(Math.toRadians(rotate-180)) * 100);
        if(refire < RELOAD_RATE / 2){
            tofs.x += xoffset * (float)refire / RELOAD_RATE; tofs.y += yoffset * (float)refire / RELOAD_RATE;
        } else {
            tofs.x += xoffset * 0.5; tofs.y += yoffset * 0.5;
            tofs.x -= xoffset * (refire-RELOAD_RATE/2.0) / RELOAD_RATE; tofs.y -= yoffset * (refire-RELOAD_RATE/2.0) / RELOAD_RATE;
        }
        if (refire == RELOAD_RATE) {
            skewer.damage = 0;
        }
        tofs.y -= 30;
        skewer.setPosition(tofs);
        skewer.setOrientation(rotate+90);

        tofs.x = pos.x + (float) (Math.cos(Math.toRadians(rotate-180)) * 30); tofs.y = pos.y + (float) (Math.sin(Math.toRadians(rotate-180)) * 30);
        xoffset = (float) (Math.cos(Math.toRadians(rotate-180)) * 100);
        yoffset = (float) (Math.sin(Math.toRadians(rotate-180)) * 100);
        if(refirePunch < RELOAD_RATE / 2){
            tofs.x += xoffset * (float)refirePunch / RELOAD_RATE; tofs.y += yoffset * (float)refirePunch / RELOAD_RATE;
        } else {
            tofs.x += xoffset * 0.5; tofs.y += yoffset * 0.5;
            tofs.x -= xoffset * (refirePunch-RELOAD_RATE/2.0) / RELOAD_RATE; tofs.y -= yoffset * (refirePunch-RELOAD_RATE/2.0) / RELOAD_RATE;
        }
        // TODO: This needs to be changed based on the behavior we want for punches
        if (refirePunch == RELOAD_RATE) {
            skewer.damage = 0;
        }
        tofs.y -= 30;
        skewer.setPunchPosition(tofs);


        // set skewer position to current position plus some function of refire vs. RELOAD_RATE

    }


    /**
     * Draws the monkey (and its related images) to the given GameCanvas.
     *
     * You will want to modify this method for Exercise 4.
     *
     * This method uses alpha blending, which is set before this method is
     * called (in GameMode).
     *
     * @param canvas The drawing canvas.
     */
    public void drawMonkey(GameCanvas canvas, OrderController orderController) {
//        if (monkeyTexture == null) return;
        if (monkeySprite == null) return;
//		// For placement purposes, put origin in center.
        float ox = 0.5f * monkeySprite.getRegionWidth();
        float oy = 0.5f * monkeySprite.getRegionHeight();
        float rotate = -(180+ang);
//        canvas.draw(monkeyTexture, Color.WHITE, ox, oy, pos.x, pos.y, rotate, DEFAULT_SCALE/1.5f, DEFAULT_SCALE/1.5f);
        // TODO: Use monkey sprite instead of monkey texture
//        monkeySprite.setFrame();
        float dir = ang - 90;
        if (dir < 0){
            dir += 360;
        }
        int animationFrame = (int) (dir / 360 * 8);
        monkeySprite.setFrame(animationFrame);
        canvas.draw(monkeySprite, Color.WHITE, ox, oy, pos.x, pos.y, 0, DEFAULT_SCALE, DEFAULT_SCALE);
        int xoffset = -2;
        int yoffset = 0;

        for (int i = 1; i <= this.hp; i++) {
//            if(heartTexture != null) canvas.draw(heartTexture, Color.WHITE, ox, oy, 80 + pos.x - xoffset*20, pos.y + MONKEY_SIZE + yoffset * 25, 0, DEFAULT_SCALE, DEFAULT_SCALE);
            if(heartTexture != null) canvas.draw(heartTexture, Color.WHITE, 0.5f * heartTexture.getWidth(), 0.5f * heartTexture.getHeight(), (float) (pos.x + xoffset*(1.0/10.0 * monkeyTexture.getWidth())), pos.y + MONKEY_SIZE + yoffset * 25, 0, DEFAULT_SCALE, DEFAULT_SCALE);
            xoffset++;
            if(i % 5 == 0){
                yoffset++;
                xoffset = -2;
            }
        }
        skewer.draw(canvas, DEFAULT_SCALE, orderController);

    }

    /**
     * Boosts the monkeys. The monkey will have to wait before it can boost again.
     */
    public void boost() {
        vel.add(BOOST_FACTOR * (float)Math.cos(Math.toRadians (ang)) * THRUST_FACTOR,
                BOOST_FACTOR * (float)-Math.sin (Math.toRadians (ang)) * THRUST_FACTOR);
        boostCooldown = BOOST_COOLDOWN_LENGTH;
    }

    /**
     * Checks if the monkey can boost
     *
     * @return Whether the monkey can boost
     */
    public boolean canBoost() {
        return boostCooldown == 0;
    }

    /**
     * Returns whether this monkey is dead
     *
     * @return whether dead
     */
    public boolean isDead() {
        return this.hp == 0;
    }
}

/*
 * Ship.java
 *
 * This class tracks all of the state (position, velocity, rotation) of a
 * single ship. In order to obey the separation of the model-view-controller
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
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/3/2015
 */
package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import edu.cornell.gdiac.util.*;

/**
 * Model class representing an alien ship.
 *
 * Note that the graphics resources in this class are static.  That
 * is because all ships share the same image file, and it would waste
 * memory to load the same image file for each ship.
 */
public abstract class Fruit extends CharacterModel {
    // Ship Frame Sprite numbers
    /** The frame number for the tightest bank for a left turn */
    public static final int SHIP_IMG_LEFT = 0;
    /** The frame number for a ship that is not turning */
    public static final int SHIP_IMG_FLAT = 9;
    /** The frame number for the tightest bank for a right turn */
    public static final int SHIP_IMG_RIGHT = 17;
    /** Red ship type */
    public static final int TYPE_PLAYER = 0;
    /** Blue ship type */
    public static final int TYPE_BLUE = 1;
    /** Red ship type */
    public static final int TYPE_RED = 2;
    /** Blue ship type */
    public static final int TYPE_GREEN = 3;

    // Private constants to avoid use of "magic numbers"
    /** The size of the ship in pixels (image is square) */
    public static final int SHIP_SIZE  = 81;
    /** The amount to offset the shadow image by */
    public static final float SHADOW_OFFSET = 10.0f;
    /** The size of the target reticule in pixels (image is square) */
    public static final int TARGET_SIZE = 19;
    /** Distance from ship to target reticule */
    public static final int TARGET_DIST = 100;
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
    public static final int RELOAD_RATE = 50;

    // Modify this as part of the lab
    /** Amount to scale the ship size */
    public static final float DEFAULT_SCALE = 1.0f;

    /** Boost cooldown length in frames */
    public static final int BOOST_COOLDOWN_LENGTH = 70;
    /** Amount of velocity the boost adds */
    public static final int BOOST_FACTOR = 40;

    private int hp;

    /** Position of the ship */
    public Vector2 pos;
    /** Velocity of the ship */
    private Vector2 vel;
    /** Color to tint this ship (red or blue) */
    public Color  tint;
    /** Color of the ships shadow (cached) */
    public Color stint;

    /** Mass/weight of the ship. Used in collisions. */
    public float mass;

    // The following are protected, because they have no accessors
    /** Offset of the ships target */
    public Vector2 tofs;
    /** Current angle of the ship */
    public float ang;
    /** Accumulator variable to turn faster as key is held down */
    public float dang;
    /** Countdown to limit refire rate */
    public int refire;
    /** Size of the ship scaled relative to SHIP_SIZE */
    public float size;
    /** Ship type */
    public int type;
    /** Stunned attribute */
    public boolean isStunned;
    public int stunTimer;
    /** Frames left until boost is available */
    public int boostCooldown;
    private int id;

    // Asset references.  These should be set by GameMode
    /** Reference to ship's sprite for drawing */
    private Texture shipSprite;
    /** Texture for the target reticule */
    private Texture weaponTexture;
    /** Texture for the heart */
    private Texture heartTexture;

    // ACCESSORS
    /**
     * Returns the image filmstrip for this ship
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * @return the image texture for this ship
     */
    public Texture getTexture() {
        return shipSprite;
    }

    /**
     * Sets the image texture for this ship
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * param value the image texture for this ship
     */
    public void setTexture(Texture value) {
        shipSprite = value;
    }

    /**
     * Returns the image texture for the target reticule
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * @return the image texture for the target reticule
     */
    public Texture getWeaponTexture() {
        return weaponTexture;
    }

    /**
     * Sets the image texture for the target reticule
     *
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * param value the image texture for the target reticule
     */
    public void setWeaponTexture(Texture value) {
        weaponTexture = value;
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
     * Returns the position of this ship.
     *
     * This is location of the center pixel of the ship on the screen.
     *
     * @return the position of this ship
     */
    public Vector2 getPosition() {
        return pos;
    }

    /**
     * Sets the position of this ship.
     *
     * This is location of the center pixel of the ship on the screen.
     *
     * @param value the position of this ship
     */
    public void setPosition(Vector2 value) {
        pos.set(value);
    }

    /**
     * Returns the velocity of this ship.
     *
     * This value is necessary to control momementum in ship movement.
     *
     * @return the velocity of this ship
     */
    public Vector2 getVelocity() {
        return vel;
    }

    /**
     * Sets the velocity of this ship.
     *
     * This value is necessary to control momementum in ship movement.
     *
     * @param value the velocity of this ship
     */
    public void setVelocity(Vector2 value) {
        vel.set(value);
    }

    /**
     * Returns the angle that this ship is facing.
     *
     * The angle is specified in degrees, not radians.
     *
     * @return the angle of the ship
     */
    public float getAngle() {
        return ang;
    }

    /**
     * Sets the angle that this ship is facing.
     *
     * The angle is specified in degrees, not radians.
     *
     * @param value the angle of the ship
     */
    public void setAngle(float value) {
        ang = value;
    }

    /**
     * Returns the tint color for this ship.
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
     * Sets the tint color for this ship.
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
     * Returns true if the ship can fire its weapon
     *
     * Weapon fire is subjected to a cooldown.  You can modify this
     * to see what happens if RELOAD_RATE is faster or slower.
     *
     * @return true if the ship can fire
     */
    public boolean canFireWeapon() {
        return (refire > RELOAD_RATE);
    }

    /**
     * Resets the reload counter so the ship cannot fire again immediately.
     *
     * The ship must wait RELOAD_RATE steps before it can fire.
     */
    public void reloadWeapon() {
        refire = 0;
    }

    /**
     * Returns the mass of the ship.
     *
     * This value is necessary to resolve collisions.
     *
     * @return the ship mass
     */
    public float getMass() {
        return mass;
    }

    /**
     * Returns the diameter of the ship ship.
     *
     * This value is necessary to resolve collisions.
     *
     * @return the ship diameter
     */
    public float getDiameter() {
        return SHIP_SIZE * size;
    }

    /**
     * Returns the type of this fruit as enum
     * May be convenient for switch statements (versus instanceof)
     *
     * @return the fruit type
     */
    public abstract FruitType getType();

    /**
     * Returns the HP of the ship.
     *
     * @return the ship type
     */
    public int getHP() {
        return hp;
    }

    /**
     * Decreases the health of the ship by damage param
     * HP lower bounded by 0
     *
     * @param damage the damage taken by the ship
     */
    public void takeDamage(int damage) {
        this.hp -= damage;
        this.hp = Math.max(this.hp, 0);
    }

    /**
     * Creates a new ship at the given location with the given facing.
     *
     * @param x The initial x-coordinate of the center
     * @param y The initial y-coordinate of the center
     * @param ang The initial angle of rotation
     * @param size The size scaling of this ship
     */
    public Fruit(float x, float y, float ang, float size, int id, int hp) {
        // Set the position of this ship.
        this.pos = new Vector2(x,y);
        this.ang = ang;
        this.size = size;

        // We start at rest.
        vel = new Vector2();
        dang = 0.0f;
        mass = 1.0f;

        // Currently no target sited.
        tofs = new Vector2();
        refire = 0;

        //Set current ship image
        tint  = new Color(Color.WHITE);
        stint = new Color(0.0f,0.0f,0.0f,0.5f);
        this.setColor(Color.WHITE);

        this.id = id;
        this.hp = hp;
        this.isStunned = false;
        this.stunTimer = 0;
    }

    /**
     * Moves the ship by the specified amount.
     *
     * Forward is the amount to move forward, while turn is the angle to turn the ship
     * (used for the "banking" animation. This method performs no collision detection.
     * Collisions are resolved afterwards.
     *
     * @param upward	Amount to move up
     * @param rightward		Amount to move right
     * @param angle     Angle to face
     */
    public void move(float upward, float rightward, float angle) {
        // Process the ship turning.
        setAngle(angle * 360 / ((float)Math.PI * 2));

        pos.add(upward * THRUST_FACTOR,rightward * THRUST_FACTOR);
        // Gradually slow the ship down
        vel.scl(FORWARD_DAMPING);

        // Move the ship, updating it.
        // Adjust the angle by the change in angle
        ang += dang;  // INVARIANT: -360 < ang < 720
        if (ang > 360)
            ang -= 360;
        if (ang < 0)
            ang += 360;

        // Move the ship position by the ship velocity
        pos.add(vel);

        //Increment the refire readiness counter
        if (refire <= RELOAD_RATE) {
            refire++;
        }

        // Progress boost cooldown
        if(boostCooldown > 0)
            boostCooldown--;
    }


    /**
     * Aim the target reticule at the opponent
     *
     * The target reticule always shows the location of our opponent.  In order
     * to place it we need to know where our opponent is.  This method is
     * called by the game engine to let us know the location of our
     * opponent.
     *
     *
     */
    public void acquireTarget() {
        /*
        // Calculate vector to 2nd ship
        tofs.set(other.pos).sub(this.pos);  // tofs = other.pos - this.pos (and not a reference to other.pos)

        // Scale it so we can draw it.
        tofs.nor();
        tofs.scl(TARGET_DIST);

         */
    }

    /**
     * Draws the ship (and its related images) to the given GameCanvas.
     *
     * You will want to modify this method for Exercise 4.
     *
     * This method uses alpha blending, which is set before this method is
     * called (in GameMode).
     *
     * @param canvas The drawing canvas.
     */
    public void drawShip(GameCanvas canvas) {
        if (shipSprite == null) {
            return;
        }
        // For placement purposes, put origin in center.
        float ox = 0.5f * shipSprite.getWidth();
        float oy = 0.5f * shipSprite.getHeight();

        // How much to rotate the image
        float rotate = -(90+ang);

        // Draw the shadow.  Make a translucent color.
        // Position it offset by 10 so it can be seen.
        float sx = pos.x+SHADOW_OFFSET;
        float sy = pos.y+SHADOW_OFFSET;

        // Need to negate y scale because of coordinate access flip.
        // Draw the shadow first
        Color s=stint;
        Color t=tint;
        if(isSpiked){
            s=Color.RED;
            t= Color.RED;
        }

        canvas.draw(shipSprite, s, ox, oy, sx, sy, ang - 180, DEFAULT_SCALE * size, DEFAULT_SCALE * size);
        // Then draw the ship
        canvas.draw(shipSprite, t, ox, oy, pos.x, pos.y, ang - 180, DEFAULT_SCALE * size, DEFAULT_SCALE * size);

        // Draw the HP
        int xoffset = 0;
        int yoffset = 0;

        for (int i = 1; i <= this.hp; i++) {
//			canvas.draw(shipSprite, tint, ox, oy, 70 + pos.x - i*20, pos.y + 50, 0, DEFAULT_SCALE/4.0f * size, DEFAULT_SCALE/4.0f * size);
            if(heartTexture != null) canvas.draw(heartTexture, Color.WHITE, ox, oy, 80 + pos.x - xoffset*20, pos.y + SHIP_SIZE + yoffset * 25, 0, DEFAULT_SCALE, DEFAULT_SCALE);
            xoffset++;
            if(i % 5 == 0){
                yoffset++;
                xoffset = 0;
            }
        }
//		canvas.draw(shipSprite, tint, ox, oy, pos.x + 50, pos.y + 50, 0, DEFAULT_SCALE/5.0f * size, DEFAULT_SCALE/5.0f * size);
        // Draw hit animation
        float hitTintMagnitude = Math.max((float)( boostCooldown) / BOOST_COOLDOWN_LENGTH, 0);
        Color hitTint = new Color(hitTintMagnitude, hitTintMagnitude, 0, hitTintMagnitude);
        canvas.draw(shipSprite, hitTint, ox, oy, pos.x, pos.y, ang, DEFAULT_SCALE * size, DEFAULT_SCALE * size);
    }

    /**
     * Draw the target cursor
     *
     * You will want to modify this method for Exercise 4.
     *
     * This method uses additive blending, which is set before this method is
     * called (in GameMode).
     *
     * @param canvas The drawing canvas.
     */
    public void drawSkewer(GameCanvas canvas) {
        if (weaponTexture == null) {
            return;
        }

        // Target position
        float tx = pos.x + tofs.x;
        float ty = pos.y + tofs.y;

        // For placement purposes, put origin in center.
        float ox = 0.5f * TARGET_SIZE;
        float oy = 0.5f * TARGET_SIZE;

        canvas.draw(weaponTexture, Color.WHITE, ox, oy, tx, ty, 0, DEFAULT_SCALE, DEFAULT_SCALE);
    }

    /**
     * Boosts the ships. The ship will have to wait before it can boost again.
     */
    public void boost() {
        vel.add(BOOST_FACTOR * (float)Math.cos(Math.toRadians (ang)) * THRUST_FACTOR,
                BOOST_FACTOR * (float)-Math.sin (Math.toRadians (ang)) * THRUST_FACTOR);
        boostCooldown = BOOST_COOLDOWN_LENGTH;
    }

    /**
     * Checks if the ship can boost
     *
     * @return Whether the ship can boost
     */
    public boolean canBoost() {
        return boostCooldown == 0;
    }

    /**
     * Returns whether this ship is dead
     *
     * @return whether dead
     */
    public boolean isDead() {
        return this.hp == 0;
    }

    public int getId() {
        return id;
    }
}

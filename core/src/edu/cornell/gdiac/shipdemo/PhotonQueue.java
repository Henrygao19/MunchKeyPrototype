/*
 * PhotonQueue.cs
 *
 * This class implements a "particle system" that manages the photons fired
 * by either ship in the game.  When a ship fires a photon, it adds it to this
 * particle system.  The particle system is responsible for moving (and drawing)
 * the photon particle.  It also keeps track of the age of the photon.  Photons
 * that are too old are deleted, so that they are not bouncing about the game
 * forever.
 * 
 * The PhotonQueue is exactly what it sounds like: a queue. In this implementation
 * we use the circular array implementation of a queue (which you may have learned
 * in CS 2110).  Why do we do this when C# has perfectly good Collection classes?
 * Because in game programming it is considered bad form to have "new" statements
 * in an update or a graphics loop if you can easily avoid it.  Each "new" is
 * a potentially expensive memory allocation.  It is (often) much better to 
 * allocate all the memory that you need at start-up, so that all you do is
 * assign variables during game time. If you notice, all the Photon objects
 * are declared and initialized in the constructor; we just reassign the fields
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * MonoGame version, 12/30/2013
 */
package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;

/**
 * Model class representing an "particle system" of photons.
 *
 * Note that the graphics resources in this class are static.  That
 * is because all photons share the same image file, and it would waste
 * memory to load the same image file for each photon.
 */
public class PhotonQueue {
	/** Standard photon size */
	public static final int PHOTON_SIZE = 40;
	/** Photon mass */
	public static final float PHOTON_MASS = 0.05f;

    // Private constants to avoid use of "magic numbers"
	/** Fixed velocity for a photon */
	private static final float PHOTON_VELOCITY = 8f;
	/** Number of animation frames a photon lives before deleted */
	private static final int MAX_AGE = 60;
	/** Maximum number of photons allowed on screen at a time. */
	private static final int MAX_PHOTONS = 512;

	/** Graphic asset representing a single photon. */
	private static Texture texture;

	// QUEUE DATA STRUCTURES
	/** Array implementation of a circular queue. */
	protected Photon[] queue;
	/** Index of head element in the queue */
	protected int head;
	/** Index of tail element in the queue */
	protected int tail;
	/** Number of elements currently in the queue */
	protected int size;
	
	/**
	 * An inner class that represents a single Photon.
	 * 
	 * To count down on memory references, the photon is "flattened" so that
	 * it contains no other objects.
     */
	public class Photon {
		public int damage;
		/** X-coordinate of photon position */
		public float x; 
		/** Y-coordinate of photon position */
		public float y;
		/** X-coordinate of photon velocity */
		public float vx; 
		/** X-coordinate of photon velocity */
		public float vy;
		/** Age for the photon in frames (for decay) */
		public int age;
		/** The type of ship that fired this photon*/
		public FruitType type;
		/** Relative size of this photon*/
		public float size;

		public boolean hasHit;
		
		/**
		 * Creates a new empty photon with age -1.
		 * 
		 * Photons created this way "do not exist".  This constructor is
		 * solely for preallocation.  To actually use a photon, use the
		 * allocate() method.
		 */
		public Photon() {
			this.x  = 0.0f; this.y  = 0.0f;
			this.vx = 0.0f; this.vy = 0.0f;
			this.age = -1;
			this.damage = 1;
        }
		
		/**
		 * Allocates a photon by setting its position and velocity.
		 * A newly allocated photon starts with age 0.
		 *
		 * @param x  The x-coordinate of the position
		 * @param y  The y-coordinate of the position
		 * @param vx The x-coordinate of the velocity
		 * @param vy The y-coordinate of the velocity
		 */
		public void allocate(float x, float y, float vx, float vy, FruitType type) {
			this.x  = x;  this.y  = y;
			this.vx = vx; this.vy = vy;
			this.age = 0;
			this.type = type;
			this.size = 1;

		}
		
		/**
		 * Moves the photon within the given boundary.
		 * 
		 * If the photon goes outside of bounds, it bounces off the edge.
		 * This method also advances the age of the photon.
		 */
		public void move(Rectangle bounds) {
			x += vx;
			y += vy;

            
            // Finally, advance the age of the photon.
            age++;
		}

		/**
		 * Return this photon's diameter
		 */
		public float getDiameter() {
			return PhotonQueue.PHOTON_SIZE * size * (1.25f - (float)age * 0.5f / (float)MAX_AGE);
		}

		public Vector2 getPosition() {
			return new Vector2(x, y);
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
	}

	/**
	 *  Constructs a new (empty) PhotonQueue
	 */
	public PhotonQueue() {
		// Construct the queue.
		queue = new Photon[MAX_PHOTONS];
		
        head = 0;
        tail = -1;
        size = 0;

        // "Predeclare" all the photons for efficiency
        for (int ii = 0; ii < MAX_PHOTONS; ii++) {
        	queue[ii] = new Photon();
        }
	}

	/** 
	 * Returns the image for a single photon; reused by all photons.
	 * 
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
	 * @return the image for a single photon; reused by all photons.
	 */
	public Texture getTexture() {
		return texture;
	}

	/** 
	 * Sets the image for a single photon; reused by all photons.
	 * 
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
	 * @param value the image for a single photon; reused by all photons.
	 */
	public void setTexture(Texture value) {
		texture = value;
	}

	/**
	 * Adds a photon to the active queue.
	 * 
	 * When adding a photon, we assume that it is fired from a ship with the
	 * given position, velocity, and facing (angle).  We could have a general
	 * photon adding function, but this will make refactoring easier in
	 * Exercise 9.
	 * 
	 * As all Photons are predeclared, this involves moving the head and the tail, 
	 * and reseting the values of the object in place.  This is a simple implementation
	 * of a memory pool. It works because we delete objects in the same order that
	 * we allocate them.
	 *
	 * @param position  The velocity of the ship firing the photon
	 * @param velocity  The position of the ship firing the photon
	 * @param angle		The facing of the ship firing the photon
	 * @param type		The type of ship firing
	 */
	public void addPhoton(Vector2 position, Vector2 velocity, float angle, FruitType type) {
		// Determine direction and velocity of the photon.
		float fire_x = velocity.x + (float)Math.cos(Math.toRadians(angle))  * PHOTON_VELOCITY;
		float fire_y = velocity.y + (float)-Math.sin(Math.toRadians(angle)) * PHOTON_VELOCITY;         
		
		// Check if any room in queue.  
		// If maximum is reached, remove the oldest photon.
        if (size == MAX_PHOTONS) {
        	head = ((head + 1) % MAX_PHOTONS);
        	size--;
        }

        // Add a new photon at the end.
        // Already declared, so just initialize.
        tail = ((tail + 1) % MAX_PHOTONS);
        queue[tail].allocate(position.x,position.y,fire_x,fire_y, type);
        size++;
	}
	
	/**
	 * Moves all the photons in the active queu.
	 *  
	 * Each photon is advanced according to its velocity. Photons out of bounds are 
	 * rebounded into view. Photons which are too old are deleted.
	 */
	public void move(Rectangle bounds) {
		// First, delete all old photons.  
        // INVARIANT: Photons are in queue in decending age order.
        // That means we just remove the head until the photons are young enough.
        while (size > 0 && queue[head].age > MAX_AGE) {
        	// As photons are predeclared, all we have to do is move head forward.
            head = ((head + 1) % MAX_PHOTONS);
            size--;
        }

        // Now, step through each active photon in the queue.
        for (int ii = 0; ii < size; ii++) {
        	// Find the position of this photon.
            int idx = ((head+ii) % MAX_PHOTONS);

            // Move the photon according to velocity.
            queue[idx].move(bounds);
        }
	}

	/**
	 * Process all photon collisions against the given ship
	 *
	 * @param fruit 				The ship to check collisions against
	 * @param physicsController	The collision controller instance
	 */
	public void shipPhotonCollisions(CharacterModel characterModel, CollisionController physicsController) {
		for (int i = 0; i < size; i++) {
			// Find the position of this photon.
			int idx = ((head+i) % MAX_PHOTONS);
			//TODO


			// Ensure photons don't collide with their original ship
			//if((queue[idx].type == Ship.TYPE_PLAYER && fruit.getType() != Ship.TYPE_PLAYER) ||
					//(queue[idx].type != Ship.TYPE_PLAYER && fruit.getType() == Ship.TYPE_PLAYER)) {
				// Compute collision
				physicsController.checkForCollision(characterModel, queue[idx]);
			//}
		}
	}

	/**
	 * Draws the photons to the drawing canvas.
	 *
	 * This method uses additive blending, which is set before this method is
	 * called (in GameMode).
	 *
	 * @param canvas The drawing canvas.
	 */
	public void draw(GameCanvas canvas) {
		if (texture == null) {
			return;
		}
		
		// Get photon texture origin
		float ox = texture.getWidth()/2.0f;
		float oy = texture.getHeight()/2.0f;
		
		// Step through each active photon in the queue.
        for (int ii = 0; ii < size; ii++) {
        	// Find the position of this photon.
            int idx = ((head + ii) % MAX_PHOTONS);

            // How big to make the photon.  Decreases with age.
            float scale = 1.25f - (float)queue[idx].age * 0.5f / (float)MAX_AGE;
			scale *= queue[idx].size;

			// Compute tint
			float ageRatio = 1 - (float)queue[idx].age / MAX_AGE;
			Color tint = Color.WHITE;
			/*
			if(queue[idx].type == Ship.TYPE_PLAYER)
				tint = new Color(ageRatio, ageRatio, ageRatio * 0.25f, ageRatio);
			else if(queue[idx].type == Ship.TYPE_RED)
				tint = new Color(ageRatio, ageRatio * 0.7f, ageRatio * 0.7f, ageRatio);
			else if(queue[idx].type == Ship.TYPE_GREEN)
				tint = new Color(ageRatio * 0.7f, ageRatio, ageRatio * 0.7f, ageRatio);
			else
				tint = new Color(ageRatio * 0.7f, ageRatio * 0.7f, ageRatio, ageRatio);

			 */

			// Use this information to draw.
			if(queue[idx].damage > 0)
            	canvas.draw(texture,tint,ox,oy,queue[idx].x,queue[idx].y,0,scale,scale);
        }
	}
}
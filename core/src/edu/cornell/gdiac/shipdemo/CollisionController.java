/* 
 * CollisionController.java
 * 
 * Unless you are making a point-and-click adventure game, every single 
 * game is going to need some sort of collision detection.  In a later 
 * lab, we will see how to do this with a physics engine. For now, we use
 * custom physics. 
 * 
 * This class is an example of subcontroller.  A lot of this functionality
 * could go into GameMode (which is the primary controller).  However, we
 * have factored it out into a separate class because it makes sense as a
 * self-contained subsystem.  Note that this class needs to be aware of
 * of all the models, but it does not store anything as fields.  Everything
 * it needs is passed to it by the parent controller.
 * 
 * This class is also an excellent example of the perils of heap allocation.
 * Because there is a lot of vector mathematics, we want to make heavy use
 * of the Vector2 class.  However, every time you create a new Vector2 
 * object, you must allocate to the heap.  Therefore, we determine the
 * minimum number of objects that we need and pre-allocate them in the
 * constructor.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.math.*;

/**
 * Controller implementing simple game physics.
 *  
 * This is the simplest of physics engines.  In later labs, we 
 * will see how to work with more interesting engines.
 */
public class CollisionController {

	/** Impulse for giving collisions a slight bounce. */
	public static final float COLLISION_COEFF = 0.1f;

	public static final int STUN_TIME = 60000;
	
	/** Caching object for computing normal */
	private Vector2 normal;

	/** Caching object for computing net velocity */
	private Vector2 velocity;
	
	/** Caching object for intermediate calculations */
	private Vector2 temp;

	/**
     * Contruct a new controller. 
     * 
     * This constructor initializes all the caching objects so that
     * there is no heap allocation during collision detection.
     */
	public CollisionController() { 
		velocity = new Vector2();
		normal = new Vector2();
		temp = new Vector2();
	}

	/** 
	 *  Handles collisions between ships, causing them to bounce off one another.
	 * 
	 *  This method updates the velocities of both ships: the collider and the 
	 *  collidee. Therefore, you should only call this method for one of the 
	 *  ships, not both. Otherwise, you are processing the same collisions twice.
	 * 
	 *  @param fruit1 First ship in candidate collision
	 *  @param fruit2 Second ship in candidate collision
	 */
	public void checkForCollision(CharacterModel fruit1, CharacterModel fruit2) {
		// Calculate the normal of the (possible) point of collision
		normal.set(fruit1.getPosition()).sub(fruit2.getPosition());
		float distance = normal.len();
		float impactDistance = (fruit1.getDiameter() + fruit2.getDiameter()) / 2f;
		normal.nor();

		// If this normal is too small, there was a collision
		if (distance < impactDistance) {

			if(fruit2.isSpiked && fruit1 instanceof Monkey){
				fruit2.setSpiked(false);
				fruit1.takeDamage(1);
			}

			if (fruit1.isSpiked && fruit2 instanceof Monkey){
				fruit1.setSpiked(false);
				fruit2.takeDamage(1);
			}

			// "Roll back" time so that the ships are barely touching (e.g. point of impact).
			// We need to use temp, as the method scl would change the contents of normal!
			temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d1 - dist)/2
			fruit1.getPosition().add(temp);

			temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d2 - dist)/2
			fruit2.getPosition().sub(temp);

			// Now it is time for Newton's Law of Impact.
			// Convert the two velocities into a single reference frame
			velocity.set(fruit1.getVelocity()).sub(fruit2.getVelocity()); // v1-v2

			// Compute the impulse (see Essential Math for Game Programmers)
			float impulse = (-(1 + COLLISION_COEFF) * normal.dot(velocity)) /
							(normal.dot(normal) * (1 / fruit1.getMass() + 1 / fruit2.getMass()));

			// Change velocity of the two ships using this impulse
			temp.set(normal).scl(impulse / fruit1.getMass());
			fruit1.getVelocity().add(temp);

			temp.set(normal).scl(impulse / fruit2.getMass());
			fruit2.getVelocity().sub(temp);
		}
	}

	/**
	 *  Handles collisions between a ship and photon
	 *
	 *  @param character Character in candidate collision
	 *  @param photon1 Photon in candidate collision
	 */
	public void checkForCollision(CharacterModel character, PhotonQueue.Photon photon1) {

		// Calculate the normal of the (possible) point of collision
		normal.set(character.getPosition()).sub(new Vector2(photon1.x, photon1.y));
		float distance = normal.len();
		float impactDistance = (character.getDiameter() + photon1.getDiameter()) / 2f;
		normal.nor();

		// If this normal is too small, there was a collision
		if (distance < impactDistance) {

			// "Roll back" time so that the ships are barely touching (e.g. point of impact).
			// We need to use temp, as the method scl would change the contents of normal!
			temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d1 - dist)/2
			character.getPosition().add(temp);

			temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d2 - dist)/2
			photon1.getPosition().sub(temp);

			// Now it is time for Newton's Law of Impact.
			// Convert the two velocities into a single reference frame
			velocity.set(character.getVelocity()).sub(photon1.getVelocity()); // v1-v2

			// Compute the impulse (see Essential Math for Game Programmers)
			float impulse = (-(1 + COLLISION_COEFF) * normal.dot(velocity)) /
					(normal.dot(normal) * (1 / character.getMass() + 1 / photon1.getMass()));

			// Change velocity of the two ships using this impulse
			temp.set(normal).scl(impulse / character.getMass());
			//character.getVelocity().add(temp);

			temp.set(normal).scl(impulse / photon1.getMass());
			photon1.setVelocity(photon1.getVelocity().mulAdd(photon1.getVelocity(),-2));
			character.takeDamage(photon1.damage);
			photon1.damage = 0;
		}
	}

	/**
	 *  Handles collisions between a ship and photon
	 *
	 *  @param fruit Fruit in candidate collision
	 *  @param skewer Skewer in candidate collision
	 */
	public void checkForCollision(Fruit fruit, Skewer skewer) {
		normal.set(fruit.getPosition()).sub(skewer.getTipPosition());
		float distance = normal.len();
		float impactDistance = (fruit.getDiameter() + skewer.getDiameter()) / 2f;
		normal.nor();

		// If this normal is too small, there was a collision
		if (distance < impactDistance) {
			// "Roll back" time so that the ships are barely touching (e.g. point of impact).
			// We need to use temp, as the method scl would change the contents of normal!
			temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d1 - dist)/2
			fruit.getPosition().add(temp);

			temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d2 - dist)/2
			skewer.getTipPosition().sub(temp);

			// Now it is time for Newton's Law of Impact.
			// Convert the two velocities into a single reference frame
			velocity.set(fruit.getVelocity()).sub(skewer.getVelocity()); // v1-v2

			// Compute the impulse (see Essential Math for Game Programmers)
//			float impulse = (-(1 + COLLISION_COEFF) * normal.dot(velocity)) /
//					(normal.dot(normal) * (1 / fruit.getMass() + 1 / skewer.getMass()));

			// Change velocity of the two ships using this impulse
//			temp.set(normal).scl(impulse / fruit.getMass());
//			fruit.getVelocity().add(temp);

//			temp.set(normal).scl(impulse / skewer.getMass());
//			skewer.setVelocity(skewer.getVelocity().mulAdd(skewer.getVelocity(), -2));
			fruit.takeDamage(skewer.damage);
			skewer.damage = 0;
		}
	}

	/**
	 *  Handles collisions between a ship and photon
	 *
	 *  @param fruit Fruit in candidate collision
	 *  @param skewer Skewer in candidate collision
	 */
	public void checkForStunCollision(Fruit fruit, Skewer skewer) {
		normal.set(fruit.getPosition()).sub(skewer.getPunchPosition());
		float distance = normal.len();
		float impactDistance = (fruit.getDiameter() + 2*skewer.getDiameter()) / 2f;
		normal.nor();

		// If this normal is too small, there was a collision
		if (distance < impactDistance) {
			// "Roll back" time so that the ships are barely touching (e.g. point of impact).
			// We need to use temp, as the method scl would change the contents of normal!
			temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d1 - dist)/2
			fruit.getPosition().add(temp);

			temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d2 - dist)/2
			skewer.getPunchPosition().sub(temp);

			// Now it is time for Newton's Law of Impact.
			// Convert the two velocities into a single reference frame
			velocity.set(fruit.getVelocity()).sub(skewer.getVelocity()); // v1-v2

			// Compute the impulse (see Essential Math for Game Programmers)
			float impulse = (-(1 + COLLISION_COEFF) * normal.dot(velocity)) /
					(normal.dot(normal) * (1 / fruit.getMass() + 1 / skewer.getMass()));

			// Change velocity of the two ships using this impulse
			temp.set(normal).scl(impulse / fruit.getMass());
			fruit.getVelocity().add(temp);

//			temp.set(normal).scl(impulse / skewer.getMass());
//			skewer.setVelocity(skewer.getVelocity().mulAdd(skewer.getVelocity(), -2));
//			fruit.takeDamage(skewer.damage);
//			skewer.damage = 0;
			fruit.isStunned = true;
			fruit.stunTimer = STUN_TIME;
		}
	}

	public void checkForBounds(CharacterModel character, int boundX, int boundY) {
		if (character.getPosition().x <= -boundX) {
			character.getPosition().x = -boundX + 1f;
		} else if (character.getPosition().x >= boundX) {
			character.getPosition().x = boundX - 1f;
		}

		if (character.getPosition().y <= -boundY) {
			character.getPosition().y = -boundY + 1f;
		} else if (character.getPosition().y >= boundY) {
			character.getPosition().y = boundY - 1f;
		}
	}
}
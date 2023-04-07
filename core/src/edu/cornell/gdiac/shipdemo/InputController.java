/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameMode does not have to keep track of the current
 * key mapping.
 *
 * This class is NOT a singleton. Each input device is its own instance,
 * and you may have multiple input devices attached to the game.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.*;
import edu.cornell.gdiac.util.*;
import com.badlogic.gdx.controllers.Controller;

/**
 * Device-independent input manager.
 *
 * This class supports both a keyboard and an X-Box controller.  Each player is
 * assigned an ID.  When the class is created, we check to see if there is a 
 * controller for that ID.  If so, we use the controller.  Otherwise, we default
 * the the keyboard.
 */
public class InputController {
	
    /** Player id, to identify which keys map to this player */
	protected int player;

    /** X-Box controller associated with this player (if any) */
	protected XBoxController xbox;

	/** How much up are we going? */
	private float up;
	
	/** How much right are we going? */
	private float right;
	/** Angle to point to */
	private float angle;
	
	/** Did we press the fire button? */
	private boolean pressedFire;
	/** Did we press the fire button? */
	private boolean pressedPunch;

	/** How to keep accelerating in a mouse context */
	private float mouseThrottle;
	/** Did we press to boost? */
	private boolean pressedBoost;
	/** Did we press switch skewer button */
	private boolean pressedSwitch;
	private boolean pressedRestart;
	
	/** 
	 * Returns the amount of upward movement.
	 * 
	 * Positive is upward acceleration, negative is downward.
	 *  
	 * @return amount of upward movement.
	 */
	public float getUp() {
		return up;
	}

	/**
	 * Returns the amount of rightward movement.
	 *
	 * Positive is rightward acceleration, negative is leftward.
	 *
	 * @return amount of rightward movement.
	 */
	public float getRight() {
		return right;
	}

	/**
	 * Returns the angle to point to.
	 *
	 * @return angle.
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * Returns whether the switch button was pressed.
	 *
	 * @return whether the switch button was pressed.
	 */
	public boolean didPressSwitch() {
		return pressedSwitch;
	}

	/**
	 * Returns whether the fire button was pressed.
	 * 
	 * @return whether the fire button was pressed.
	 */
	public boolean didPressFire() {
		return pressedFire;
	}

	/**
	 * Returns whether the fire button was pressed.
	 *
	 * @return whether the fire button was pressed.
	 */
	public boolean didPressPunch() {
		return pressedPunch;
	}

	/**
	 * Returns whether the boost button was pressed.
	 *
	 * @return whether the boost button was pressed.
	 */
	public boolean didPressBoost() {
		return pressedBoost;
	}

	public boolean didPressRestart() {
		return pressedRestart;
	}


	/**
	 * Creates a new input controller for the specified player.
	 * 
	 * The game supports two players working against each other in hot seat mode. 
	 * We need a separate input controller for each player. In keyboard, this is 
	 * WASD vs. Arrow keys.  We also support multiple X-Box game controllers.
	 * 
	 * @param id Player id number (0..4)
	 */
	public InputController(int id) {
		player = id;
		
		// If we have a game-pad for id, then use it.
		Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
		if (controllers.size > id) {
			xbox = controllers.get(id);
		} else {
			xbox = null;
		}
	}

	/**
	 * Reads the input for this player and converts the result into game logic.
	 *
	 * This is an example of polling input.  Instead of registering a listener,
	 * we ask the controller about its current state.  When the game is running,
	 * it is typically best to poll input instead of using listeners.  Listeners
	 * are more appropriate for menus and buttons (like the loading screen). 
	 */
	public void readInput() {
		// If there is a game-pad, then use it.
		if (xbox != null) {
			System.err.println("Xbox controller unsupported");
			pressedFire = xbox.getRightTrigger() > 0.6f;

			pressedBoost = xbox.getLeftTrigger() > 0.6f;
		} else {
            // Figure out, based on which player we are, which keys
			// control our actions (depends on player).
            int up, left, right, down, shoot, boost, restart = Input.Keys.R;
			if (player == 0) {
                up    = Input.Keys.UP; 
                down  = Input.Keys.DOWN;
                left  = Input.Keys.LEFT; 
                right = Input.Keys.RIGHT;
                shoot = Input.Keys.SPACE;
				boost = Input.Keys.SHIFT_RIGHT;
			} else {
                up    = Input.Keys.W; 
                down  = Input.Keys.S;
                left  = Input.Keys.A; 
                right = Input.Keys.D;
                shoot = Input.Keys.SPACE;
				boost = Input.Keys.SHIFT_LEFT;
            }
			
            // Convert keyboard state into game commands
            pressedFire = false;
			pressedPunch = false;

			this.right = this.up = 0;

            // Movement forward/backward
			if (Gdx.input.isKeyPressed(up) && !Gdx.input.isKeyPressed(down)) {
                this.right = 1;
			} else if (Gdx.input.isKeyPressed(down) && !Gdx.input.isKeyPressed(up)) {
                this.right = -1;
			}
			
            // Movement left/right
			if (Gdx.input.isKeyPressed(left) && !Gdx.input.isKeyPressed(right)) {
                this.up = -1;
			} else if (Gdx.input.isKeyPressed(right) && !Gdx.input.isKeyPressed(left)) {
                this.up = 1;
			}

            // Shooting
			if (Gdx.input.isKeyPressed(shoot)) {
                pressedFire = true;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.F)) {
				pressedPunch = true;
			}

			// Boosting
			pressedBoost = Gdx.input.isKeyJustPressed(boost);

			angle = (float)Math.atan2(Gdx.input.getY() - 360, Gdx.input.getX() - 640);


			if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
				pressedFire = true;
			}

			pressedSwitch = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);

			this.pressedRestart = Gdx.input.isKeyPressed(restart);


		}
    }
}
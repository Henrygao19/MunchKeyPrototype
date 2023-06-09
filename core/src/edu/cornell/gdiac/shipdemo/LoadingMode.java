/*
 * LoadingMode.java
 *
 * Asset loading is a really tricky problem.  If you have a lot of sound or images,
 * it can take a long time to decompress them and load them into memory.  If you just
 * have code at the start to load all your assets, your game will look like it is hung
 * at the start.
 *
 * The alternative is asynchronous asset loading.  In asynchronous loading, you load a
 * little bit of the assets at a time, but still animate the game while you are loading.
 * This way the player knows the game is not hung, even though he or she cannot do 
 * anything until loading is complete. You know those loading screens with the inane tips 
 * that want to be helpful?  That is asynchronous loading.  
 *
 * This player mode provides a basic loading screen.  While you could adapt it for
 * between level loading, it is currently designed for loading all assets at the 
 * start of the game.
 */

package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;

import edu.cornell.gdiac.assets.*;
import edu.cornell.gdiac.util.*;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You DO NOT need to understand this class for the first lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * This version of loading mode uses what is known as an "asset directory".  This is
 * a JSON file that specifies all of the locations of the assets and their types.  You
 * can then access the asset by its JSON key. This greatly reduces the use of magic
 * numbers in your file. In previous years, we did a lot of asset loading manually,
 * and the code was particularly ugly.
 */
public class LoadingMode implements ModeController, InputProcessor, ControllerListener {
	// There are TWO asset managers.  One to load the loading screen.  The other to load the assets
	/** Internal assets for this loading screen */
	private AssetDirectory internal;
	/** The actual assets to be loaded */
	private AssetDirectory assets;

	// Textures necessary to support the loading screen
	/** Background texture for start-up */
	private Texture background;
	/** Play button to display when done */
	private Texture playButton;
	/** Texture atlas to support a progress bar */
	private Texture statusBar;
	
	// statusBar is a "texture atlas." Break it up into parts.
	/** Left cap to the status background (grey region) */
	private TextureRegion statusBkgLeft;
	/** Middle portion of the status background (grey region) */
	private TextureRegion statusBkgMiddle;
	/** Right cap to the status background (grey region) */
	private TextureRegion statusBkgRight;
	/** Left cap to the status forground (colored region) */
	private TextureRegion statusFrgLeft;
	/** Middle portion of the status forground (colored region) */
	private TextureRegion statusFrgMiddle;
	/** Right cap to the status forground (colored region) */
	private TextureRegion statusFrgRight;	

	/** Default budget for asset loader (do nothing but load 60 fps) */
	private static int DEFAULT_BUDGET = 15;

	// Asset Dimensions
	/** Standard width that the assets were designed for */
	private static int STANDARD_WIDTH  = 1280;
	/** Standard height that the assets were designed for */
	private static int STANDARD_HEIGHT = 720;
	/** Width of the screen to be used for the loading bar */
	private static float BAR_WIDTH_RATIO  = 0.5f;
	/** Ratio from the bottom for the height */
	private static float BAR_HEIGHT_RATIO = 0.18f;	
	/** Additional scaling factor for play button */
	private static float PLAY_BUTTON_RATIO = 0.75f;

	/** The width of the progress bar */
	private int width;
	/** The y-coordinate of the center of the progress bar */
	private int centerY;
	/** The x-coordinate of the center of the progress bar */
	private int centerX;
	/** The height of the canvas window (necessary since sprite origin != screen origin) */
	private int heightY;
	/** Scaling factor for when the student changes the resolution. */
	private float scale;
	
	/** Current progress (0 to 1) of the asset manager */
	private float progress;
	/** The current state of the play button */
	private int   pressState;
	/** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
	private int   budget;

	/**
	 * Returns the budget for the asset loader.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @return the budget in milliseconds
	 */
	public int getBudget() {
		return budget;
	}

	/**
	 * Sets the budget for the asset loader.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param millis the budget in milliseconds
	 */
	public void setBudget(int millis) {
		budget = millis;
	}
	
	/**
	 * Returns true if all assets are loaded and the player is ready to go.
	 *
	 * @return true if the player is ready to go
	 */
	public boolean isReady() {
		return pressState == 2;
	}

	/**
	 * Returns the asset directory produced by this loading screen
	 *
	 * This asset loader is NOT owned by this loading scene, so it persists even
	 * after the scene is disposed.  It is your responsbility to unload the
	 * assets in this directory.
	 *
	 * @return the asset directory produced by this loading screen
	 */
	public AssetDirectory getAssets() {
		return assets;
	}
	
	/**
	 * Creates a LoadingMode with the default budget, size and position.
	 *
	 * @param file  	The asset directory to load in the background
	 */
	public LoadingMode(String file) {
		this(file,DEFAULT_BUDGET);
	}

	/**
	 * Creates a LoadingMode with the default size and position.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param file  	The asset directory to load in the background
	 * @param millis 	The loading budget in milliseconds
	 */
	public LoadingMode(String file, int millis) {
		budget = millis;

		// Waiting on these values until we see the canvas
		width = -1;
		centerY = -1;
		centerX = -1;
		heightY = -1;
		scale = -1.0f;

		// We need these files loaded immediately
		internal = new AssetDirectory( "loading.json" );
		internal.loadAssets();
		internal.finishLoading();

		// Load the next two images immediately.
		playButton = null;
		background = internal.getEntry( "background", Texture.class );
		background.setFilter( TextureFilter.Linear, TextureFilter.Linear );
		statusBar = internal.getEntry( "progress", Texture.class );

		// Break up the status bar texture into regions
		statusBkgLeft = internal.getEntry( "progress.backleft", TextureRegion.class );
		statusBkgRight = internal.getEntry( "progress.backright", TextureRegion.class );
		statusBkgMiddle = internal.getEntry( "progress.background", TextureRegion.class );

		statusFrgLeft = internal.getEntry( "progress.foreleft", TextureRegion.class );
		statusFrgRight = internal.getEntry( "progress.foreright", TextureRegion.class );
		statusFrgMiddle = internal.getEntry( "progress.foreground", TextureRegion.class );

		// No progress so far.
		progress = 0;
		pressState = 0;

		Gdx.input.setInputProcessor( this );

		// Let ANY connected controller start the game.
		for (XBoxController controller : Controllers.get().getXBoxControllers()) {
			controller.addListener( this );
		}

		// Start loading the real assets
		assets = new AssetDirectory( file );
		assets.loadAssets();
	}
	
	/** 
	 * Read user input, calculate physics, and update the models.
	 *
	 * This method is HALF of the basic game loop.  Every graphics frame 
	 * calls the method update() and the method draw().  The method update()
	 * contains all of the calculations for updating the world, such as
	 * checking for collisions, gathering input, and playing audio.  It
	 * should not contain any calls for drawing to the screen.
	 *
	 * In the case of the loading screen, all this does is load assets.
	 */
	@Override
	public void update() {
		if (playButton == null) {
			assets.update(budget);
			this.progress = assets.getProgress();
			if (progress >= 1.0f) {
				this.progress = 1.0f;
				playButton = internal.getEntry("play",Texture.class);
			}
		}
	}
	
	/**
	 * Draw the game on the provided GameCanvas
	 *
	 * There should be no code in this method that alters the game state.  All 
	 * assignments should be to local variables or cache fields only.
	 *
	 * In the case of the loading screen, this draws the progress bar.
	 *
	 * @param canvas The drawing context
	 */
	@Override
	public void draw(GameCanvas canvas) {
		// If this is the first time drawing, get info from the canvas.
		if (width == -1) {
			resize(canvas.getWidth(),canvas.getHeight());
		}
		
		canvas.drawOverlay(background, true);
		if (playButton == null) {
			drawProgress(canvas);
		} else {
			Color tint = (pressState == 1 ? Color.GRAY: Color.WHITE);
			canvas.draw(playButton, tint, playButton.getWidth()/2, playButton.getHeight()/2, 
						centerX, centerY, 0, scale*PLAY_BUTTON_RATIO, scale*PLAY_BUTTON_RATIO);
		}
	}
	
	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 *
	 * As our textures are not static this time, we actually have to do something.
	 */
	@Override
	public void dispose() {
		internal.unloadAssets();
		internal.dispose();
	}
	
	/**
	 * Updates the progress bar according to loading progress
	 *
	 * The progress bar is composed of parts: two rounded caps on the end, 
	 * and a rectangle in a middle.  We adjust the size of the rectangle in
	 * the middle to represent the amount of progress.
	 *
	 * @param canvas The drawing context
	 */	
	private void drawProgress(GameCanvas canvas) {	
		// In practice I would do this with affine transforms, but that would make Exercise 4 harder.
		canvas.drawProgress( false, statusFrgRight, statusFrgMiddle,
				 statusFrgLeft,  statusBkgRight,
				statusBkgMiddle, statusBkgLeft, centerX, centerY, width, scale, progress);
//		System.out.format("width: %d , scale: %f \n", width, scale);
	}
	
	/**
	 * Resize the window for this player mode to the given dimensions.
	 *
	 * This method is unsupported in the loading screen.
	 *
	 * @param width The width of the game window
	 * @param height The height of the game window
	 */
	public void resize(int width, int height) {
		// Compute the drawing scale
		float sx = ((float)width)/STANDARD_WIDTH;
		float sy = ((float)height)/STANDARD_HEIGHT;
		scale = (sx < sy ? sx : sy);
		
		this.width = (int)(BAR_WIDTH_RATIO*width);
		centerY = (int)(BAR_HEIGHT_RATIO*height);
		centerX = width/2;
		heightY = height;
	}

	// PROCESSING PLAYER INPUT
	/** 
	 * Called when the screen was touched or a mouse button was pressed.
	 *
	 * This method checks to see if the play button is available and if the click
	 * is in the bounds of the play button.  If so, it signals the that the button
	 * has been pressed and is currently down. Any mouse button is accepted.
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param pointer the button or touch finger number
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (playButton == null || pressState == 2) {
			return true;
		}
		
		// Flip to match graphics coordinates
		screenY = heightY-screenY;
		
		// Play button is a circle.
		float radius = scale*playButton.getWidth()/2.0f;
		float dist = (screenX-centerX)*(screenX-centerX)+(screenY-centerY)*(screenY-centerY);
		if (dist < radius*radius) {
			pressState = 1;
		}
		return false;
	}
	
	/** 
	 * Called when a finger was lifted or a mouse button was released.
	 *
	 * This method checks to see if the play button is currently pressed down. If so, 
	 * it signals the that the player is ready to go.
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param pointer the button or touch finger number
	 * @return whether to hand the event to other listeners. 
	 */	
	public boolean touchUp(int screenX, int screenY, int pointer, int button) { 
		if (pressState == 1) {
			pressState = 2;
			return false;
		}
		return true;
	}
	
	/** 
	 * Called when a button on the Controller was pressed. 
	 *
	 * The buttonCode is controller specific. This listener only supports the start
	 * button on an X-Box controller.  This outcome of this method is identical to 
	 * pressing (but not releasing) the play button.
	 *
	 * @param controller The game controller
	 * @param buttonCode The button pressed
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean buttonDown (Controller controller, int buttonCode) {
		if (pressState == 0) {
			ControllerMapping mapping = controller.getMapping();
			if (mapping != null && buttonCode == mapping.buttonStart ) {
				pressState = 1;
				return false;
			}
		}
		return true;
	}
	
	/** 
	 * Called when a button on the Controller was released. 
	 *
	 * The buttonCode is controller specific. This listener only supports the start
	 * button on an X-Box controller.  This outcome of this method is identical to 
	 * releasing the the play button after pressing it.
	 *
	 * @param controller The game controller
	 * @param buttonCode The button pressed
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean buttonUp (Controller controller, int buttonCode) {
		if (pressState == 1) {
			ControllerMapping mapping = controller.getMapping();
			if (mapping != null && buttonCode == mapping.buttonStart ) {
				pressState = 2;
				return false;
			}
		}
		return true;
	}
	
	// UNSUPPORTED METHODS FROM InputProcessor

	/** 
	 * Called when a key is pressed (UNSUPPORTED)
	 *
	 * @param keycode the key pressed
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean keyDown(int keycode) { 
		return true; 
	}

	/** 
	 * Called when a key is typed (UNSUPPORTED)
	 *
	 * @param character	the key typed
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean keyTyped(char character) { 
		return true; 
	}

	/** 
	 * Called when a key is released (UNSUPPORTED)
	 *
	 * @param keycode the key released
	 * @return whether to hand the event to other listeners. 
	 */	
	public boolean keyUp(int keycode) { 
		return true; 
	}
	
	/** 
	 * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @return whether to hand the event to other listeners. 
	 */	
	public boolean mouseMoved(int screenX, int screenY) { 
		return true; 
	}

	/** 
	 * Called when the mouse wheel was scrolled. (UNSUPPORTED)
	 *
	 * @param dx the amount of horizontal scroll
	 * @param dy the amount of vertical scroll
	 *
	 * @return whether to hand the event to other listeners.
	 */	
	public boolean scrolled(float dx, float dy) {
		return true; 
	}

	/** 
	 * Called when the mouse or finger was dragged. (UNSUPPORTED)
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param pointer the button or touch finger number
	 * @return whether to hand the event to other listeners. 
	 */		
	public boolean touchDragged(int screenX, int screenY, int pointer) { 
		return true; 
	}
	
	// UNSUPPORTED METHODS FROM ControllerListener
	
	/**
	 * Called when a controller is connected. (UNSUPPORTED)
	 *
	 * @param controller The game controller
	 */
	public void connected (Controller controller) {}

	/**
	 * Called when a controller is disconnected. (UNSUPPORTED)
	 *
	 * @param controller The game controller
	 */
	public void disconnected (Controller controller) {}

	/** 
	 * Called when an axis on the Controller moved. (UNSUPPORTED) 
	 *
	 * The axisCode is controller specific. The axis value is in the range [-1, 1]. 
	 *
	 * @param controller The game controller
	 * @param axisCode 	The axis moved
	 * @param value 	The axis value, -1 to 1
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean axisMoved (Controller controller, int axisCode, float value) {
		return true;
	}
	
}
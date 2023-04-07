/*
 * GameMode.java
 *
 * This is the primary class file for running the game.  You should study this file for
 * ideas on how to structure your own root class. This class follows a 
 * model-view-controller pattern fairly strictly.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.shipdemo.ai.AIController;
import edu.cornell.gdiac.shipdemo.ai.AIFactory;
import edu.cornell.gdiac.util.FilmStrip;

import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;

import java.util.*;

/**
 * The primary controller class for the game.
 *
 * While GDXRoot is the root class, it delegates all of the work to the player mode
 * classes. This is the player mode class for running the game. In initializes all 
 * of the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode implements ModeController {
	//New fields relevant for Gameplay prototype below:
	/** Order Controller */
	OrderController orderController;
	/** Maps ID to enemy ship*/
	Map<Integer,Fruit> fruits = new HashMap<>();
	/** Maps ID to AI controller*/
	Map<Integer, AIController> enemyControllers = new HashMap<>();
	/** Internal structure for managing deletions*/
	List<Integer> toDelete = new ArrayList<>();

	/** Counter for assigning ID to ships*/
	int spawnCounter = 1;

	/** X (horizontal) boundary*/
	public static final int BOUND_X = 1280;
	/** Y (vertical) boundary*/
	public static final int BOUND_Y = 1600;



	/** Number of rows in the ship image filmstrip */
	private static final int SHIP_ROWS = 4;
	/** Number of columns in this ship image filmstrip */
	private static final int SHIP_COLS = 5;
	/** Number of elements in this ship image filmstrip */
	private static final int SHIP_SIZE = 18;
	/** Left portion of the status background (grey region) */
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
	/** The background image for the battle */
	private Texture background;
	private Texture background2;
	private Texture borderLeft;
	private Texture borderRight;
	private Texture borderTop;
	private Texture borderBottom;
	private Texture borderBl;
	private Texture borderBr;
	private Texture borderTl;
	private Texture borderTr;


	/** The image for a single apple skewer */
	private Texture appleSkTexture;
	/** The image for a single strawberry skewer*/
	private Texture strawberrySkTexture;
	/** The image for a single orange skewer*/
	private Texture orangeSkTexture;

	/** The image for a single apple */
	private Texture appleTexture;
	/** The image for a single orange */
	private Texture orangeTexture;
	/** The image for a single strawberry */
	private Texture strawberryTexture;
	/** The image for a empty skewer*/
	private Texture emptySkTexture;
	/** The image for a single proton */
	private Texture photonTexture;
	/** Texture for the ship (colored for each player) */
	private Texture shipTexture;
	/** Texture for the target reticule */
	private Texture targetTexture;
	/** Texture for the heart */
	private Texture heartTexture;
	/** Texture for the monkey */
	private Texture monkeyTexture;
	/** Texture for the monkey */
	private Texture monkeySpriteTexture;
	/** Texture for the skewer */
	private Texture skewerTexture;
	/** Texture for the skewer */
	private Texture punchTexture;
	/** Game over picture */
	private Texture gameOverTexture;
	/** The weapon fire sound for the blue player */
	private Sound blueSound;
	/** The weapon fire sound for the red player */
	private Sound redSound;
	
    // Instance variables
	/** Read input for blue player from keyboard or game pad (CONTROLLER CLASS) */
	protected InputController blueController;
	/** Read input for red player from keyboard or game pad (CONTROLLER CLASS) */
	protected InputController redController;
    /** Handle collision and physics (CONTROLLER CLASS) */
    protected CollisionController physicsController;

	/** Player ship */
	public Monkey playerMonkey;
	/** Shared memory pool for photons. (MODEL CLASS) */
	protected PhotonQueue photons;

	/** Store the bounds to enforce the playing region */	
	private Rectangle bounds;

	/** The number of frames that have elapsed */
	private int frameCount;
	/** Is game over? */
	private boolean isGameOver;
	private boolean isRestart;

	/** The font for giving messages to the player */
	private BitmapFont displayFont;


	/**
	 * Creates a new game with a playing field of the given size.
	 *
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 *
	 * @param width 	The width of the game window
	 * @param height 	The height of the game window
	 * @param assets	The asset directory containing all the loaded assets
	 */
	public GameMode(float width, float height, AssetDirectory assets) {
//		displayFont = assets.getEntry("times",BitmapFont.class);
		// Extract the assets from the asset directory.  All images are textures.
		background = assets.getEntry("background", Texture.class );
		background2 = assets.getEntry("background2", Texture.class );
		borderLeft = assets.getEntry("borderleft", Texture.class );
		borderRight = assets.getEntry("borderright", Texture.class );
		borderTop = assets.getEntry("bordertop", Texture.class );
		borderBottom = assets.getEntry("borderbottom", Texture.class );
		borderBl = assets.getEntry("borderbl", Texture.class );
		borderBr = assets.getEntry("borderbr", Texture.class );
		borderTl = assets.getEntry("bordertl", Texture.class );
		borderTr = assets.getEntry("bordertr", Texture.class );

		shipTexture = assets.getEntry( "ship", Texture.class );
		monkeySpriteTexture = assets.getEntry( "monkeySprite", Texture.class );
		targetTexture = assets.getEntry( "target", Texture.class );
		heartTexture = assets.getEntry("heart", Texture.class);
		monkeyTexture = assets.getEntry("monkey", Texture.class);
		photonTexture = assets.getEntry( "photon", Texture.class );
		skewerTexture = assets.getEntry("skewer", Texture.class);
		punchTexture = assets.getEntry("glove", Texture.class);
		gameOverTexture = assets.getEntry("gameOver", Texture.class);
		strawberryTexture =  assets.getEntry("strawberry", Texture.class);
		appleTexture =  assets.getEntry("apple", Texture.class);
		orangeTexture =  assets.getEntry("orange", Texture.class);
		statusBkgLeft = assets.getEntry( "progress.backleft", TextureRegion.class );
		statusBkgRight = assets.getEntry( "progress.backright", TextureRegion.class );
		statusBkgMiddle = assets.getEntry( "progress.background", TextureRegion.class );

		statusFrgLeft = assets.getEntry( "progress.foreleft", TextureRegion.class );
		statusFrgRight = assets.getEntry( "progress.foreright", TextureRegion.class );
		statusFrgMiddle = assets.getEntry( "progress.foreground", TextureRegion.class );

		strawberrySkTexture=  assets.getEntry("strawberrySkewer", Texture.class);
		appleSkTexture=  assets.getEntry("appleSkewer", Texture.class);
		orangeSkTexture=  assets.getEntry("orangeSkewer", Texture.class);
		emptySkTexture=  assets.getEntry("emptySkewer", Texture.class);

		// Initialize the photons.
		photons = new PhotonQueue();
		photons.setTexture(photonTexture);
		bounds = new Rectangle(0,0,width,height);

		// Load the sounds.  We need to use the subclass SoundBuffer because of our changes to audio.
		blueSound = assets.getEntry( "laser",  SoundEffect.class);
		redSound  = assets.getEntry( "fusion", SoundEffect.class);

        // Player ship
		playerMonkey = new Monkey(0, 0, 0, 1.5f);

		playerMonkey.setPunchTexture(punchTexture);

		playerMonkey.setSkewerTexture(skewerTexture,strawberrySkTexture, appleSkTexture, orangeSkTexture, emptySkTexture);


		playerMonkey.setHeartTexture(heartTexture);
		playerMonkey.setMonkeyTexture(monkeyTexture);
		playerMonkey.setFilmStrip(new FilmStrip(monkeySpriteTexture,2,5,8));

		// Create the input controllers.
		redController  = new InputController(1);
		blueController = new InputController(0);
		physicsController = new CollisionController();
		orderController = new OrderController(this);

		orderController.loadFilm(new FilmStrip(shipTexture,SHIP_ROWS,SHIP_COLS,SHIP_SIZE));
		orderController.setEnemyTexture(strawberrySkTexture, appleSkTexture, orangeSkTexture, emptySkTexture);
		orderController.setSkewerTexture(skewerTexture);
		orderController.setLoadingTexture(statusBkgLeft, statusBkgRight, statusBkgMiddle, statusFrgLeft, statusFrgRight, statusFrgMiddle);

	}

	/** 
	 * Read user input, calculate physics, and update the models.
	 *
	 * This method is HALF of the basic game loop.  Every graphics frame 
	 * calls the method update() and the method draw().  The method update()
	 * contains all of the calculations for updating the world, such as
	 * checking for collisions, gathering input, and playing audio.  It
	 * should not contain any calls for drawing to the screen.
	 */
	@Override
	public void update() {
		// Read the keyboard for each controller.
		redController.readInput ();
		blueController.readInput ();

		isRestart = redController.didPressRestart() && isGameOver;
		if(isGameOver) return;

		frameCount++;

		orderController.update(frameCount);

		
		// Move the photons forward, and add new ones if necessary.
		//photons.move (width,height);

//		if (redController.didPressFire() && firePhoton(playerMonkey,photons)) {
//            blueSound.play();
//		}

		if (redController.didPressFire() && skewerAttack(playerMonkey)) {
//			blueSound.play();
		}

		if (redController.didPressPunch() && punch(playerMonkey)) {
//			blueSound.play();
		}

		if(redController.didPressSwitch()) {
			orderController.switchSkewer();
		}

		// Move the ships forward (ignoring collisions)
		playerMonkey.move(redController.getUp(),   redController.getRight(), redController.getAngle());
		photons.move(bounds);

		// Process ship boosts
		if(playerMonkey.canBoost() && redController.didPressBoost()) {
			playerMonkey.boost();
		}

		for(Fruit s : getFruits()) {
			getAIController(s.getId()).update(frameCount);
			if(getAIController(s.getId()).didPressFire() && firePhoton(s, photons)) {
//				redSound.play();
			}
		}
		
		// Change the target position.
		//shipRed.acquireTarget(shipBlue);
		//shipBlue.acquireTarget(shipRed);
		
		// Handles ship-to-ship and photon collisions; QUADRATIC WARNING!
		for(Fruit s : getFruits()) {
			//photons.shipPhotonCollisions(s, physicsController);
			physicsController.checkForCollision(playerMonkey, s);
			if(!playerMonkey.canFireWeapon()) playerMonkey.skewer.fruitSkewerCollisions(s, physicsController);
			if(!playerMonkey.canPunch()) playerMonkey.skewer.fruitPunchCollisions(s, physicsController);
		}
		for(Fruit s1 : getFruits()) {
			for(Fruit s2 : getFruits()) {
				if(s1 != s2) physicsController.checkForCollision(s1, s2);
			}
		}
		//physicsController.checkInBounds(shipBlue, bounds);
		//physicsController.checkInBounds(shipRed, bounds);

		photons.shipPhotonCollisions(playerMonkey, physicsController);

		//photons.shipPhotonCollisions(shipBlue, physicsController);

		for(Fruit s : getFruits()) {
			if(s.isDead()) {
				orderController.acceptIngredient(s.getType());
				deleteShip(s.getId());
			}
			physicsController.checkForBounds(s, BOUND_X, BOUND_Y);
		}

		physicsController.checkForBounds(playerMonkey, BOUND_X, BOUND_Y);

		for(int id : toDelete) {
			fruits.remove(id);
			enemyControllers.remove(id);
		}
		toDelete.clear();

		doSpawns();

		if(playerMonkey.isDead()) {
			gameOver();
		}
	}

	/**
	 * Called when the player dies
	 */
	public void gameOver() {
		isGameOver = true;
	}

	/**
	 * Get enemy ship of ID
	 *
	 * Will throw exception if ship of given ID doesn't exist
	 *
	 * @param id ID of ship
	 * @return ship of id
	 */
	public Fruit getShip(int id) {
		return fruits.get(id);
	}

	/**
	 * Get enemy AI Controller of ID
	 *
	 * Will throw exception if ship of given ID doesn't exist
	 *
	 * @param id ID of controller
	 * @return AI Controller of id
	 */
	public AIController getAIController(int id) {
		return enemyControllers.get(id);
	}

	/**
	 * Collection of enemy ships
	 *
	 * Useful for for-each loops
	 *
	 * @return Collection of enemy ships
	 */
	public Collection<Fruit> getFruits() {
		return fruits.values();
	}

	/**
	 * Delete the ship and its associated AI Controller
	 *
	 * Note that deletions are carried through only at the end of each tick
	 *
	 * @param id of ship to delete
	 */
	public void deleteShip(int id) {
		toDelete.add(id);
	}

	/**
	 * This method spawns enemy ships.
	 *
	 * Called every frame.
	 */
	public void doSpawns() {

		//Don't spawn ships if there are already 25 ships on screen
		if(fruits.size() > 25){
			return;
		}

		//Generate initial location of enemies
		Random rand = new Random();
		float radius = bounds.width/2 + rand.nextInt(800);
		double theta = 2 * Math.PI *  Math.random();
		float x =  (float) (radius * Math.cos(theta));
		float y = (float) (radius * Math.sin(theta));


		//Set the type of the enemy
		FruitType type = FruitType.STRAWBERRY;
		switch (rand.nextInt(3)){
			case 0:
				type = FruitType.ORANGE;
				break;
			case 1:
				type = FruitType.STRAWBERRY;
				break;
			case 2:
				type = FruitType.APPLE;
				break;
		}



		spawn(x, y, type);
	}

	/**
	 * Spawn a new ship
	 *
	 * This method is where instances of AIController are instantiated
	 *
	 * @param relativeX x position of spawned ship relative to player x position
	 * @param relativeY y position of spawned ship relative to player y position
	 * @param type color of spawned ship (must be red, green, or blue)
	 * @return ID of newly spawned ship
	 */
	public int spawn(float relativeX, float relativeY, FruitType type) {


		Fruit ship;

		switch(type){
			case APPLE:
				ship = new Apple(playerMonkey.getPosition().x + relativeX, playerMonkey.getPosition().y + relativeY,
						0, 1, spawnCounter);
				ship.setTexture(appleTexture);
				break;
			case ORANGE:
				ship = new Orange(playerMonkey.getPosition().x + relativeX, playerMonkey.getPosition().y + relativeY,
						0, 1, spawnCounter);
				ship.setTexture(orangeTexture);
				break;
			case STRAWBERRY:
				ship = new Strawberry(playerMonkey.getPosition().x + relativeX, playerMonkey.getPosition().y + relativeY,
						0, 1, spawnCounter);
				ship.setTexture(strawberryTexture);
				break;
			default:
				throw new AssertionError();

		}
		ship.setHeartTexture(heartTexture);
		AIController controller = AIFactory.makeAI(ship, this);

		fruits.put(spawnCounter, ship);
		enemyControllers.put(spawnCounter, controller);

		spawnCounter++;
		return ship.getId();

	}


	/**
	 * Draw the game on the provided GameCanvas
	 *
	 * There should be no code in this method that alters the game state.  All 
	 * assignments should be to local variables or cache fields only.
	 *
	 * @param canvas The drawing context
	 */
	@Override
	public void draw(GameCanvas canvas) {
		for(int i = -2; i <= 1; i++) {
			for(int j = -2; j <= 1; j++) {
				int x = (i + (int) playerMonkey.getPosition().x / canvas.getWidth()) * canvas.getWidth();
				int y = (j + (int) playerMonkey.getPosition().y / canvas.getHeight()) * canvas.getHeight();
				if(x >= BOUND_X) {
					if(y >= BOUND_Y) {
						canvas.draw(borderTr, Color.WHITE, 0, 0,
								x,
								y,
								0, 1.334f, 1.334f);
					}
					else {
						if(y < -BOUND_Y) {
							canvas.draw(borderBr, Color.WHITE, 0, 0,
									x,
									y,
									0, 1.334f, 1.334f);
						}
						else {
							canvas.draw(borderRight, Color.WHITE, 0, 0,
									x,
									y,
									0, 1.334f, 1.334f);
						}
					}
				}
				else if (x < -BOUND_X) {
					if(y >= BOUND_Y) {
						canvas.draw(borderTl, Color.WHITE, 0, 0,
								x,
								y,
								0, 1.334f, 1.334f);
					}
					else {
						if(y < -BOUND_Y) {
							canvas.draw(borderBl, Color.WHITE, 0, 0,
									x,
									y,
									0, 1.334f, 1.334f);
						}
						else {
							canvas.draw(borderLeft, Color.WHITE, 0, 0,
									x,
									y,
									0, 1.334f, 1.334f);
						}
					}
				}
				else {
					if(y >= BOUND_Y) {
						canvas.draw(borderTop, Color.WHITE, 0, 0,
								x,
								y,
								0, 1.334f, 1.334f);
					}
					else {
						if(y < -BOUND_Y) {
							canvas.draw(borderBottom, Color.WHITE, 0, 0,
									x,
									y,
									0, 1.334f, 1.334f);
						}
						else {
							canvas.draw(background, Color.WHITE, 0, 0,
									x,
									y,
									0, 1.334f, 1.334f);
						}
					}
				}
			}
		}




		// First drawing pass (ships + shadows)
		// Draw Red and Blue ships
		canvas.setXOffset(-playerMonkey.getPosition().x + canvas.getWidth() / 2f);
		canvas.setYOffset(-playerMonkey.getPosition().y + canvas.getHeight() / 2f);

		playerMonkey.drawMonkey(canvas, orderController);

		for(Fruit s : getFruits()) {
			s.drawShip(canvas);
		}

		// Second drawing pass (photons)
		canvas.setBlendState(GameCanvas.BlendState.ALPHA_BLEND);
		photons.draw(canvas);         // Draw Photons
		canvas.setBlendState(GameCanvas.BlendState.ALPHA_BLEND);

		orderController.draw(canvas);

//		canvas.drawText("message", displayFont, 5f, canvas.getHeight()-5f);
		if(isGameOver) {
			canvas.drawOverlay(gameOverTexture, true);
		}
	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		// Garbage collection here is sufficient.  Nothing to do
	}
	
	/**
	 * Resize the window for this player mode to the given dimensions.
	 *
	 * This method is not guaranteed to be called when the player mode
	 * starts.  If the window size is important to the player mode, then
	 * these values should be passed to the constructor at start.
	 *
	 * @param width The width of the game window
	 * @param height The height of the game window
	 */
	public void resize(int width, int height) {
		bounds.set(0,0,width,height);
	}
	
	/**
 	 * Fires a photon from the ship, adding it to the PhotonQueue.
 	 * 
 	 * This is not inside either PhotonQueue or Ship because it is a relationship
 	 * between to objects.  As we will see in class, we do not want to code binary
 	 * relationships that way (because it increases dependencies).
 	 *
 	 * @param ship  	Ship firing the photon
 	 * @param photons 	PhotonQueue for allocation
 	 */
	private boolean firePhoton(Fruit ship, PhotonQueue photons) {
		// Only process if enough time has passed since last.
		if (ship.canFireWeapon()) {
			photons.addPhoton(ship.getPosition(), ship.getVelocity(), ship.getAngle(), ship.getType());
			ship.reloadWeapon();
//			ship.takeDamage(1);
			return true;
		}
		return false;
	}

	/**
	 * Fires a photon from the ship, adding it to the PhotonQueue.
	 *
	 * This is not inside either PhotonQueue or Ship because it is a relationship
	 * between to objects.  As we will see in class, we do not want to code binary
	 * relationships that way (because it increases dependencies).
	 *
	 * @param ship  	Ship firing the photon
	 * @param photons 	PhotonQueue for allocation
	 */
	private boolean firePhoton(Monkey ship, PhotonQueue photons) {
		// Only process if enough time has passed since last.
		if (ship.canFireWeapon()) {
			photons.addPhoton(ship.getPosition(), ship.getVelocity(), ship.getAngle(), null);
			ship.reloadWeapon();
//			ship.takeDamage(1);
			return true;
		}
		return false;
	}

	/**
	 * Attacks with the skewer
	 *
	 * @param ship  	Ship firing the photon
	 */
	private boolean skewerAttack(Monkey ship) {
		// Only process if enough time has passed since last.
		if (ship.canFireWeapon() && ship.canPunch()) {
			ship.reloadWeapon();
			ship.skewer.damage = 1;
			// make a function that indicates that an attack is starting
			return true;
		}
		return false;
	}

	/**
	 * Attacks with the skewer
	 *
	 * @param ship  	Ship firing the photon
	 */
	private boolean punch(Monkey ship) {
		// Only process if enough time has passed since last.
		if (ship.canPunch() && ship.canFireWeapon()) {
			ship.reloadPunch();
//			ship.skewer.damage = 1;
			// make a function that indicates that an attack is starting
			return true;
		}
		return false;
	}

	public boolean isRestart() {
		return isRestart;
	}
}
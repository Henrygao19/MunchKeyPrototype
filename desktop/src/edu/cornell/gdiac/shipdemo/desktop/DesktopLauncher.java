/*
 * DesktopLauncher.java
 *
 * LibGDX is a cross-platform development library. You write all of your code in
 * the core project.  However, you still need some extra classes if you want to
 * deploy on a specific platform (e.g. PC, Android, Web).  That is the purpose
 * of this class.  It deploys your game on a PC/desktop computer.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/3/21
 */
package edu.cornell.gdiac.shipdemo.desktop;

import lwjgl3.Lwjgl3Application;
import lwjgl3.Lwjgl3ApplicationConfiguration;
import edu.cornell.gdiac.shipdemo.GDXRoot;

/**
 * The main class of the game.
 *
 * This class sets the window size and launches the game.  Aside from modifying
 * the window size, you should almost never need to modify this class.
 */
public class DesktopLauncher {

	/**
	 * Classic main method that all Java programmers know.
	 *
	 * This method simply exists to start a new GDXApp. For desktop games,
	 * LibGDX is built on top of LWJGL (this is not the case for Android).
	 *
	 * @param arg Command line arguments
	 */
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode( 1280, 800 );
		config.setResizable( true );
		config.setTitle("Munchkey Prototype");
		config.setForegroundFPS(60);
		new Lwjgl3Application(new GDXRoot(), config);
	}
}

package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.math.Vector2;

public abstract class CharacterModel {
    public Vector2 pos;
    /*checks whether the player takes damages from being nearby*/
    public boolean isSpiked=false;
    public float mass;
    private int hp;
    private Vector2 vel;

    public Vector2 getPosition(){
        return pos;
    };
    public void setSpiked(boolean spike){
        this.isSpiked=spike;
    }

    public abstract float getDiameter();

    public Vector2 getVelocity() {
        return vel;
    }
    public float getMass() {
        return mass;
    }

    public void takeDamage(int damage) {
        this.hp -= damage;
        this.hp = Math.max(this.hp, 0);
    }
}

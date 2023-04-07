package edu.cornell.gdiac.shipdemo;

public class Strawberry extends Fruit {

    public static final int STRAWBERRY_HP = 3;
    public static final FruitType FRUIT_TYPE = FruitType.STRAWBERRY;

    public Strawberry(float x, float y, float ang, float size, int id) {
        super(x, y, ang, size, id, STRAWBERRY_HP);
    }

    @Override
    public FruitType getType() {
        return FRUIT_TYPE;
    }
}

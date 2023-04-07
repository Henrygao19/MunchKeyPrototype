package edu.cornell.gdiac.shipdemo;

public class Apple extends Fruit {

    public static final int APPLE_HP = 3;
    public static final FruitType FRUIT_TYPE = FruitType.APPLE;

    public Apple(float x, float y, float ang, float size, int id) {
        super(x, y, ang, size, id, APPLE_HP);
    }

    @Override
    public FruitType getType() {
        return FRUIT_TYPE;
    }
}

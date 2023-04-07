package edu.cornell.gdiac.shipdemo;

public class Orange extends Fruit {
    public static final int ORANGE_HP = 5;
    public static final FruitType FRUIT_TYPE = FruitType.ORANGE;


    public Orange(float x, float y, float ang, float size, int id) {
        super(x, y, ang, size, id, ORANGE_HP);
    }

    @Override
    public FruitType getType() {
        return FRUIT_TYPE;
    }
}

package edu.cornell.gdiac.shipdemo;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.FilmStrip;

import java.util.Deque;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashMap;

public class OrderController {
    /**
     * Reference to GameMode
     */
    GameMode gameMode;
    /**
     * Amount to scale the ship size
     */
    private static final float DEFAULT_SCALE = 1.0f;

    /**
     * Texture for the ship (colored for each player)
     */
    private Texture appleTexture;
//    private Texture watermelonTexture;
//    private Texture pineappleTexture;
    private Texture strawberryTexture;
    private Texture orangeTexture;
    private Texture skewerTexture;
    private Texture emptyTexture;

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

    private Skewer skewers[];

    private int completed;
    static private Array<Order> orders;

    /**
     * Texture for the ship (colored for each player)
     */
    private static FilmStrip shipSprite;
    private int currentSkewer;
    private int numSkewers;

    static final int MaxOrders=3;

    private Texture getEnemy(FruitType i) {

        switch (i) {
            case EMPTY:
                return emptyTexture;
            case APPLE:
                return appleTexture;
            case STRAWBERRY:
                return strawberryTexture;
            case ORANGE:
                return orangeTexture;
        }
        return appleTexture;

    }



    public OrderController(GameMode gameMode) {
        this(gameMode, 3);
    }

    public OrderController(GameMode gameMode, int num_skewers) {
        this.gameMode = gameMode;

        this.orders = new Array<Order>();
        this.completed=0;
        currentSkewer = 0;
        this.numSkewers = num_skewers;
        this.skewers =new Skewer[num_skewers];
        for(int i=0; i<num_skewers;i++){
            this.skewers[i] = new Skewer(3);

        }
  
    }

    public void update(int frameCount) {
        if(orders.size<MaxOrders) {
            if (new Random().nextInt(20) == 1) {
                addOrder();
            }
        }
        for(int i=0; i< orders.size; i++){
           if(orders.get(i).update()){
               orders.removeIndex(i);
           };

        }
        completed();
    }

    public void loadFilm(FilmStrip assets) {
        this.shipSprite = assets;
    }

    public void setEnemyTexture(Texture strawberryTexture, Texture appleTexture, Texture orangeTexture, Texture emptyTexture){
        this.strawberryTexture = strawberryTexture;
        this.appleTexture = appleTexture;
        this.orangeTexture = orangeTexture;
        this.emptyTexture = emptyTexture;
    }
    public void setSkewerTexture(Texture skewerTexture){
        this.skewerTexture=skewerTexture;
    }
    public void setLoadingTexture( TextureRegion BL,  TextureRegion BR,  TextureRegion BM, TextureRegion FL, TextureRegion FR, TextureRegion FM){
        this.statusBkgLeft = BL;
        this.statusBkgRight = BR;
        this.statusBkgMiddle =BM;
        this.statusFrgLeft= FL;
        this.statusFrgRight= FR;
        this.statusFrgMiddle= FM;
    }


    /**
     * Draw orders UI
     *
     * @param canvas canvas to draw on (you should probably use drawAbsolute)
     */
    public void draw(GameCanvas canvas) {
        // prints the skewers
//        System.out.println("# of skewers: " + skewers.length);
        if(shipSprite==null){
            return;
        }
        for (int skew = 0; skew < skewers.length; skew++) {
            float skewerscale=1,sizefactor=1;
            if(skew==currentSkewer){
                sizefactor=2;
                skewerscale=1.2f;
            }
            canvas.drawAbsolute(skewerTexture, Color.WHITE, skewerTexture.getWidth()/2, skewerTexture.getHeight()/2,
                    100,canvas.getHeight() - 40 * (1 + skew),270,DEFAULT_SCALE,DEFAULT_SCALE*3.5f*skewerscale);

            for (int i = 0; i < skewers[skew].getQueue().size; i++) {
                Color c = Color.WHITE;
                if(skewers[skew].getFruitAt(i)==FruitType.EMPTY) {
                    c = new Color(0, 0, 0, 1);
                }
                Texture enemy = getEnemy(skewers[skew].getFruitAt(i));
                float size = DEFAULT_SCALE/2;

                canvas.drawAbsolute(enemy, c, enemy.getWidth()/2, enemy.getHeight()/2,
                        60+i * 60, canvas.getHeight() - 40 * (1 + skew), 0,sizefactor*size, sizefactor*size);
            }
        }
            // prints the orders
        for(int o=0; o<Math.min(MaxOrders,orders.size);o++ ){
            orders.get(o).draw(canvas,canvas.getHeight()-60*(o+1)+20);
        }
        for(int i=0; i<completed;i++){
            canvas.drawAbsolute(shipSprite, new Color(.5f,.5f,.5f,1), 0, 0, i * 20, 20, 0,DEFAULT_SCALE/3f, DEFAULT_SCALE/3f);
        }
    }

    public FruitType random(){
        switch(ThreadLocalRandom.current().nextInt(0, 3 )){
            case(0):
                return FruitType.APPLE;
            case(1):
                return FruitType.STRAWBERRY;
            case(2):
                return FruitType.ORANGE;
        }
        return FruitType.APPLE;
    }
    public void addOrder(){
        FruitType a=random();
        FruitType b=random();
        FruitType c=random();
        orders.add(new Order(a,b,c));
    }

    public void completed(){
        for(int i=0; i<Math.min(MaxOrders,orders.size); i++){
            if(orders.get(i).check(skewers)){
                orders.removeIndex(i);
                completed++;
            }
        }
    }
    /**
     * Called to switch skewer
     */
    public void switchSkewer() {
        currentSkewer++;
        currentSkewer %= 3;
    }
    public Skewer getSkewer(){
        return skewers[currentSkewer];
    }

    /**
     * Called when an ingredient has been received
     *
     * This happens when an enemy has been defeated
     *
     * @param type type of ingredient received
     */
    public void acceptIngredient(FruitType type){

        skewers[currentSkewer].acceptIngredient(type);

    }

        class Order {

            private FruitType items[];
            /** Left cap to the status background (grey region) */

            private float progress;
            private float scale = 1.0f;
            private int width = 120;
            private final int time =60;

            public Order(FruitType a, FruitType b, FruitType c) {
//                internal = new AssetDirectory( "loading.json" );

                // Break up the status bar texture into regions

                // No progress so far.
                progress = 0f;


                items = new FruitType[3];
                items[0] = a;
                items[1] = b;
                items[2] = c;
            }
            private float convert_frames_to_time(){
                progress=progress+1;
                int frames= time*60;
                float val= progress/frames;
                return val;
            }
            public boolean update(){
                if (convert_frames_to_time()>=1f){
                    return true;
                }
                return false;

            }

            public void draw(GameCanvas canvas, int height) {
                if (statusFrgRight==null|| statusFrgMiddle==null||
                        statusFrgLeft==null||  statusBkgRight==null|| statusBkgMiddle==null||
                        statusBkgLeft==null) {
                    return;
                }
                canvas.drawProgress( true, statusFrgRight, statusFrgMiddle,
                        statusFrgLeft,  statusBkgRight,
                        statusBkgMiddle, statusBkgLeft, canvas.getWidth()-70, height+25, width, scale/5, convert_frames_to_time());

                canvas.drawAbsolute(skewerTexture, Color.WHITE, skewerTexture.getWidth()/2, skewerTexture.getHeight()/2,
                        canvas.getWidth() - (220/3)-30,height,270,DEFAULT_SCALE,DEFAULT_SCALE*3.5f);
                for (int i = 0; i < items.length; i++) {
                    Color c = Color.WHITE;
                    Texture enemy = getEnemy(items[i]);
                    float size = DEFAULT_SCALE/1.5f;

                    canvas.drawAbsolute(enemy, c, enemy.getWidth()/2, enemy.getHeight()/2,  canvas.getWidth() - 110 + (i * 40), height, 0,size,size);
                }

            }


            public boolean check(Skewer [] skewer){
                for(int i=0; i<skewer.length;i++){
                    Queue<FruitType> q = skewer[i].getQueue();
                    boolean ret=true;
                    if(q.size==3) {

                        for (int j = 0; j < q.size; j++) {
                            ret = ret && (items[j] == q.get(j));
                        }
                        if (ret) {
                           skewer[i].makeEmpty();
                           return true;
                        }
                    }

                }
                return false;
            }

        }

}

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

public class Stars extends Application {
    
    class Interval {
        private float x;
        private float y;
        private Vector span;

        Interval(float x, float y, Vector span){
            this.x = x;
            this.y = y;
            this.span = span;
        }

        public float[] getRoot(){
            return new float[] { x, y };
        }

        public Vector getSpan(){
            return span;
        }
    }

    class Vector {
        private float x;
        private float y;

        Vector(float x, float y){
            this.x = x;
            this.y = y;
        }

        public float[] components() {
            return new float[] { x, y };
        }

        public float xComponent() {
            return x;
        }

        public float yComponent() {
            return y;
        }

        public void scale(float s){
            x *= s;
            y *= s;
        }
    }

    class Star {
        private float x;
        private float y;
        private int size;
        private Vector direction;
        private float velocity;

        Star(float x, float y, int size, Vector dir, float vel){
            this.x = x;
            this.y = y;
            this.size = size;
            direction = dir;
            velocity = vel;
        }

        public float[] getPosition(){
            return new float[] { x, y };
        }

        public int getSize(){
            return size;
        }

        public Vector getDirection(){
            return direction;
        }

        public float getVelocity(){
            return velocity;
        }

        public void setPosition(float[] pos){
            x = pos[0];
            y = pos[1];
        }
    }

    // ------- Program Variables ------------------------------

    final Random rand = new Random();
    final double width = 1280, height = 960;
    final float starPossibility = 0.5f;
    final long interval = 450_000;
    long lastUpdateTime;

    float newStarThreshold = 0.95f;
    ArrayList<Star> starsOnCanvas = new ArrayList<Star>();

    // ------- Program Methods --------------------------------

    void updateStars() {
        if (rand.nextFloat() > newStarThreshold)
            starsOnCanvas.add(generateStar());
        for (int i = 0; i < starsOnCanvas.size(); i++){
            if (starFlyingOutOfCanvas(starsOnCanvas.get(i))){
                starsOnCanvas.remove(i);
                i--;
            }
            else {
                var velocity = starsOnCanvas.get(i).getVelocity();
                var direction = starsOnCanvas.get(i).getDirection();
                var currPos = starsOnCanvas.get(i).getPosition();
                starsOnCanvas.get(i).setPosition(new float[] {
                    currPos[0] += direction.xComponent() * velocity,
                    currPos[1] += direction.yComponent() * velocity
                });
            }
        }
    }

    // ------- Star methods -----------------------------------

    private boolean starFlyingOutOfCanvas(Star s){
        var pos = s.getPosition();
        var xcomp = s.direction.xComponent();
        var ycomp = s.direction.yComponent();

        var xInside = (pos[0] < 0 && xcomp > 0) || (pos[0] > width && xcomp < 0);
        var yInside = (pos[1] < 0 && ycomp > 0) || (pos[1] > height && ycomp < 0);
        
        return xInside && yInside;
    }

    // get star spawn point (outside screen canvas)
    private Interval getInterval(int section){
        var leftBorder = new Interval(-16f, -9f, new Vector(0f, (float)height + 9f));
        var topBorder = new Interval(0f, -9f, new Vector((float)width, 0f));
        var rightBorder = new Interval((float)width, -9f, new Vector((float)width + 16f, (float)height + 9f));
        var bottomBorder = new Interval(0f, (float)height, new Vector((float)width, (float)height + 9f));

        return switch (section){
            case 0 -> leftBorder;
            case 1 -> topBorder;
            case 2 -> rightBorder;
            default -> bottomBorder;
        };
    }

    private float[] nextSpawnPoint(Interval section){
        var xScale = rand.nextFloat();
        var yScale = rand.nextFloat();
        var roots = section.getRoot(); // { x, y }

        return new float[] {
            roots[0] + section.getSpan().xComponent() * xScale, //x spawn
            roots[1] + section.getSpan().yComponent() * yScale, //y spawn
        };
    }

    private Vector nextVector(){
        var xVel = rand.nextFloat();
        var yVel = rand.nextFloat();

        return new Vector(xVel, yVel);
    }

    private Star generateStar(){
        var randSection = rand.nextInt(4);
        var spawn = getInterval(randSection);
        var signum = randSection > 1 ? -1f : 1f;
        var spawnPoint = nextSpawnPoint(spawn);
        var dirVector = nextVector();
        dirVector.scale(signum);
        var velocity = rand.nextFloat() * 30f; //30 = max velocity

        return new Star(
            spawnPoint[0],
            spawnPoint[1],
            rand.nextInt(5),
            dirVector,
            velocity
        );
    }

    // ------- JavaFX stuff -----------------------------------

    @Override
    public void start(Stage primaryStage) {
        var root = new Group();
        var canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        var gc = canvas.getGraphicsContext2D();

        var timer = new AnimationTimer(){
            public void handle(long now){
                long elapsedNs = now - lastUpdateTime;
                if (elapsedNs > interval){
                    updateStars();
                    render(gc);
                    lastUpdateTime = now;
                }
            }
        };

        var scene = new Scene(root);
        primaryStage.setTitle("Shooting stars");
        primaryStage.setScene(scene);
        primaryStage.show();

        timer.start();
    }

    public void render(GraphicsContext gc){
        gc.clearRect(0, 0, width, height);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
        gc.setFill(Color.WHITE);
        for (int i = 0; i < starsOnCanvas.size(); i++){
            Star tmp = starsOnCanvas.get(i);
            var tmpPos = tmp.getPosition();
            var size = tmp.getSize();
            var x = Math.round(tmpPos[0]) + size / 2;
            var y = Math.round(tmpPos[1]) + size / 2;
            gc.fillOval(x, y, size, size);
        }
    }

    public static void main(String[] args){
        launch(args);
    }
}

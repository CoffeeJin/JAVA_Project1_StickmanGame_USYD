package ballboy.model.levels;

import ballboy.ConfigurationParseException;
import ballboy.model.Entity;
import ballboy.model.Level;
import ballboy.model.entities.*;
import ballboy.model.entities.utilities.Vector2D;
import ballboy.model.factories.EntityFactory;
import javafx.scene.paint.Color;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Level logic, with abstract factor methods.
 */
public class LevelImpl implements Level {

    private final List<Entity> entities = new ArrayList<>();
    private final PhysicsEngine engine;
    private final EntityFactory entityFactory;
    private ControllableDynamicEntity<DynamicEntity> hero;
    private Entity finish;
    private Entity cat;
    private double levelHeight;
    private double levelWidth;
    private double levelGravity;
    private double floorHeight;
    private Color floorColor;
    private JSONArray levelConfigurations;
    private JSONObject levelConfiguration;
    private boolean finishFlag = false;
    private int blueScore = 0;
    private int greenScore = 0;
    private int redScore = 0;
    private int levelIndex;
    private final double frameDurationMilli;

    /**
     * A callback queue for post-update jobs. This is specifically useful for scheduling jobs mid-update
     * that require the level to be in a valid state.
     */
    private final Queue<Runnable> afterUpdateJobQueue = new ArrayDeque<>();

    public LevelImpl(
            JSONArray levelConfigurations,
            PhysicsEngine engine,
            EntityFactory entityFactory,
            double frameDurationMilli,
            Integer levelIndex
            ) {
        this.levelConfigurations = levelConfigurations;
        this.engine = engine;
        this.entityFactory = entityFactory;
        this.frameDurationMilli = frameDurationMilli;
        this.levelConfiguration = (JSONObject) levelConfigurations.get(levelIndex);
        this.levelIndex = levelIndex;
        initLevel(levelConfiguration);
    }

    public LevelImpl(
            LevelImpl level,
            Integer levelIndex
    ){
        this.levelConfigurations = level.levelConfigurations;
        this.engine = level.engine;
        this.entityFactory = level.entityFactory;
        this.frameDurationMilli = level.frameDurationMilli;
        this.levelConfiguration = (JSONObject) levelConfigurations.get(levelIndex);
        this.levelIndex = levelIndex;
        initLevel(levelConfiguration);
    }

    public LevelImpl(
            LevelImpl level
    ){
        this.levelConfigurations = level.levelConfigurations;
        this.engine = level.engine;
        this.entityFactory = level.entityFactory;
        this.frameDurationMilli = level.frameDurationMilli;
        this.levelWidth = level.levelWidth;
        this.levelHeight = level.levelHeight;
        this.levelGravity = level.levelGravity;
        this.floorColor = level.floorColor;
        this.floorHeight = level.floorHeight;
        this.levelIndex = level.levelIndex;
        this.redScore = level.redScore;
        this.greenScore = level.greenScore;
        this.blueScore = level.blueScore;
        for (DynamicEntity d:level.getDynamicEntities()){
        if(level.isHero(d)){
                this.hero = new ControllableDynamicEntity<>((ControllableDynamicEntity) d,this);
                this.entities.add(hero);
            }else if(!level.isCat(d)){
                this.entities.add(new DynamicEntityImpl((DynamicEntityImpl) d,this));
            }

        }
        for (DynamicEntity d:level.getDynamicEntities()){
            if(level.isCat(d)){
                this.cat = new DynamicEntityImpl((DynamicEntityImpl) d,this);
                this.entities.add(cat);
            }
        }


        for (StaticEntity s:level.getStaticEntities()){
            if(level.isFinish(s)){
                this.finish= new StaticEntityImpl((StaticEntityImpl) s);
                this.entities.add(finish);
            }else{
                this.entities.add(new StaticEntityImpl((StaticEntityImpl) s));
            }
        }
    }

    /**
     * Instantiates a level from the level configuration.
     *
     * @param levelConfiguration The configuration for the level.
     */
    private void initLevel(JSONObject levelConfiguration) {
        this.levelWidth = ((Number) levelConfiguration.get("levelWidth")).doubleValue();
        this.levelHeight = ((Number) levelConfiguration.get("levelHeight")).doubleValue();
        this.levelGravity = ((Number) levelConfiguration.get("levelGravity")).doubleValue();

        JSONObject floorJson = (JSONObject) levelConfiguration.get("floor");
        this.floorHeight = ((Number) floorJson.get("height")).doubleValue();
        String floorColorWeb = (String) floorJson.get("color");
        this.floorColor = Color.web(floorColorWeb);

        JSONArray generalEntities = (JSONArray) levelConfiguration.get("genericEntities");
        for (Object o : generalEntities) {
            this.entities.add(entityFactory.createEntity(this, (JSONObject) o));
        }

        JSONObject heroConfig = (JSONObject) levelConfiguration.get("hero");
        double maxVelX = ((Number) levelConfiguration.get("maxHeroVelocityX")).doubleValue();

        Object hero = entityFactory.createEntity(this, heroConfig);
        if (!(hero instanceof DynamicEntity)) {
            throw new ConfigurationParseException("hero must be a dynamic entity");
        }
        DynamicEntity dynamicHero = (DynamicEntity) hero;
        Vector2D heroStartingPosition = dynamicHero.getPosition();
        this.hero = new ControllableDynamicEntity<>(dynamicHero, heroStartingPosition, maxVelX, floorHeight,
                levelGravity);
        this.entities.add(this.hero);


        JSONObject finishConfig = (JSONObject) levelConfiguration.get("finish");
        this.finish = entityFactory.createEntity(this, finishConfig);
        this.entities.add(finish);

        JSONObject catConfig = (JSONObject) levelConfiguration.get("cat");
        this.cat = entityFactory.createEntity(this,catConfig);
        this.entities.add(cat);

    }

    @Override
    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    private List<DynamicEntity> getDynamicEntities() {
        return entities.stream().filter(e -> e instanceof DynamicEntity).map(e -> (DynamicEntity) e).collect(
                Collectors.toList());
    }

    private List<StaticEntity> getStaticEntities() {
        return entities.stream().filter(e -> e instanceof StaticEntity).map(e -> (StaticEntity) e).collect(
                Collectors.toList());
    }

    @Override
    public double getLevelHeight() {
        return this.levelHeight;
    }

    @Override
    public double getLevelWidth() {
        return this.levelWidth;
    }

    @Override
    public double getHeroHeight() {
        return hero.getHeight();
    }

    @Override
    public double getHeroWidth() {
        return hero.getWidth();
    }

    @Override
    public double getFloorHeight() {
        return floorHeight;
    }

    @Override
    public Color getFloorColor() {
        return floorColor;
    }

    @Override
    public double getGravity() {
        return levelGravity;
    }

    @Override
    public void update() {
        List<DynamicEntity> dynamicEntities = getDynamicEntities();

        dynamicEntities.stream().forEach(e -> {
            e.update(frameDurationMilli, levelGravity);
        });

        for (int i = 0; i < dynamicEntities.size(); ++i) {
            DynamicEntity dynamicEntityA = dynamicEntities.get(i);

            for (int j = i + 1; j < dynamicEntities.size(); ++j) {
                DynamicEntity dynamicEntityB = dynamicEntities.get(j);

                if (dynamicEntityA.collidesWith(dynamicEntityB)) {
                    dynamicEntityA.collideWith(dynamicEntityB);
                    dynamicEntityB.collideWith(dynamicEntityA);
                    if (!isHero(dynamicEntityA) && !isHero(dynamicEntityB)) {
                        engine.resolveCollision(dynamicEntityA, dynamicEntityB);
                    }
                }
            }

            for (StaticEntity staticEntity : getStaticEntities()) {
                if (dynamicEntityA.collidesWith(staticEntity)) {
                    dynamicEntityA.collideWith(staticEntity);
                    engine.resolveCollision(dynamicEntityA, staticEntity, this);
                }
            }
        }

        dynamicEntities.stream().forEach(e -> engine.enforceWorldLimits(e, this));

        afterUpdateJobQueue.forEach(j -> j.run());
        afterUpdateJobQueue.clear();
    }

    @Override
    public double getHeroX() {
        return hero.getPosition().getX();
    }

    @Override
    public double getHeroY() {
        return hero.getPosition().getY();
    }

    @Override
    public boolean boostHeight() {
        return hero.boostHeight();
    }

    @Override
    public boolean dropHeight() {
        return hero.dropHeight();
    }

    @Override
    public boolean moveLeft() {
        return hero.moveLeft();
    }

    @Override
    public boolean moveRight() {
        return hero.moveRight();
    }

    @Override
    public boolean isHero(Entity entity) {
        return entity == hero;
    }

    //changecat
    @Override
    public boolean isCat(Entity entity) {
        return entity == cat;
    }

    @Override
    public boolean isFinish(Entity entity) {
        return this.finish == entity;
    }

    @Override
    public void resetHero() {
        afterUpdateJobQueue.add(() -> this.hero.reset());
    }

    @Override
    public void finish() {
        finishFlag = true;
    }
    @Override
    public boolean checkFinish(){
        return finishFlag;
    }



    @Override
    public void killEnemy(Entity entity) {
        String[] str = entity.getImage().getUrl().split("/");
        switch (str[str.length - 1]) {
            case "slimeRa.png":
                redScore++;
                break;
            case "slimeGa.png":
                greenScore++;
                break;
            case "slimeBa.png":
                blueScore++;
                break;
        }
        entities.remove(entity);


    }

    public int[] getScores(){
        int[] scores = {redScore,greenScore,blueScore};
        return scores;
    }

    @Override
    public int getLevelIndex() {
        return levelIndex;
    }

}

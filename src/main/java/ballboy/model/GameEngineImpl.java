package ballboy.model;

import ballboy.model.Memento.Memento;
import ballboy.model.Observers.Observer;
import ballboy.model.levels.LevelImpl;

/**
 * Implementation of the GameEngine interface.
 * This provides a common interface for the entire game.
 */
public class GameEngineImpl implements GameEngine {
    private Level level;
    private boolean allLevelFinished = false;
    private Observer gameObserver;
    private Memento memento;

    public GameEngineImpl(Level level,Observer observer) {
        this.level = level;
        this.gameObserver = observer;
        observer.setLevel(level);
    }

    public Level getCurrentLevel() {

        return level;
    }

    public void levelUp() {
        try{
            Level newLevel = new LevelImpl((LevelImpl)level,level.getLevelIndex()+1);
            this.level = newLevel;
            this.gameObserver.levelUp(level);
        }catch(Exception e){
            allLevelFinished = true;
        }
    }

    public boolean boostHeight() {
        return level.boostHeight();
    }

    public boolean dropHeight() {
        return level.dropHeight();
    }

    public boolean moveLeft() {
        return level.moveLeft();
    }

    public boolean moveRight() {
        return level.moveRight();
    }

    public void tick() {
        level.update();
        if(level.checkFinish()){
            levelUp();
        }
    }

    public boolean allLevelFinished(){
        return allLevelFinished;
    }

    public void save(){
        this.memento = new Memento((LevelImpl) level,gameObserver);
    }

    public void load(){
        try{
            this.level = new LevelImpl(memento.getLevel());
            this.gameObserver.loadMarks(memento.loadMarks());
            this.gameObserver.setLevel(level);
        }catch(Exception e){
            System.out.println("There is no saved level");
        }

    }

}
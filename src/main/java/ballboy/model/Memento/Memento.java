package ballboy.model.Memento;

import ballboy.model.Observers.GameObserver;
import ballboy.model.Observers.Observer;
import ballboy.model.levels.LevelImpl;

public class Memento{
    private LevelImpl level;
    private int[] marks;

    public Memento(LevelImpl level, Observer observer){
        this.level = new LevelImpl(level);
        this.marks = observer.saveMarks();
    }

    /**
     * get the level object saved in memento
     * @return
     */
    public LevelImpl getLevel() {
        return level;
    }

    /**
     * get the mark array saved in memento
     * @return
     */
    public int[] loadMarks(){
        return marks;
    }
}

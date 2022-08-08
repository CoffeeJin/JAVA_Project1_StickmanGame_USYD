package ballboy.model.Observers;

import ballboy.model.Level;

public interface Observer {
    /**
     * attach the level
     * @param level
     */
    void setLevel(Level level);

    /**
     * get scores saved in observer
     * @return score array
     */
    int[] getScore();

    /**
     * get total scores saved in observer
     * @return total score array
     */
    int[] getTotalScore();

    /**
     * attach new level when level up
     * @param level
     */
    void levelUp(Level level);

    /**
     * add scores to total scores
     */
    void winUpdate();

    /**
     * get the saved marks
     * @return array of  all scores
     */
    int[] saveMarks();

    /**
     * save the marks
     * @param marks
     */
    void loadMarks(int[] marks);
}

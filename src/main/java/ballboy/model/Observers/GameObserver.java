package ballboy.model.Observers;

import ballboy.model.Level;


public class GameObserver implements Observer{
    private Level level;
    private int blueScore = 0;
    private int greenScore = 0;
    private int redScore = 0;
    private int totalRedScore = 0;
    private int totalBlueScore = 0;
    private int totalGreenScore = 0;

    public GameObserver(){
    }

    public void setLevel(Level level){
        this.level = level;
    }

    public int[] getScore(){
        this.redScore = level.getScores()[0];
        this.greenScore = level.getScores()[1];
        this.blueScore = level.getScores()[2];
        return level.getScores();
    }

    public int[] getTotalScore(){
        return new int[]{totalRedScore,totalGreenScore,totalBlueScore};
    }

    public void levelUp(Level level){
        totalBlueScore += blueScore;
        totalGreenScore += greenScore;
        totalRedScore += redScore;

        this.level = level;
    }
    public void winUpdate(){
        totalBlueScore += blueScore;
        totalGreenScore += greenScore;
        totalRedScore += redScore;
    }
    public int[] saveMarks(){
        int[] marks = {redScore,greenScore,blueScore,totalRedScore,totalGreenScore,totalBlueScore};
        return marks;
    }
    public void loadMarks(int[] marks){
        redScore = marks[0];
        greenScore = marks[1];
        blueScore = marks[2];
        totalRedScore = marks[3];
        totalGreenScore = marks[4];
        totalBlueScore = marks[5];
    }
}

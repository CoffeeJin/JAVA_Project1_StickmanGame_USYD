package ballboy.model.entities.behaviour;

import ballboy.model.Level;
import ballboy.model.entities.DynamicEntity;
import ballboy.model.entities.utilities.Vector2D;

/**
 * behaviour for the square cat
 */
public class SquareCatBehaviourStrategy implements BehaviourStrategy{

    private Level level;

    private boolean topLeft = true;
    private boolean topRight = false;
    private boolean botLeft = false;
    private boolean botRight = false;
    private int count = 0;

    public SquareCatBehaviourStrategy(Level level) {
        this.level = level;

    }

    @Override
    public void behave(
            DynamicEntity entity,
            double frameDurationMilli) {
        int heroWidth = (int)level.getHeroWidth()+1;
        int heroHeight = (int)level.getHeroHeight()+1;
        double x = level.getHeroX();
        double y = level.getHeroY();
        if(topLeft){
            if(count<=heroWidth+20){
                entity.setPosition(new Vector2D(x-20+count,y-20));
                count++;
            }else{
                entity.setPosition(new Vector2D(x+heroWidth,y-20));
                count = 0;
                topLeft = false;
                topRight = true;
            }
        }else if(topRight){
            if(count<=heroHeight+20){
                entity.setPosition(new Vector2D(x+heroWidth,y-20+count));
                count++;
            }else{
                entity.setPosition(new Vector2D(x+heroWidth,y+heroHeight));
                count = 0;
                topRight = false;
                botRight = true;
            }
        }else if(botRight){
            if(count<=heroWidth+20){
                entity.setPosition(new Vector2D(x+heroWidth-count,y+heroHeight));
                count++;
            }else{
                entity.setPosition(new Vector2D(x-20,y+heroHeight));
                count = 0;
                botRight = false;
                botLeft = true;
            }
        }else if(botLeft){
            if(count<=heroHeight+20){
                entity.setPosition(new Vector2D(x-20,y+heroHeight-count));
                count++;
            }else{
                entity.setPosition(new Vector2D(x-20,y-20));
                count = 0;
                botLeft = false;
                topLeft = true;
            }
        }
    }

    @Override
    public SquareCatBehaviourStrategy copy(Level level) {
        return new SquareCatBehaviourStrategy(level);
    }


}


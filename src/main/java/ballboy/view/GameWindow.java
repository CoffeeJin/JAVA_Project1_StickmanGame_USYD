package ballboy.view;

import ballboy.model.Entity;
import ballboy.model.GameEngine;
import ballboy.model.Observers.GameObserver;
import ballboy.model.Observers.Observer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class GameWindow {
    private static final double VIEWPORT_MARGIN_X = 100;
    private static final double VIEWPORT_MARGIN_Y = 50;
    private final int width;
    private final int height;
    private final double frameDurationMilli;
    private final Scene scene;
    private final Pane pane;
    private final GameEngine model;
    private final List<EntityView> entityViews;
    private final BackgroundDrawer backgroundDrawer;
    private double xViewportOffset = 0.0;
    private double yViewportOffset = 0.0;
    private final Label winNotification;
    private Label currentScore;
    private Label totalScore;
    private Observer gameObserver;
    private boolean lock = false;

    public GameWindow(
            GameEngine model,
            int width,
            int height,
            double frameDurationMilli,
            GameObserver gameObserver) {
        this.model = model;
        this.width = width;
        this.height = height;
        this.frameDurationMilli = frameDurationMilli;
        this.gameObserver = gameObserver;
        pane = new Pane();

        this.winNotification = new Label();
        winNotification.setFont(new Font(100));
        winNotification.setLayoutX(220);
        winNotification.setLayoutY(100);
        pane.getChildren().add(winNotification);

        this.currentScore = new Label();
        currentScore.setLayoutY(50);
        currentScore.setLayoutX(200);
        pane.getChildren().add(currentScore);

        this.totalScore = new Label();
        totalScore.setLayoutY(70);
        totalScore.setLayoutX(200);
        pane.getChildren().add(totalScore);

        scene = new Scene(pane, width, height);

        entityViews = new ArrayList<>();

        KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler(model);

        scene.setOnKeyPressed(keyboardInputHandler::handlePressed);
        scene.setOnKeyReleased(keyboardInputHandler::handleReleased);

        backgroundDrawer = new BlockedBackground();
        backgroundDrawer.draw(model, pane);

    }

    public Scene getScene() {
        return scene;
    }

    public void run() {

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(frameDurationMilli),
                t -> this.draw()));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();


    }

    private void draw() {
        if (!lock) {


            model.tick();
            if (model.allLevelFinished()) {
                this.winNotification.setText("Winner!");
                this.lock = true;
                this.gameObserver.winUpdate();
            }
            currentScore.setText("Current Level Score: Red:"+gameObserver.getScore()[0]+"  Green:"+gameObserver.getScore()[1]+"  Blue:"+gameObserver.getScore()[2]);
            totalScore.setText("  Total Level Score: Red:"+gameObserver.getTotalScore()[0]+"  Green:"+gameObserver.getTotalScore()[1]+"  Blue:"+gameObserver.getTotalScore()[2]);
            List<Entity> entities = model.getCurrentLevel().getEntities();

            for (EntityView entityView : entityViews) {
                entityView.markForDelete();
            }

            double heroXPos = model.getCurrentLevel().getHeroX();
            double viewportLeftBar = xViewportOffset + VIEWPORT_MARGIN_X;
            double viewportRightBar = viewportLeftBar + (width - 2 * VIEWPORT_MARGIN_X);

            if (heroXPos < viewportLeftBar) {
                xViewportOffset -= heroXPos - viewportLeftBar;
            } else if (heroXPos + model.getCurrentLevel().getHeroWidth() > viewportRightBar) {
                xViewportOffset += heroXPos + model.getCurrentLevel().getHeroWidth() - viewportRightBar;
            }

            heroXPos -= xViewportOffset;

            if (heroXPos < VIEWPORT_MARGIN_X) {
                if (xViewportOffset >= 0) { // Don't go further left than the start of the level
                    xViewportOffset -= VIEWPORT_MARGIN_X - heroXPos;
                    if (xViewportOffset < 0) {
                        xViewportOffset = 0;
                    }
                }
            }

            double levelRight = model.getCurrentLevel().getLevelWidth();
            double screenRight = xViewportOffset + width - model.getCurrentLevel().getHeroWidth();
            if (screenRight > levelRight) {
                xViewportOffset = levelRight - width + model.getCurrentLevel().getHeroWidth();
            }


            double levelTop = 0.0;

            double levelBottom = model.getCurrentLevel().getLevelHeight();
            double heroYPos = model.getCurrentLevel().getHeroY();
            double heroHeight = model.getCurrentLevel().getHeroHeight();
            double viewportTop = yViewportOffset + VIEWPORT_MARGIN_Y;
            double viewportBottom = yViewportOffset + height - 2 * VIEWPORT_MARGIN_Y;

            if (heroYPos + heroHeight > viewportBottom) {
                // if below, shift down
                yViewportOffset += heroYPos + heroHeight - viewportBottom;
            } else if (heroYPos < viewportTop) {
                // if above, shift up
                yViewportOffset -= viewportTop - heroYPos;
            }

            double screenBottom = yViewportOffset + height;
            double screenTop = yViewportOffset;
            // shift back in the instance when we're near the boundary
            if (screenBottom > levelBottom) {
                yViewportOffset -= screenBottom - levelBottom;
            } else if (screenTop < 0.0) {
                yViewportOffset -= screenTop;
            }


//        double viewportBottomBar = yViewportOffset + height - VIEWPORT_MARGIN_Y;
//        double viewportTopBar = yViewportOffset + VIEWPORT_MARGIN_Y;
//
//        if (heroYPos + model.getCurrentLevel().getHeroHeight() > viewportBottomBar) {
//            yViewportOffset += (heroYPos + model.getCurrentLevel().getHeroHeight()) - viewportBottomBar;
//        } else if (heroYPos < viewportTopBar) {
//            yViewportOffset -= viewportTopBar - heroYPos;
//        }
//
//        heroYPos -= yViewportOffset;
//
//        if (heroYPos > VIEWPORT_MARGIN_Y) {
//            if (yViewportOffset >= 0) { // avoid going further than bottom of the screen
//                yViewportOffset -= heroYPos - VIEWPORT_MARGIN_Y;
//                if (yViewportOffset < 0) {
//                    yViewportOffset = 0;
//                }
//            }
//        }


            backgroundDrawer.update(xViewportOffset, yViewportOffset);


            for (Entity entity : entities) {
                boolean notFound = true;
                for (EntityView view : entityViews) {
                    if (view.matchesEntity(entity)) {
                        notFound = false;
                        view.update(xViewportOffset, yViewportOffset);
                        break;
                    }
                }
                if (notFound) {
                    EntityView entityView = new EntityViewImpl(entity);
                    entityViews.add(entityView);
                    pane.getChildren().add(entityView.getNode());
                }
            }

            for (EntityView entityView : entityViews) {
                if (entityView.isMarkedForDelete()) {
                    pane.getChildren().remove(entityView.getNode());
                }
            }
            entityViews.removeIf(EntityView::isMarkedForDelete);
        }
    }

}

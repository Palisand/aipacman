package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.EnumMap;
import java.util.HashMap;

/**
 * Created by Sam on 12/1/2015.
 */
public class QLearning extends Controller<Constants.MOVE> {

    Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController;
    int nearestTargetIndex = 0;
    double gamma = .8;

    public QLearning(Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController){
        this.ghostController = ghostController;
    }

    public int reward(Game game, Constants.MOVE move){
        int sum = 0;
        int steps = 5;
        sum += moveHasScore(game, move, steps) * 20;
        sum += moveIsCloserToTarget(game, move, steps) * 10;
        sum += moveHasDanger(game, move, steps) * -40;

        return sum;
    }

    public int moveHasScore(Game game, Constants.MOVE move, int steps){
        int maxScore = game.getScore();
        Game copy = game.copy();
        while( steps > 0 ) {
            copy.advanceGame(move, ghostController.getMove(copy, -1));
            if(copy.getScore() > maxScore){
                return 1;
            }
            steps--;
        }
        return 0;
    }

    public int moveIsCloserToTarget(Game game, Constants.MOVE move, int steps){
        double nearestTargetDistance = getTargetDistance(game);
        Game copy = game.copy();
        while( steps > 0 ) {
            copy.advanceGame(move, ghostController.getMove(copy, -1));
            if(getTargetDistance(copy) < nearestTargetDistance){
                return 1;
            }
            steps--;
        }
        return 0;
    }

    public int moveHasDanger(Game game, Constants.MOVE move, int steps){
        int lives = game.getPacmanNumberOfLivesRemaining();
        Game copy = game.copy();
        while( steps > 0 ) {
            copy.advanceGame(move, ghostController.getMove(copy, -1));
            if(copy.getPacmanNumberOfLivesRemaining() < lives){
                return 1;
            }
            steps--;
        }
        return 0;
    }

    public double maxQ(Game game, boolean overall){
        double max = 0;
        for(Constants.MOVE move : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())) {
            double moveReward = overall ? qVal( game, move ) : reward( game, move );

            if (moveReward >= max) {
                max = moveReward;
            }
        }
        return max;
    }

    public double qVal(Game game, Constants.MOVE move){
        Game copy = game.copy();
        copy.advanceGame(move, ghostController.getMove(copy, -1));
        double qVal = reward(copy, move) + gamma * maxQ(copy, false);
        return qVal;
    }

    public int getNearestTargetIndex( Game game){
        int currentPacManIndex=game.getPacmanCurrentNodeIndex();
        int[] activePills=game.getActivePillsIndices();
        int[] activePowerPills=game.getActivePowerPillsIndices();
        int[] targetNodeIndices=new int[activePills.length+activePowerPills.length];

        for(int i=0;i<activePills.length;i++) {
            targetNodeIndices[i] = activePills[i];
        }

        for(int i=0;i<activePowerPills.length;i++) {
            targetNodeIndices[activePills.length + i] = activePowerPills[i];
        }

        return game.getClosestNodeIndexFromNodeIndex(currentPacManIndex, targetNodeIndices, Constants.DM.EUCLID);
    }

    public double getTargetDistance( Game game ){
        int currentPacManIndex = game.getPacmanCurrentNodeIndex();
        double targetDistance = game.getDistance(currentPacManIndex, nearestTargetIndex, Constants.DM.EUCLID);

        return targetDistance;
    }

    public Constants.MOVE getMove(Game game, long timeDue){
        if( nearestTargetIndex == 0 || !game.isPillStillAvailable(nearestTargetIndex) || !game.isPowerPillStillAvailable(nearestTargetIndex) ) {
            nearestTargetIndex = getNearestTargetIndex(game);
        }

        double maxQValue = 0;

        Constants.MOVE nextMove = Constants.MOVE.NEUTRAL;

        for( Constants.MOVE move : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())){
            Game copy = game.copy();
            copy.advanceGame(move, ghostController.getMove(copy, -1));
            double currentQValue = maxQ( copy, true );

            if( currentQValue >= maxQValue ){
                maxQValue = currentQValue;
                nextMove = move;
            }
        }

        return nextMove;
    }
}

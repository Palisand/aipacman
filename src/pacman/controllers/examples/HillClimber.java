package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.internal.Ghost;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;

public class HillClimber extends Controller<MOVE> {

    Controller<EnumMap<Constants.GHOST, MOVE>> ghostController;
    static int nearestTargetIndex;

    public HillClimber(Controller<EnumMap<Constants.GHOST, MOVE>> ghostController){
        this.ghostController = ghostController;
    }

    public MOVE getMove(Game game, long timeDue)
    {
        return hillClimbMove(game, ghostController);
    }

    static public int getNearestTargetIndex( Game game){
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

    static public double getStraightLineDistance( Game game ){
        int currentPacManIndex = game.getPacmanCurrentNodeIndex();
        double straightLineDistance = game.getDistance(currentPacManIndex, nearestTargetIndex, Constants.DM.EUCLID);

        return straightLineDistance;
    }

    public int heuristicCostEstimate( Game game, double currentStraightLineDistance, int currentMaxScore, int currentLives ){

        if ( currentMaxScore < game.getScore() ){
            return 20;
        } else if ( game.getPacmanNumberOfLivesRemaining() < currentLives){
            return Integer.MIN_VALUE;
        } else if ( currentStraightLineDistance > getStraightLineDistance( game ) ){
            return 10;
        } else {
            return 0;
        }
    }

    public MOVE hillClimbMove( Game game, Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController )
    {
        MOVE nextMove = MOVE.NEUTRAL;
        int maxScore = game.getScore();
        if( nearestTargetIndex == 0 || !game.isPillStillAvailable(nearestTargetIndex) || !game.isPowerPillStillAvailable(nearestTargetIndex) ) {
            nearestTargetIndex = getNearestTargetIndex( game );
        }
        double currentStraightLineDistance = getStraightLineDistance( game );

        int maxHeuristic = -5;
        for ( MOVE move : game.getPossibleMoves(game.getPacmanCurrentNodeIndex())){
            Game copy = game.copy();
            copy.advanceGame(move, ghostController.getMove(copy, -1));
            int currentHeuristic = heuristicCostEstimate( copy, currentStraightLineDistance, maxScore, game.getPacmanNumberOfLivesRemaining() );
            if( currentHeuristic > maxHeuristic ){
                maxHeuristic = currentHeuristic;
                nextMove = move;
            }
        }
        return nextMove;
    }
}

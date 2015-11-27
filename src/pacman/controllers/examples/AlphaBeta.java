package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.internal.Node;

import java.util.*;

/**
 * Created by Sam on 11/26/2015.
 */
public class AlphaBeta extends Controller<Constants.MOVE> {
    Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController;
    Controller<EnumMap<Constants.GHOST, Constants.MOVE>> optimalGhost = new OptimalGhosts();
    static int lives = -1;
    static int nearestTargetIndex = 0;

    public AlphaBeta(Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController){
        this.ghostController = ghostController;
    }

    public Constants.MOVE getMove(Game game, long timeDue)
    {
        if( lives == -1 ){
            lives = game.getPacmanNumberOfLivesRemaining();
        }

        if( game.getPacmanNumberOfLivesRemaining() < lives ){
            lives = game.getPacmanNumberOfLivesRemaining();
            nearestTargetIndex = getNearestTargetIndex(game);
        }
        return alphaBetaMove(game, ghostController);
    }

    static public int getNearestTargetIndex(Game game){
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

        return game.getClosestNodeIndexFromNodeIndex(currentPacManIndex, targetNodeIndices, Constants.DM.PATH);
    }

    static public double getStraightLineDistance( Game game ){
        int currentPacManIndex = game.getPacmanCurrentNodeIndex();
        double straightLineDistance = game.getDistance(currentPacManIndex, nearestTargetIndex, Constants.DM.PATH);
        return straightLineDistance;
    }

    public int heuristicCostEstimate( Game game, double currentStraightLineDistance, int nearestGhost, int nearestEdibleGhost, int currentMaxScore, int currentLives ) {

        if ( game.getPacmanNumberOfLivesRemaining() < currentLives){
            return Integer.MIN_VALUE;
        } else if (nearestGhost < game.getNearestGhostDistance(false)){
            if ( currentMaxScore < game.getScore() ){
                return 40;
            } else if ( nearestEdibleGhost > game.getNearestGhostDistance(true)) {
                return 35;
            } else if ( currentStraightLineDistance > getStraightLineDistance(game) ){
                return 30;
            } else {
                return 25;
            }
        } else {
            if ( nearestEdibleGhost > game.getNearestGhostDistance(true)) {
                return 20;
            } else if ( currentMaxScore < game.getScore() ){
                return 15;
            } else if ( currentStraightLineDistance > getStraightLineDistance(game) ){
                return 10;
            } else {
                return 5;
            }
        }
    }

    public Constants.MOVE alphaBetaMove( Game game, Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController )
    {
        Constants.MOVE nextMove = Constants.MOVE.NEUTRAL;
        int maxHeuristic = Integer.MIN_VALUE;
        for ( Constants.MOVE move : game.getPossibleMoves(game.getPacmanCurrentNodeIndex())){
            Game copy = game.copy();
            copy.advanceGame(move, ghostController.getMove(copy, -1));
            if( nearestTargetIndex == 0 || !copy.isPillStillAvailable(nearestTargetIndex) || !copy.isPowerPillStillAvailable(nearestTargetIndex) ) {
                nearestTargetIndex = getNearestTargetIndex( copy );
            }
            int maxScore = copy.getScore();
            double currentStraightLineDistance = getStraightLineDistance(copy);
            int nearestGhost = copy.getNearestGhostDistance(false);
            int nearestEdibleGhost = copy.getNearestGhostDistance(true);
            int alpha = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;
            int currentHeuristic = alphabeta(copy, 3, alpha, beta, true, currentStraightLineDistance, nearestGhost, nearestEdibleGhost, maxScore, copy.getPacmanNumberOfLivesRemaining());
            if( currentHeuristic > maxHeuristic ){
                maxHeuristic = currentHeuristic;
                nextMove = move;
            }
        }
        return nextMove;
    }

    public int alphabeta(Game game, int depth, int alpha, int beta, boolean maximizingPlayer, double currentStraightLineDistance, int nearestGhost, int nearestEdibleGhost, int maxScore, int livesRemaining) {

        if (depth == 0 || game.gameOver()) {
            return heuristicCostEstimate(game, currentStraightLineDistance, nearestGhost, nearestEdibleGhost, maxScore, livesRemaining);
        }
        if (maximizingPlayer){
            int v = Integer.MIN_VALUE;
            for( Constants.MOVE move : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())) {
                Game copy = game.copy();
                copy.advanceGame(move, optimalGhost.getMove(copy, -1));
                v = Math.max(v, alphabeta( copy, depth - 1, alpha, beta, !maximizingPlayer, currentStraightLineDistance, nearestGhost, nearestEdibleGhost, maxScore, livesRemaining));
                alpha = Math.max( alpha, v );
                if( beta <= alpha ){
                    break;
                }
            }
            return v;
        } else {
            int v = Integer.MAX_VALUE;
            for( Constants.MOVE move : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())) {
                Game copy = game.copy();
                copy.advanceGame(move, optimalGhost.getMove(copy, -1));
                v = Math.min(v, alphabeta( copy, depth - 1, alpha, beta, !maximizingPlayer, currentStraightLineDistance, nearestGhost, nearestEdibleGhost, maxScore, livesRemaining));
                beta = Math.min( alpha, v );
                if( beta <= alpha ){
                    break;
                }
            }
            return v;
        }
    }
}

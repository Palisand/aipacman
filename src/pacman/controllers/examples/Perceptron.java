package pacman.controllers.examples;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.io.*;
import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Created by Sam on 11/29/2015.
 */
public class Perceptron extends Controller<Constants.MOVE> {
    ArrayList<NeuralNetwork> networks = new ArrayList<NeuralNetwork>();
    UpNetwork up = new UpNetwork();
    DownNetwork down = new DownNetwork();
    LeftNetwork left = new LeftNetwork();
    RightNetwork right = new RightNetwork();
    NeutralNetwork neutral = new NeutralNetwork();
    static Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController;
    static int nearestTargetIndex = 0;

    public Perceptron(boolean doTraining, Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController ){

        this.ghostController = ghostController;

        networks.add(up);
        networks.add(down);
        networks.add(left);
        networks.add(right);
        networks.add(neutral);

        NeuralNetwork network = new NeuralNetwork();
        network.initWeights(doTraining);
        if(doTraining){
            network.trainNetwork();
        }
    }

    public static int getNearestTargetIndex( Game game){
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

    public static double getTargetDistance( Game game ){
        int currentPacManIndex = game.getPacmanCurrentNodeIndex();
        double targetDistance = game.getDistance(currentPacManIndex, nearestTargetIndex, Constants.DM.EUCLID);

        return targetDistance;
    }

    public Constants.MOVE getMove(Game game, long timeDue) {
        if( nearestTargetIndex == 0 || !game.isPillStillAvailable(nearestTargetIndex) || !game.isPowerPillStillAvailable(nearestTargetIndex) ) {
            nearestTargetIndex = getNearestTargetIndex( game );
        }

        for(NeuralNetwork network : networks){
            Constants.MOVE move = network.move(game);
            if( move != null ){
                for( Constants.MOVE posMove : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())){
                    if( move == posMove ){
                        return move;
                    }
                }
            }
        }
        return Constants.MOVE.NEUTRAL;
    }

    public static String moveHasScore(Game game, Constants.MOVE move, int steps){
        int maxScore = game.getScore();
        Game copy = game.copy();
        while( steps > 0 ) {
            copy.advanceGame(move, ghostController.getMove(copy, -1));
            if(copy.getScore() > maxScore){
                return "1.0,";
            }
            steps--;
        }
        return "0.0,";
    }

    public static String moveIsCloserToTarget(Game game, Constants.MOVE move, int steps){
        double nearestTargetDistance = getTargetDistance(game);
        Game copy = game.copy();
        while( steps > 0 ) {
            copy.advanceGame(move, ghostController.getMove(copy, -1));
            if(getTargetDistance(copy) < nearestTargetDistance){
                return "1.0,";
            }
            steps--;
        }
        return "0.0,";
    }

    public static String moveHasDanger(Game game, Constants.MOVE move, int steps){
        int lives = game.getPacmanNumberOfLivesRemaining();
        Game copy = game.copy();
        while( steps > 0 ) {
            copy.advanceGame(move, ghostController.getMove(copy, -1));
            if(copy.getPacmanNumberOfLivesRemaining() < lives){
                return "1.0";
            }
            steps--;
        }
        return "0.0";
    }
}
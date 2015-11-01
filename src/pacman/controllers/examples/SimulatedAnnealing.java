package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.internal.Ghost;

import java.util.*;

public class SimulatedAnnealing extends Controller<MOVE> {

    Controller<EnumMap<Constants.GHOST, MOVE>> ghostController;

    final int START_TEMP = 100;
    double temperature = START_TEMP;  // T
    double evalChange;  // ∆E
    double probability;
    int nearestTargetIndex;

    Random random = new Random();

    public SimulatedAnnealing(Controller<EnumMap<Constants.GHOST, MOVE>> ghostController){
        this.ghostController = ghostController;
    }

    public MOVE getMove(Game game, long timeDue) {
        return simAnnealingMove(game, ghostController);
    }

    public double getGameValue(Game game) {
        if( nearestTargetIndex == 0 || !game.isPillStillAvailable(nearestTargetIndex) || !game.isPowerPillStillAvailable(nearestTargetIndex) ) {
            nearestTargetIndex = HillClimber.getNearestTargetIndex(game);
        }
        double straightLineDistance = HillClimber.getStraightLineDistance(game);

        if (game.wasPacManEaten())
            return Integer.MIN_VALUE;
        else
            return game.getScore() - straightLineDistance;
    }

    public double getEvalChange(Game current, Game next) {
        double currentValue = getGameValue(current);
        double nextValue = getGameValue(next);
        return nextValue - currentValue;
    }

    public MOVE simAnnealingMove(Game game, Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController) {
        Game current = game.copy();
        while (true) {
            if (temperature-- < 0) {
                temperature = START_TEMP;
                return current.getPacmanLastMoveMade();
            }
            MOVE[] moves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
            MOVE selected = moves[random.nextInt(moves.length)];
            Game next = game.copy();
            next.advanceGame(selected, ghostController.getMove(next, -1));
            evalChange = getEvalChange(game, next);
            if (evalChange > 0) {
                current = next;
//                temperature = START_TEMP;
//                return current.getPacmanLastMoveMade();
            }
            else {
                probability = Math.exp(evalChange / temperature);  // e^(∆E/T)
                if (random.nextDouble() < probability) {
                    current = next;
                }
            }
        }
    }
}

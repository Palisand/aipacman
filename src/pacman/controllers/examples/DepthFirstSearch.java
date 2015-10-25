package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;

import static pacman.game.Constants.MOVE;

public class DepthFirstSearch extends Controller<MOVE> {

    /* (non-Javadoc)
     * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
     */
    Controller<EnumMap<Constants.GHOST, MOVE>> ghostController;

    public DepthFirstSearch(Controller<EnumMap<Constants.GHOST, MOVE>> ghostController) {
        this.ghostController = ghostController;
    }

    public MOVE getMove(Game game, long timeDue) {
        MOVE nextMove;
        HashMap<Integer, LinkedList<MOVE>> paths = new HashMap<Integer, LinkedList<MOVE>>();
        HashSet<Integer> explored = new HashSet<Integer>();
        int maxScore = game.getScore();
        nextMove = dfsRecursive(game, paths, explored, maxScore);
        return nextMove;
    }

    public MOVE dfsRecursive(Game currentGame, HashMap<Integer, LinkedList<MOVE>> paths, HashSet<Integer> explored, int maxScore) {

        int currentScore = currentGame.getScore();

        if ((currentScore > maxScore)) {
            return paths.get(currentGame.getPacmanCurrentNodeIndex()).poll();
        }

        explored.add(currentGame.getPacmanCurrentNodeIndex());

        if (!paths.containsKey(currentGame.getPacmanCurrentNodeIndex())) {
            paths.put(currentGame.getPacmanCurrentNodeIndex(), new LinkedList<MOVE>());
        }
        LinkedList<MOVE> pathSoFar = paths.get(currentGame.getPacmanCurrentNodeIndex());

        for (MOVE move : currentGame.getPossibleMoves(currentGame.getPacmanCurrentNodeIndex(), currentGame.getPacmanLastMoveMade())) {
            Game copy = currentGame.copy();
            copy.advanceGame(move, ghostController.getMove(copy, -1));

            if (!paths.containsKey(copy.getPacmanCurrentNodeIndex())) {
                paths.put(copy.getPacmanCurrentNodeIndex(), (LinkedList<MOVE>) pathSoFar.clone());
            }
            paths.get(copy.getPacmanCurrentNodeIndex()).add(move);

            if (!explored.contains(copy.getPacmanCurrentNodeIndex())) {
                return dfsRecursive(copy, paths, explored, maxScore);
            }
        }
        return MOVE.NEUTRAL;
    }
}
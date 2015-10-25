package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;

import static pacman.game.Constants.MOVE;

public class IterativeDeepening extends Controller<MOVE> {

    /* (non-Javadoc)
     * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
     */
    Controller<EnumMap<Constants.GHOST, MOVE>> ghostController;
    DepthLimitedSearch dfdlsController;

    public IterativeDeepening(Controller<EnumMap<Constants.GHOST, MOVE>> ghostController) {
        dfdlsController = new DepthLimitedSearch(ghostController, 0);
    }

    public MOVE getMove(Game game, long timeDue) {
        int depth = 0;
        MOVE nextMove = MOVE.NEUTRAL;
        while( nextMove == MOVE.NEUTRAL ){
            ++depth;
            nextMove = dfdlsController.getMove(game, timeDue, depth, nextMove);
        }
        return nextMove;
    }

}
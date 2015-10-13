package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.EnumMap;

import static pacman.game.Constants.DM;
import static pacman.game.Constants.MOVE;

/*
 * The Class NearestPillBFS.
 */
public class BreadthFirstSearch extends Controller<MOVE> {

    /* (non-Javadoc)
     * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
     */
    Controller<EnumMap<Constants.GHOST, MOVE>> ghostController;

    public BreadthFirstSearch(Controller<EnumMap<Constants.GHOST, MOVE>> ghostController){
        this.ghostController = ghostController;
    }

    public MOVE getMove(Game game, long timeDue) {
        return game.breadthFirstSearchMove(game, ghostController);
    }
}
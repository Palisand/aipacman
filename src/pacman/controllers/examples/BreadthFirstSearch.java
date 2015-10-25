package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

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

    public MOVE getMove(Game game, long timeDue)
    {
        return breadthFirstSearchMove(game, ghostController);
    }

    public MOVE breadthFirstSearchMove( Game game, Controller<EnumMap<Constants.GHOST, MOVE>> ghostController )
    {
        MOVE nextMove = MOVE.NEUTRAL;
        LinkedList<Game> frontier = new LinkedList<Game>();
        HashMap<Integer, LinkedList<MOVE>> paths = new HashMap<Integer, LinkedList<MOVE>>();
        HashSet<Integer> explored = new HashSet<Integer>();

        frontier.add(game);

        int maxScore = game.getScore();
        boolean routeFound = false;

        while( !frontier.isEmpty() && !routeFound ){
            Game currentGame = frontier.poll();

            if(!paths.containsKey(currentGame.getPacmanCurrentNodeIndex())){
                paths.put(currentGame.getPacmanCurrentNodeIndex(), new LinkedList<MOVE>());
            }
            LinkedList<MOVE> pathSoFar = paths.get(currentGame.getPacmanCurrentNodeIndex());

            if(!explored.contains(currentGame.getPacmanCurrentNodeIndex())) {
                for(MOVE move : currentGame.getPossibleMoves(currentGame.getPacmanCurrentNodeIndex(), currentGame.getPacmanLastMoveMade())){
                    Game copy = currentGame.copy();
                    copy.advanceGame(move, ghostController.getMove(copy, -1));
                    if(!paths.containsKey(copy.getPacmanCurrentNodeIndex())){
                        paths.put(copy.getPacmanCurrentNodeIndex(), (LinkedList<MOVE>) pathSoFar.clone());
                    }
                    paths.get(copy.getPacmanCurrentNodeIndex()).add(move);
                    frontier.add(copy);
                    int currentScore = copy.getScore();
                    if ((currentScore > maxScore)) {
                        nextMove = paths.get(copy.getPacmanCurrentNodeIndex()).poll();
                        routeFound = true;
                        break;
                    }
                    explored.add(currentGame.getPacmanCurrentNodeIndex());
                }
            }
        }
        return nextMove;
    }
}
package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.EnumMap;

/**
 * Created by Sam on 11/26/2015.
 */
public class OptimalGhosts extends Controller<EnumMap<Constants.GHOST,Constants.MOVE>>
{
    private EnumMap<Constants.GHOST,Constants.MOVE> myMoves=new EnumMap<Constants.GHOST,Constants.MOVE>(Constants.GHOST.class);

    /* (non-Javadoc)
     * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
     */
    public EnumMap<Constants.GHOST,Constants.MOVE> getMove(Game game,long timeDue)
    {
        myMoves.clear();

        for(Constants.GHOST ghost : Constants.GHOST.values())				//for each ghost
            if(game.doesGhostRequireAction(ghost))		//if it requires an action
            {
                myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
                        game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost), Constants.DM.PATH));
            }

        return myMoves;
    }
}
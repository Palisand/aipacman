package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;

import static pacman.game.Constants.MOVE;

public class OurAStar extends Controller<MOVE> {

    /* (non-Javadoc)
     * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
     */
    Controller<EnumMap<Constants.GHOST, MOVE>> ghostController;
    HashMap<Integer, Double> fScore = new HashMap<Integer,Double>();

    public OurAStar(Controller<EnumMap<Constants.GHOST, MOVE>> ghostController) {
        this.ghostController = ghostController;

    }

    public MOVE getMove(Game game, long timeDue)
    {
        fScore.clear();
        return AStarMove(game, ghostController);
    }

    public double heuristicCostEstimate( Game game ){
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

        int nearestPillIndex = game.getClosestNodeIndexFromNodeIndex(currentPacManIndex, targetNodeIndices, Constants.DM.EUCLID);
        double straightLineDistance = game.getDistance(currentPacManIndex, nearestPillIndex, Constants.DM.EUCLID);

        return straightLineDistance;
    }

    public MOVE AStarMove( Game game, Controller<EnumMap<Constants.GHOST, MOVE>> ghostController )
    {
        MOVE nextMove = MOVE.NEUTRAL;
        HashSet<Integer> openSet = new HashSet<Integer>();
        HashSet<Integer> closedSet = new HashSet<Integer>();
        PriorityQueue<Game> frontier = new PriorityQueue<Game>(20, new FScoreComparator());
        HashMap<Integer, Double> gScore = new HashMap<Integer, Double>();
        HashMap<Integer, LinkedList<MOVE>> paths = new HashMap<Integer, LinkedList<MOVE>>();

        gScore.put(game.getPacmanCurrentNodeIndex(), 0.0);
        fScore.put(game.getPacmanCurrentNodeIndex(), gScore.get(game.getPacmanCurrentNodeIndex()) + heuristicCostEstimate(game));

        int maxScore = game.getScore();
        boolean routeFound = false;
        frontier.add( game );

        while( !frontier.isEmpty() && !routeFound ){
            Game currentGame = frontier.poll();
            openSet.remove(currentGame.getPacmanCurrentNodeIndex());

            if(!paths.containsKey(currentGame.getPacmanCurrentNodeIndex())){
                paths.put(currentGame.getPacmanCurrentNodeIndex(), new LinkedList<MOVE>());
            }
            LinkedList<MOVE> pathSoFar = paths.get(currentGame.getPacmanCurrentNodeIndex());

            if( currentGame.getScore() > maxScore ){
                nextMove = paths.get(currentGame.getPacmanCurrentNodeIndex()).poll();
                routeFound = true;
            }

            closedSet.add(currentGame.getPacmanCurrentNodeIndex());
            for(MOVE move : currentGame.getPossibleMoves(currentGame.getPacmanCurrentNodeIndex())) {
                Game copy = currentGame.copy();
                copy.advanceGame(move, ghostController.getMove(copy, -1));

                if(closedSet.contains(copy.getPacmanCurrentNodeIndex())){
                   continue;
                }

                double tentativeGScore = gScore.get(currentGame.getPacmanCurrentNodeIndex()) + 1;

                if(!openSet.contains(copy.getPacmanCurrentNodeIndex())){
                    openSet.add(copy.getPacmanCurrentNodeIndex());
                    if(!bfsIsDangerAhead(copy)){
                        frontier.add(copy);
                    }
                } else if (tentativeGScore >= gScore.get(copy.getPacmanCurrentNodeIndex())){
                    continue;
                }

                if(!paths.containsKey(copy.getPacmanCurrentNodeIndex())){
                    paths.put(copy.getPacmanCurrentNodeIndex(), (LinkedList<MOVE>) pathSoFar.clone());
                }
                paths.get(copy.getPacmanCurrentNodeIndex()).add(move);
                gScore.put(copy.getPacmanCurrentNodeIndex(), tentativeGScore);
                fScore.put(copy.getPacmanCurrentNodeIndex(), gScore.get(copy.getPacmanCurrentNodeIndex()) + heuristicCostEstimate( copy ));
            }
        }

        return nextMove;
    }

    class FScoreComparator implements Comparator<Game> {

        @Override
        public int compare(Game o1, Game o2) {
            double o1Score, o2Score;

            o1Score = fScore.get(o1.getPacmanCurrentNodeIndex()) == null ? Integer.MAX_VALUE : fScore.get(o1.getPacmanCurrentNodeIndex());
            o2Score = fScore.get(o2.getPacmanCurrentNodeIndex()) == null ? Integer.MAX_VALUE : fScore.get(o2.getPacmanCurrentNodeIndex());

            return (int) (o1Score - o2Score);
        }
    }

    public boolean bfsIsDangerAhead( Game game ){
        boolean danger = false;
        int i = 5;
        int lives = game.getPacmanNumberOfLivesRemaining();
        for(MOVE secondaryMove : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())) {
            Game copy = game.copy();
            while( i > 0 ){
                copy.advanceGame(secondaryMove, ghostController.getMove(copy, -1));
                i--;
                if (copy.getPacmanNumberOfLivesRemaining() < lives) {
                    danger = true;
                    break;
                }
            }
            if(danger){
                break;
            }
        }

        return danger;
    }
}
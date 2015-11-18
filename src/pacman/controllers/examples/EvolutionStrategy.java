package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.util.*;

/**
 * Created by Sam on 11/18/2015.
 */
public class EvolutionStrategy extends Controller<MOVE>{
    Controller<EnumMap<Constants.GHOST, MOVE>> ghostController;
    int m = 3;
    int l = 7;
    int genomeSize = 10;
    int generations = 5;
    int populationSize = m + l;
    ArrayList<MOVE> moveTypes = new ArrayList<MOVE>();

    public EvolutionStrategy(Controller<EnumMap<Constants.GHOST, MOVE>> ghostController){
        this.ghostController = ghostController;
        moveTypes.add(MOVE.NEUTRAL);
        moveTypes.add(MOVE.UP);
        moveTypes.add(MOVE.DOWN);
        moveTypes.add(MOVE.LEFT);
        moveTypes.add(MOVE.RIGHT);
    }

    public void fillPopulation(ArrayList<LinkedList<MOVE>> population){
        for( int i = 0; i < populationSize; i++ ){
            LinkedList<MOVE> genome = new LinkedList<MOVE>();
            for( int j = 0; j < genomeSize; j++ ){
                genome.add( moveTypes.get( ( int ) (Math.random() * 5) ) );
            }
            population.add(genome);
        }
    }

    public ArrayList<LinkedList<MOVE>> sortByFitness(HashMap<LinkedList<MOVE>, Integer> fitnessMap){
        Set<Map.Entry<LinkedList<MOVE>, Integer>> set = fitnessMap.entrySet();
        ArrayList<Map.Entry<LinkedList<MOVE>, Integer>> list = new ArrayList<Map.Entry<LinkedList<MOVE>, Integer>>(set);
        Collections.sort( list, new Comparator<Map.Entry<LinkedList<MOVE>, Integer>>()
        {
            public int compare( Map.Entry<LinkedList<MOVE>, Integer> o1, Map.Entry<LinkedList<MOVE>, Integer> o2){
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        });
        ArrayList<LinkedList<MOVE>> sortedPopulation = new ArrayList<LinkedList<MOVE>>();
        for( Map.Entry<LinkedList<MOVE>, Integer> entry : list ){
            sortedPopulation.add(entry.getKey());
        }
        return sortedPopulation;
    }

    public void mutate( ArrayList<LinkedList<MOVE>> population){
        while( population.size() > m ){
            population.remove( population.size() - 1 );
        }
        while( population.size() < populationSize ){
            LinkedList<MOVE> randomSelection = population.get( (int) (Math.random() * m) );
            LinkedList<MOVE> mutation = new LinkedList<MOVE>();
            for( MOVE move : randomSelection ){
                MOVE newMove;
                if( (int) (Math.random() * 10) >= 5){
                    newMove = move.opposite();
                } else {
                    newMove = move;
                }
                mutation.add( newMove );
            }
            population.add(mutation);
        }
    }

    public MOVE getMove(Game game, long timeDue)
    {
        return evoStratMove(game, ghostController);
    }

    public MOVE evoStratMove( Game game, Controller<EnumMap<Constants.GHOST, MOVE>> ghostController )
    {
        HashMap<LinkedList<MOVE>, Integer> fitnessMap = new HashMap<LinkedList<MOVE>, Integer>();
        ArrayList<LinkedList<MOVE>> population = new ArrayList<LinkedList<MOVE>>();
        fillPopulation(population);
        int generationCount = 0;
        while( generationCount < generations ){
            mutate(population);
            for( LinkedList<MOVE> genome : population ){
                Game copy = game.copy();
                for( MOVE move : genome){
                    copy.advanceGame( move, ghostController.getMove(copy, -1));
                }
                fitnessMap.put( genome, copy.getScore() );
            }
            population = sortByFitness(fitnessMap);
            generationCount++;
        }
        return population.get(0).pollFirst();
    }
}

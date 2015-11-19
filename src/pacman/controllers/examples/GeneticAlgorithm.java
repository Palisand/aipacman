package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.util.*;

public class GeneticAlgorithm extends Controller<MOVE>{
    Controller<EnumMap<Constants.GHOST, MOVE>> ghostController;
    int m = 3;
    int l = 7;
    int genomeSize = 10;
    int generations = 7;
    int populationSize = m + l;
    ArrayList<MOVE> moveTypes = new ArrayList<MOVE>();

    public GeneticAlgorithm(Controller<EnumMap<Constants.GHOST, MOVE>> ghostController){
        this.ghostController = ghostController;
        moveTypes.add(MOVE.UP);
        moveTypes.add(MOVE.DOWN);
        moveTypes.add(MOVE.LEFT);
        moveTypes.add(MOVE.RIGHT);
    }

    public void fillPopulation(ArrayList<LinkedList<MOVE>> population){
        for( int i = 0; i < populationSize; i++ ){
            LinkedList<MOVE> genome = new LinkedList<MOVE>();
            for( int j = 0; j < genomeSize; j++ ){
                genome.add( moveTypes.get( ( int ) (Math.random() * 4) ) );
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

    public void mutate( LinkedList<MOVE> selection){
        for( MOVE move : selection ){
            if( (int) (Math.random() * 10) >= 5){
                move = move.opposite();
            }
        }
    }

    public LinkedList<MOVE> reproduce( LinkedList<MOVE> parentX, LinkedList<MOVE> parentY){
        int n = parentX.size();
        int c = (int) ( Math.random() * n);
        LinkedList<MOVE> child = new LinkedList<MOVE>();
        for(int i = 0; i < parentX.size(); i++){
            if( i <= c ){
                child.add(parentX.get(i));
            } else {
                child.add(parentY.get(i));
            }
        }

        return child;
    }

    public MOVE getMove(Game game, long timeDue)
    {
        return geneticAlgoMove(game, ghostController);
    }

    public MOVE geneticAlgoMove( Game game, Controller<EnumMap<Constants.GHOST, MOVE>> ghostController )
    {
        HashMap<LinkedList<MOVE>, Integer> fitnessMap = new HashMap<LinkedList<MOVE>, Integer>();
        ArrayList<LinkedList<MOVE>> population = new ArrayList<LinkedList<MOVE>>();
        fillPopulation(population);
        int generationCount = 0;

        while( generationCount < generations ) {
            ArrayList<LinkedList<MOVE>> newPopulation = new ArrayList<LinkedList<MOVE>>();
            for (int i = 0; i < population.size(); i++) {
                LinkedList<MOVE> parentX = population.get((int) (Math.random() * populationSize));
                LinkedList<MOVE> parentY = population.get((int) (Math.random() * populationSize));
                LinkedList<MOVE> child = reproduce(parentX, parentY);
                if (Math.random() * 100 < 5) {
                    mutate(child);
                }
                newPopulation.add(child);
            }
            population.addAll(newPopulation);
            generationCount++;
        }
        for (LinkedList<MOVE> genome : population) {
            Game copy = game.copy();
            for (MOVE move : genome) {
                copy.advanceGame(move, ghostController.getMove(copy, -1));
            }
            fitnessMap.put(genome, copy.getScore());
        }
        population = sortByFitness(fitnessMap);
        return population.get(0).pollFirst();
    }
}

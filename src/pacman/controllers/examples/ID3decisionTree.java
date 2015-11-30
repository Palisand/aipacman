package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

import java.util.*;
import java.io.*;

public class ID3decisionTree extends Controller<MOVE>{
    String examplesFileName = "myData/id3_examples.txt";
    String delim = ",";
    ID3node tree = new ID3node();
    Controller<EnumMap<GHOST, MOVE>> ghostController;
    List<String> attributes = Arrays.asList(
//            "alternate", "bar", "friday", "hungry", "patrons", "price", "rain", "reservation", "type", "estimate"
            "ghost_is_near", "moved_closer_to_ghost", "moved_closer_to_pill",
            "moved_closer_to_power_pill", "score_increased", "was_eaten" // refers to nearest objects
    );
    Map<String,List<String>> attrToValues = new HashMap<String,List<String>>();

    List<List<String>> examples = new ArrayList<List<String>>();
    int goalIndex = 6;  // 10 for textbook example, 6 for project

    public ID3decisionTree(Controller<EnumMap<GHOST, MOVE>> ghostController) {
//        attrToValues.put("alternate", Arrays.asList("yes", "no"));
//        attrToValues.put("bar", Arrays.asList("yes", "no"));
//        attrToValues.put("friday", Arrays.asList("yes", "no"));
//        attrToValues.put("hungry", Arrays.asList("yes", "no"));
//        attrToValues.put("patrons", Arrays.asList("some", "full", "none"));
//        attrToValues.put("price", Arrays.asList("$","$$","$$$"));
//        attrToValues.put("rain", Arrays.asList("yes", "no"));
//        attrToValues.put("reservation", Arrays.asList("yes", "no"));
//        attrToValues.put("type", Arrays.asList("french", "thai", "burger", "italian"));
//        attrToValues.put("estimate", Arrays.asList("0-10", "10-30", "30-60", ">60"));
        attrToValues.put("ghost_is_near", Arrays.asList("yes", "no"));
        attrToValues.put("moved_closer_to_ghost", Arrays.asList("yes", "no"));
        attrToValues.put("moved_closer_to_pill", Arrays.asList("yes", "no"));
        attrToValues.put("moved_closer_to_power_pill", Arrays.asList("yes", "no"));
        attrToValues.put("score_increased", Arrays.asList("yes", "no"));
        attrToValues.put("was_eaten", Arrays.asList("yes", "no"));
        buildTrees();
        this.ghostController = ghostController;
    }

    public MOVE getMove(Game game, long timeDue) {
        return id3Move(game);
    }

    public void buildTrees() {
        List<List<String>> examples = getExamplesFromFile();
        tree = decisionTreeLearning(examples, attributes, null);
        tree.print();
    }

    public List<List<String>> getExamplesFromFile() {
        String line;
        List<List<String>> examples = new ArrayList<List<String>>();
        FileReader fileReader;
        try {
            fileReader = new FileReader(examplesFileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                examples.add(Arrays.asList(line.split(delim)));
            }
            fileReader.close();
            bufferedReader.close();
        }
        catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + examplesFileName + "'");
            System.exit(1);
        }
        catch (IOException ex) {
            System.out.println("Error reading file '" + examplesFileName + "'");
            ex.printStackTrace();
            System.exit(1);
        }
        return examples;
    }

    public ID3node decisionTreeLearning(List<List<String>> examples, List<String> attributes, List<List<String>> parentExamples) {
        if (examples.isEmpty()) {
            return pluralityValue(parentExamples);
        }
        else if (sameClassification(examples)) {
            return new ID3node(examples.get(0).get(goalIndex));  // since all same, just use first
        }
        else if (attributes.isEmpty()) {
            return pluralityValue(examples);
        }
        else {
            String bestAttribute = getBestAttribute(examples);  // importance
            ID3node tree = new ID3node(bestAttribute);
            for (String label : attrToValues.get(bestAttribute)) {
                List<String> remainingAttributes = new ArrayList<String>(attributes);
                remainingAttributes.remove(bestAttribute);
                List<List<String>> exs = getExamplesByAttrValue(bestAttribute, label, examples);
                ID3node subtree = decisionTreeLearning(exs, remainingAttributes, examples);
                subtree.label = label;
                tree.children.add(subtree);
            }
            return tree;
        }
    }

    public List<List<String>> getExamplesByAttrValue(String attribute, String label, List<List<String>> examples) {
        List<List<String>> exs = new ArrayList<List<String>>();
        int index = attributes.indexOf(attribute);
        for (List<String> example : examples) {
            if (example.get(index).equals(label)) {
                exs.add(example);
            }
        }
        return exs;
    }

    public String getBestAttribute(List<List<String>> examples) {
        int p = 0;  // positive examples
        int n = 0;  // negative examples
        for (List<String> example : examples) {
            if (example.get(goalIndex).equals("yes")) { ++p; }
            else if (example.get(goalIndex).equals("no")) { ++n; }
        }
        double b = boolRandVarEntropy((double) p / (p + n));
        double remainder = 0;
        double gain;
        int p_k = 0, n_k = 0;
        String bestAttribute = "";
        double bestGain = -1;
        for (int i = 0; i < attributes.size(); i++) {
            for(String value : attrToValues.get(attributes.get(i))) {
                for (List<String> example : examples) {
                    if (example.get(i).equals(value)) {
                        if (example.get(goalIndex).equals("yes")) { ++p_k; }
                        else if (example.get(goalIndex).equals("no")) { ++n_k; }
                    }
                }
                double addToRemainder = ((double) (p_k + n_k) / (p + n)) * boolRandVarEntropy((double) p_k / (p_k + n_k));
                if (!Double.isNaN(addToRemainder)) {
                    remainder += addToRemainder;
                }
                p_k = 0;
                n_k = 0;
            }
            gain = b - remainder;
            if (gain > bestGain) {
                bestAttribute = attributes.get(i);
                bestGain = gain;
            }
            remainder = 0;
        }
        return bestAttribute;
    }

    static public double boolRandVarEntropy(double q) {
        return -((q * (Math.log(q)/Math.log(2))) + ((1 - q) * (Math.log(1 - q)/Math.log(2))));
    }

    public boolean sameClassification(List<List<String>> examples) {
        String classification = examples.get(0).get(goalIndex);
        for (List<String> example : examples) {
            if (!example.get(goalIndex).equals(classification)) {
                return false;
            }
        }
        return true;
    }

    public ID3node pluralityValue(List<List<String>> examples) {
        int yesCount = 0;
        int noCount = 0;
        for (List<String> example : examples) {
            if (example.get(goalIndex).equals("yes")) {
                yesCount++;
            }
            if (example.get(goalIndex).equals("no")) {
                noCount++;
            }
        }
        if (yesCount > noCount) {
            return new ID3node("yes");
        }
        else if (noCount < yesCount) {
            return new ID3node("no");
        }
        else {
            return (new Random().nextInt(2) == 0) ? new ID3node("yes") : new ID3node("no");
        }
    }

    public MOVE id3Move(Game game) {
        MOVE moves[] = {MOVE.UP, MOVE.DOWN, MOVE.LEFT, MOVE.RIGHT, MOVE.NEUTRAL};
        boolean willMove[] = {false, false, false, false, false};
        MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());
        for(MOVE move : possibleMoves) {
            switch(move) {
                case UP:
                    willMove[0] = getGoalOutput(tree, game, MOVE.UP);
                    break;
                case DOWN:
                    willMove[1] = getGoalOutput(tree, game, MOVE.DOWN);
                    break;
                case LEFT:
                    willMove[2] = getGoalOutput(tree, game, MOVE.LEFT);
                    break;
                case RIGHT:
                    willMove[3] = getGoalOutput(tree, game, MOVE.RIGHT);
                    break;
            }
        }
        MOVE chosenMove = MOVE.NEUTRAL;
        for (int i = 0; i < moves.length; i++) {
            if (willMove[i]) {
                chosenMove = moves[i];
                break;
            }
        }
        return chosenMove;
    }

    public boolean getGoalOutput(ID3node tree, Game game, MOVE move) {
        Game copy = game.copy();
        game.advanceGame(move, ghostController.getMove(copy, -1));
        ID3node currNode = tree;
        while (!currNode.children.isEmpty()) {
            switch(currNode.value) {
                case "ghost_is_near":
                    String ghostIsNear = copy.getNearestGhostDistance(false) < 10 ? "yes" : "no";
                    currNode = getMatchingChild(ghostIsNear, currNode);
                    break;
                case "moved_closer_to_ghost":
                    String closerToGhost = copy.getNearestGhostDistance(false) < game.getNearestGhostDistance(false) ? "yes" : "no";
                    currNode = getMatchingChild(closerToGhost, currNode);
                    break;
                case "moved_closer_to_pill":
                    String closerToPill = copy.getNearestPillDistance() < game.getNearestPillDistance() ? "yes" : "no";
                    currNode = getMatchingChild(closerToPill, currNode);
                    break;
                case "moved_closer_to_power_pill":
                    String closerToPowerPill = copy.getNearestPowerPillDistance() < game.getNearestPowerPillDistance() ? "yes" : "no";
                    currNode = getMatchingChild(closerToPowerPill, currNode);
                    break;
                case "score_increased":
                    String scoreIncreased = copy.getScore() > game.getScore() ? "yes" : "no";
                    currNode = getMatchingChild(scoreIncreased, currNode);
                    break;
                case "was_eaten":
                    String wasEaten = copy.wasPacManEaten() ? "yes" : "no";
                    currNode = getMatchingChild(wasEaten, currNode);
                    break;
                default:
                    break;
            }
        }
        return currNode.value.equals("yes");
    }

    public ID3node getMatchingChild(String label, ID3node parent) {
        for (ID3node child : parent.children) {
            if (child.label.equals(label)) {
                return child;
            }
        }
        return null;
    }

    static public class ID3node {
        
        String label = null;
        String value = null;
        ID3node parent = null;
        List<ID3node> children = new ArrayList<ID3node>();

        public ID3node() {}

        public ID3node(String value) {
            this.value = value;
        }

        public void print() {
            print("", true);
        }

        private void print(String prefix, boolean isTail) {
            System.out.println(prefix + (isTail ? "└── " : "├── ") + label + " : " + value);
            for (int i = 0; i < children.size() - 1; i++) {
                children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
            }
            if (children.size() > 0) {
                children.get(children.size() - 1).print(prefix + (isTail ? "    " : "│   "), true);
            }
        }

    }
}

/*
The following might differ slightly per run due to line 188
(selecting the most common output value among a set of examples, breaking ties RANDOMLY)

Textbook Tree
└── null : patrons
    ├── some : yes
    ├── full : hungry
    │   ├── yes : type
    │   │   ├── french : yes
    │   │   ├── thai : friday
    │   │   │   ├── yes : yes
    │   │   │   └── no : no
    │   │   ├── burger : yes
    │   │   └── italian : no
    │   └── no : no
    └── none : no

Project Tree
└── null : was_eaten
    ├── yes : no
    └── no : moved_closer_to_ghost
        ├── yes : ghost_is_near
        │   ├── yes : no
        │   └── no : moved_closer_to_pill
        │       ├── yes : yes
        │       └── no : moved_closer_to_power_pill
        │           ├── yes : yes
        │           └── no : score_increased
        │               ├── yes : yes
        │               └── no : no
        └── no : yes
 */

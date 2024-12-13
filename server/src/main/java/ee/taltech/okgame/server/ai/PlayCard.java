package ee.taltech.okgame.server.ai;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import java.util.Map;
import java.util.logging.Logger;
import static ee.taltech.okgame.server.GameServer.DECIDER;
import static java.lang.Math.abs;

public class PlayCard extends LeafTask<Blackboard> {

    private static final Logger logger = Logger.getLogger("AI Play Task");
    private Blackboard blackboard;
    @Override
    protected Task<Blackboard> copyTo(Task<Blackboard> task) {
        return task; // Implement copy logic if necessary
    }

    public Status execute() {
        blackboard = getObject();
        AI playerAI = blackboard.getAI(); // Initialize playerAI within the execute method
        int bestCardToPlay = this.getIdOfMostFavorableCard();
        if (bestCardToPlay != 0 && playerAI != null && !playerAI.getRole().equals(DECIDER)) {
            playerAI.setCardToPlay(bestCardToPlay);
            logger.info("\n\n::: [AI Play Task] playing card: " + bestCardToPlay + "\n");
            return Status.SUCCEEDED;
        } else {
            return Status.FAILED; // Return failed status if playerAI is null
        }
    }

    /**
     * Get card to play depending on round
     * @return int cardID
     */
    public int getIdOfMostFavorableCard() {
        blackboard = getObject();
        int round = blackboard.getCurrentRound();
        // Sum of cards on the side where new card is added to
        double currentState = blackboard.getSumOfCardsOnRelevantSide();
        double currentStateOnOtherSide = blackboard.getGameStatePositivity() - currentState;
        double difference = 100;
        double newDifference;
        int bestCard = 0;
        for (Map.Entry<Integer, Double> entry : blackboard.getResultOfParallelTask().entrySet()) {
            Integer cardID = entry.getKey();
            Double newSumPositivity = entry.getValue();
            // Play through with cards given to AI in current round.
            // If last modifier round, AI can use modifiers from 4th round to avoid unlogical choice due to lack of variability
            if (isAllowedInThisRound(cardID, round)) {
                newDifference = getDifferenceByRound(round, currentState, currentStateOnOtherSide, newSumPositivity);
                if (newDifference < difference) {
                    difference = newDifference;
                    bestCard = cardID;
                }
            }
        }
        // Removal is needed in round 5 because all >300 cards are allowed
        blackboard.removeResultFromParallelTask(bestCard);
        return bestCard;
    }

    private boolean isAllowedInThisRound(int cardID, int round) {
        return ((blackboard.getAiCards().contains(cardID)) || ((round > 5) && (cardID > 300)));
    }

    private double getDifferenceByRound(int round, double currentState, double currentStateOnOtherSide, double newSumPositivity) {
        double newDifference = 0;
        if (round == 4) {
            // In round 4 AI plays the worst card on opponents side
            newDifference = newSumPositivity - currentState;
        }
        if (round < 4) {
            // AI wants to make two sides as similar as possible to make decision harder for decider
            // newDifference = abs(newSumPositivity - currentState);
            newDifference = abs(newSumPositivity - currentStateOnOtherSide);
        }
        if (round == 5) {
            // AI wants to play the best card on its own side
            newDifference = currentState - newSumPositivity;
        }
        return newDifference;
    }
}

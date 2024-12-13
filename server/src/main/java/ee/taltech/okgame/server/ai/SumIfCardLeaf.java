package ee.taltech.okgame.server.ai;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import java.util.logging.Logger;

public class SumIfCardLeaf extends LeafTask<Blackboard> {

    @TaskAttribute(required = true)
    private static final Logger logger = Logger.getLogger("AI Parallel Task");
    private final int indexOfCardToCheck;
    public SumIfCardLeaf(int index) {
        this.indexOfCardToCheck = index;
    }

    @Override
    public Task.Status execute() {
        Blackboard blackboard = getObject();
        try {
            logger.info("::: [AI Sum If Task] Leaf " + indexOfCardToCheck);
            int cardToCheck = blackboard.getAiCards().get(indexOfCardToCheck);
            double sumOnTheRelevantSide = blackboard.getSumOfCardsOnRelevantSide();
            int valueOfNewCard = blackboard.getPositivityById(cardToCheck);
            double newSum = sumOnTheRelevantSide + valueOfNewCard;
            blackboard.setResultOfParallelTask(cardToCheck, newSum);
            logger.info("::: [AI Sum If Task] Hmm, If I played card " + cardToCheck
                    + " with value " + valueOfNewCard
                    + " it would change the value from " + sumOnTheRelevantSide
                    + " to " + newSum + "...");
            return Task.Status.SUCCEEDED;
        } catch (Exception e) {
            logger.severe("::: [AI Sum if Task] failed: " + e);
            return Task.Status.FAILED;
        }
    }

    @Override
    protected Task<Blackboard> copyTo(Task<Blackboard> task) {
        return null;
    }
}

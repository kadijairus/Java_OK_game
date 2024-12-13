package ee.taltech.okgame.server.ai;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;

import java.util.Collection;
import java.util.logging.Logger;


public class GetSumPositivity extends LeafTask<Blackboard> {

    @TaskAttribute(required = true)
    private static final Logger logger = Logger.getLogger("AI");

    @Override
    public Task.Status execute() {
        try {
            Blackboard blackboard = getObject();
            double sumOfLeftCards = this.getSumOfLeftCards();
            blackboard.setSumOfLeftCards(sumOfLeftCards);
            double sumOfRightCards = this.getSumOfRightCards();
            blackboard.setSumOfRightCards(sumOfRightCards);
            logger.info("::: [AI Sum Task] Calculated sum of left and right cards: " + sumOfLeftCards + " / " + sumOfRightCards);
            return Task.Status.SUCCEEDED;
        } catch (Exception e) {
            logger.severe("::: [AI Sum Task] failed: " + e);
            return Status.FAILED;
        }
    }

    /**
     * Get average of whole map from blackboard
     */
    public double getSumOfLeftCards() {
        Blackboard blackboard = getObject();
        Collection<Integer> positivities = blackboard.getLeftCardsPositivityMap().values();
        return positivities.stream().reduce(0, Integer::sum);
    }

    public double getSumOfRightCards() {
        Blackboard blackboard = getObject();
        Collection<Integer> positivities = blackboard.getRightCardsPositivityMap().values();
        return positivities.stream().reduce(0, Integer::sum);
    }

    @Override
    protected Task<Blackboard> copyTo(Task<Blackboard> task) {
        return null;
    }
}

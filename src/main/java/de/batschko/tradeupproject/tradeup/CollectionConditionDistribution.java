package de.batschko.tradeupproject.tradeup;

import de.batschko.tradeupproject.enums.Condition;
import lombok.Getter;

import java.util.List;

/**
 * Stores Collection and Condition distribution in two list using the same indexes
 */
@Getter
public class CollectionConditionDistribution {

    List<Integer> collNumber;

    List<Condition> conditionList;


    /**
     * Instantiates a new {@link CollectionConditionDistribution}.
     *
     * @param collNumber    the coll number at index corresponding to the condition list
     * @param conditionList the condition list
     */
    public CollectionConditionDistribution(List<Integer> collNumber, List<Condition> conditionList) {
        this.collNumber = collNumber;
        this.conditionList = conditionList;
    }

    @Override
    public String toString() {
        return "CollectionNumberConditionDistribution{" +
                "numberDistribution=" + collNumber +
                ", conditionList=" + conditionList +
                '}';
    }
}

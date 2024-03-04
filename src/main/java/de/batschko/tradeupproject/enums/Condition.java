package de.batschko.tradeupproject.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum to handle skin conditions.
 */
@Getter
public enum Condition {
    FN("Factory New",0.00, 0.07),
    MW("Minimal Wear",0.07, 0.15),
    FT("Field-Tested",0.15, 0.38),
    WW("Well-Worn",0.38, 0.45),
    BS("Battle-Scarred",0.45, 1.0);

    private final String text;
    private final double floatStart;
    private final double floatEnd;

    Condition(String text, double floatStart, double floatEnd) {
        this.text = text;
        this.floatStart = floatStart;
        this.floatEnd = floatEnd;
    }

    /**
     * Gets {@link Condition} by float value.
     *
     * @param floatValue the float value
     * @return {@link Condition} for float
     */
    public static Condition getConditionByFloat(double floatValue) {
        if (floatValue < 0.07) {
            return Condition.FN;
        } else if (floatValue < 0.15) {
            return Condition.MW;
        } else if (floatValue < 0.38) {
            return Condition.FT;
        } else if (floatValue < 0.45) {
            return Condition.WW;
        } else {
            return Condition.BS;
        }
    }

    private static boolean checkSkinRangeInCondRange(Condition condition, double skinFloatStart, double skinFloatEnd) {
        return (condition.floatStart < skinFloatStart && condition.floatEnd > skinFloatStart) ||
        (condition.floatStart < skinFloatEnd && condition.floatEnd >= skinFloatEnd) ||
        (condition.floatStart >= skinFloatStart && condition.floatEnd <= skinFloatEnd);
    }


    /**
     * Gets possible conditions from float start and end.
     *
     * @param skinFloatStart skin float start
     * @param skinFloatEnd   skin float end
     * @return list of possible {@link Condition}
     */
    public static List<Condition> getPossibleConditions(double skinFloatStart, double skinFloatEnd) {
        List<Condition> possibleConditions = new ArrayList<>();
        for (Condition condition : Condition.values()) {
            if(checkSkinRangeInCondRange(condition, skinFloatStart, skinFloatEnd)) possibleConditions.add(condition);
        }
        return possibleConditions;
    }

    /**
     * Get {@link Condition} array from target condition.
     *
     * @param conditionTarget {@link Condition} condition target
     * @return {@link Condition}[]
     */
    public static Condition[] getConditionArrayFromTarget(Condition conditionTarget){
        switch (conditionTarget) {
            case FN -> {
                return new Condition[]{FN,MW};
            }
            case MW -> {
                return new Condition[]{MW,FT};
            }
            case FT -> {
                return new Condition[]{FT,WW};
            }
            default -> throw new IllegalArgumentException("Unexpected conditionTarget: " + conditionTarget);
        }
    }

}


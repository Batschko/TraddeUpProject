package de.batschko.tradeupproject.enums;

import de.batschko.tradeupproject.db.query.QRStashHolder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


/**
 * Enum to handle rarities
 */
@Getter
public enum Rarity {

    HOWL ("Contraband Rifle"),
    RED ("Covert"),
    PINK ("Classified"),
    PURPLE ("Restricted"),
    BLUE ("Mil-Spec"),
    LIGHT_BLUE ("Industrial Grade"),
    GRAY ("Consumer Grade");

    private final String text;
    Rarity(String text){
        this.text = text;
    }


    /**
     * Validates a rarity string.
     *
     * @param rarityString the rarity string
     * @return the rarity as {@link Rarity}
     */
    public static Rarity validateRarity(String rarityString) {
        for (Rarity rarity : Rarity.values()) {
            if (rarityString.contains(rarity.getText())) {
                return rarity;
            }
        }
        throw new IllegalArgumentException("validateRarity: Invalid rarity string: " + rarityString);
    }


    /**
     * Get a list of possible TradeUp {@link Rarity}s for a case/collection.
     *
     * @param collectionName the collection name
     * @return {@link Rarity} list
     */
    public static List<Rarity> getPossibleTradeUpRarities(String collectionName){
        List<Rarity> enumList = new ArrayList<>();
        Rarity highestRarity = decrease(QRStashHolder.getHighestRarityByCollName(collectionName));
        Rarity lowestRarity = QRStashHolder.getLowestRarityByCollName(collectionName);
        boolean addNext = false;
        for (Rarity rarity : Rarity.values()) {
            if (rarity == highestRarity || addNext) {
                enumList.add(rarity);
                if (rarity == lowestRarity) {
                    break;
                }
                addNext = true;
            }
        }
        return enumList;
    }

    /**
     * Decrease {@link Rarity}.
     *
     * @param rarity {@link Rarity}
     * @return decreased {@link Rarity}
     */
    public static Rarity decrease(Rarity rarity){
        switch (rarity){
            case HOWL, RED -> {
                return PINK;
            }
            case PINK -> {
                return PURPLE;
            }
            case PURPLE -> {
                return BLUE;
            }
            case BLUE -> {
                return LIGHT_BLUE;
            }
            case LIGHT_BLUE -> {
                return GRAY;
            }
            default -> throw new RuntimeException("Error getting decreased Rarity: "+ rarity);
        }
    }

    /**
     * Increase {@link Rarity}.
     *
     * @param rarity {@link Rarity}
     * @return increased {@link Rarity}
     */
    public static Rarity increase(Rarity rarity){
        switch (rarity){
            case PINK -> {
                return RED;
            }
            case PURPLE -> {
                return PINK;
            }
            case BLUE -> {
                return PURPLE;
            }
            case LIGHT_BLUE -> {
                return BLUE;
            }
            case GRAY -> {
                return LIGHT_BLUE;
            }
            default -> throw new RuntimeException("Error getting increased Rarity: "+ rarity);
        }
    }

}

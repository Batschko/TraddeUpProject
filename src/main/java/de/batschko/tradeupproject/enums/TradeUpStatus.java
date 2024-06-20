package de.batschko.tradeupproject.enums;

import lombok.Getter;

/**
 * Enum to set a status for a TradeUp
 */
@Getter
public enum TradeUpStatus {

    NOT_CALCULATED(0),
    CALCULATED (1),
    CLASSIFIED (2),
    WASTED (3),
    ERROR (4),
    CALCULATED_CSMONEY (5);

    private final int status;
    TradeUpStatus(int status){
        this.status = status;
    }

}

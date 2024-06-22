package de.batschko.tradeupproject.enums;


/**
 * Enum to handle skin-price type
 */
@Deprecated
public enum PriceType {

    FN_STAT,
    MW_STAT,
    FT_STAT,
    WW_STAT,
    BS_STAT,
    FN,
    MW,
    FT,
    WW,
    BS;


    /**
     * Gets price type for given condition and stattrak.
     *
     * @param condition condition {@link Condition}
     * @param stattrak  stattrak as byte
     * @return {@link PriceType}
     */
    public static PriceType getPriceType(Condition condition, byte stattrak) {
        switch (condition) {
            case FN -> {
                if (stattrak == 1) {
                    return FN_STAT;
                } else return FN;
            }
            case MW -> {
                if (stattrak == 1) {
                    return MW_STAT;
                } else return MW;
            }
            case FT -> {
                if (stattrak == 1) {
                    return FT_STAT;
                } else return FT;
            }
            case WW -> {
                if (stattrak == 1) {
                    return WW_STAT;
                } else return WW;
            }
            case BS -> {
                if (stattrak == 1) {
                    return BS_STAT;
                } else return BS;
            }
            default ->
                    throw new IllegalArgumentException("Unexpected Enum enums.PriceType parametes: " + condition + " " + stattrak);
        }
    }
}



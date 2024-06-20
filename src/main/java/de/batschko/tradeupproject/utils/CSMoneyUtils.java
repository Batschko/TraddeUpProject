package de.batschko.tradeupproject.utils;

import de.batschko.tradeupproject.db.query.QRUtils;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.webfetchers.CSMoneyScraper;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Utility methods for CSMoney
 */
public class CSMoneyUtils {

    /**
     * Gets cs money url filtered for TradeUp-Skins.
     *
     * @param tupId the TradeUp id
     */
    public static String getCSMoneyUrlFiltered(int tupId) {
        List<SkinUtils.TradeUpSkinInfo> skinList = QRUtils.getTradeUpSkinsInfo(tupId);
        StringBuilder sb = new StringBuilder();
        String baseUrl = ("https://cs.money/csgo/trade/");
        //TODO why for loop???
        //TODO why for loop???
        //TODO why for loop???
        //TODO why for loop???
        for (SkinUtils.TradeUpSkinInfo skin : skinList) {
            sb.setLength(0);
            Rarity rarity = skin.getRarity();
            byte stat = skin.getStattrak();
            String coll = skin.getColl_name();
            coll = coll.replace("Case", "Collection");
            sb.append("?sort=price&order=desc");
            sb.append("&rarity=").append(rarity.getText());
            sb.append("&isStatTrak=").append(stat == 1);
            sb.append("&collection=").append("The ").append(coll);

            String encoded = sb.toString().replace(" ", "+");

            try {
                String uri = String.valueOf(new URI(baseUrl + encoded).toURL());
                return uri;
            } catch (MalformedURLException | URISyntaxException e) {
                throw new RuntimeException(e);
            }

        }
        return "";
    }





}

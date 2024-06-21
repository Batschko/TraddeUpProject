package de.batschko.tradeupproject.utils;

import de.batschko.tradeupproject.db.query.QRUtils;
import de.batschko.tradeupproject.enums.Rarity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;

/**
 * Utility methods for CSMoney
 */
public class CSMoneyUtils {

    /**TODO only working for one collection
     *
     * Gets cs money url filtered for TradeUp-Skins.
     *
     * @param tupId the TradeUp id
     */
    public static String getCSMoneyUrlFiltered(int tupId) {
        final String baseUrl = ("https://cs.money/csgo/trade/");
        List<SkinUtils.TradeUpSkinInfo> infoList = QRUtils.getTradeUpSkinsInfo(tupId);

        StringBuilder sb = new StringBuilder();
        Rarity rarity = infoList.getFirst().getRarity();
        byte stat = infoList.getFirst().getStattrak();
        sb.append("?sort=price&order=desc");
        sb.append("&rarity=").append(rarity.getText());
        sb.append("&isStatTrak=").append(stat == 1);

        HashSet<String> collSet = new HashSet<>();
        for (SkinUtils.TradeUpSkinInfo skin : infoList) {
            String coll = skin.getColl_name();
            if(!collSet.add(coll)){
                continue;
            }
            coll = coll.replace("Case", "Collection");
            sb.append("&collection=").append("The ").append(coll);

        }
        String encoded = sb.toString().replace(" ", "+");

        try {
            return String.valueOf(new URI(baseUrl + encoded).toURL());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }





}

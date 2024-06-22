package de.batschko.tradeupproject.api;

import de.batschko.tradeupproject.db.query.QRCS2Skin;
import de.batschko.tradeupproject.db.query.api.QRTradeUpTable;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.TradeUpMade;
import de.batschko.tradeupproject.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing TableTradeUps.
 *
 * <p>All routes start with <code>/api/tup</code>.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/tup")
public class TradeUpController {

    /**
     * Get TableTradeUps.
     *
     * @return {@link JSONArray} TableTradeUps
     */
    @GetMapping(value = "/", produces = "application/json" )
    public String tup() {
        log.info("sending TradeUps");
        return ApiUtils.tradeupResultToJsonArray(QRTradeUpTable.getTradeUps(false)).toString();
    }

    /**
     * Get marked TableTradeUps.
     *
     * @return marked TableTradeUps
     */
    @GetMapping(value = "/marked", produces = "application/json" )
    public String tupMarked() {
        log.info("sending TradeUpsMarked");
        return ApiUtils.tradeupResultToJsonArray(QRTradeUpTable.getTradeUpsMarked(false)).toString();
    }

    /**
     * Patch un-/mark a TradeUp.
     *
     * @return {@link JSONArray} marked TableTradeUps
     */
    @PatchMapping(value = "/marked/{tupId}", produces = "application/json" )
    public String tupMarkedPatch(@PathVariable int tupId) {
        log.info("marking TradeUp");
        log.info("sending TradeUps");
        QRTradeUpTable.toggleMarkTradeUp(tupId, false);
        return ApiUtils.tradeupResultToJsonArray(QRTradeUpTable.getTradeUps(false)).toString();
    }

    /**
     * Get watched TableTradeUps.
     *
     * @return {@link JSONArray} watched TableTradeUps
     */
    @GetMapping(value = "/watched", produces = "application/json" )
    public String tupWatched() {
        log.info("sending TradeUpsWatched");
        return ApiUtils.tradeupResultToJsonArray(QRTradeUpTable.getTradeUpsWatched(false)).toString();
    }

    /**
     * Patch watched TableTradeUps.
     *
     * @return {@link JSONArray} marked TableTradeUps
     */
    @PatchMapping(value = "/watched/{tupId}", produces = "application/json" )
    public String tupWatchPatch(@PathVariable int tupId) {
        log.info("marking TradeUp as watched");
        log.info("sending TradeUps");
        QRTradeUpTable.toggleWatch(tupId, false);
        return ApiUtils.tradeupResultToJsonArray(QRTradeUpTable.getTradeUpsMarked(false)).toString();
    }

    /**
     * Get active TableTradeUps.
     *
     * @return {@link JSONArray} active TableTradeUps
     */
    @GetMapping(value = "/active", produces = "application/json" )
    public String tupActive() {
        log.info("sending TradeUpsActive");
        return ApiUtils.tradeupResultToJsonArray(QRTradeUpTable.getTradeUpsActive(false)).toString();
    }

    /**
     * Patch active TableTradeUps.
     *
     * @return {@link JSONArray} watched TableTradeUps
     */
    @PatchMapping(value = "/active/{tupId}", produces = "application/json" )
    public String tupActivePatch(@PathVariable int tupId) {
        log.info("marking TradeUp active");
        log.info("sending TradeUpsWatched");
        QRTradeUpTable.toggleActive(tupId, false);
        return ApiUtils.tradeupResultToJsonArray(QRTradeUpTable.getTradeUpsWatched(false)).toString();
    }

    /**
     * Get made TableTradeUps.
     *
     * @return {@link JSONArray} made TableTradeUps  {@link JSONObject} -> "tups": jsonArray(TradeUps), "profit": profit
     */
    @GetMapping(value = "/made", produces = "application/json" )
    public String tupMade() {
        return QRTradeUpTable.getTradeUpsMade().toString();
    }

    /**
     * Get grouped made TableTradeUps.
     *
     * @return {@link JSONArray} made TableTradeUps  {@link JSONObject} ->  madeTup id, tup id, stat, rarity, gsettings, sum profit, made count, avg profi
     */
    @GetMapping(value = "/made/grouped", produces = "application/json" )
    public String tupMadeGrouped() {
        return QRTradeUpTable.getTradeUpsMadeGrouped().toString();
    }

    /**
     * Post/make a {@link TradeUpMade}.
     *
     * @return saved for success
     */
    @PostMapping("/make")
    public String tupMake(@RequestBody String body) {
        JSONObject json;
        try {
            json = new JSONObject(new JSONTokener(body));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String[] nameToken = json.getString("name").split(" \\| ");
        if(nameToken.length!=2)throw new RuntimeException();
        int tradeUpId = json.getInt("tupId");
        Condition cond = Condition.valueOf(json.getString("cond"));
        int skinId = QRCS2Skin.getOutSkinIdByNameCondTup(nameToken[0], nameToken[1], cond, tradeUpId, false);
        byte stattrak = (byte) json.getInt("stat");
        Rarity rarity = Rarity.valueOf(json.getString("rarity")) ;
        int floatDictId = json.getInt("floatDictId");
        String gsettings = json.getString("gsettings");
        double cost = json.getDouble("cost");
        double price = json.getDouble("price");

        int saved = QRTradeUpTable.saveMadeTradeUp((byte) 0,tradeUpId, stattrak, rarity, floatDictId, gsettings, skinId, (String) json.get("name"), cond, cost, price);
        if(saved !=1) throw new RuntimeException();

        return "saved";
    }
}

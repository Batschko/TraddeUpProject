package de.batschko.tradeupproject.api;

import de.batschko.tradeupproject.db.query.QRCS2Skin;
import de.batschko.tradeupproject.tables.TradeUpOutcomeSkins;
import de.batschko.tradeupproject.tables.TradeUpSkins;
import de.batschko.tradeupproject.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Skins.
 *
 * <p>All routes start with <code>/api/skins</code>.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/skins")
public class SkinsController {

    /**
     * Get TradeUpSkins.
     *
     * @return {@link JSONArray} of {@link TradeUpSkins}s as {@link JSONObject}s.
     */
    @GetMapping(value = "/tupskins/{tupId}", produces = "application/json" )
    public String tupSkins(@PathVariable int tupId) {
        log.info("sending TradeUpSkins");
        Result<Record> result = QRCS2Skin.getTradeUpSkins(tupId, (byte) 0);
        return ApiUtils.skinResultToJsonArray(result).toString();
    }

    /**
     * Get custom TradeUpSkins.
     *
     * @return {@link JSONArray} of {@link TradeUpSkins}s as {@link JSONObject}s.
     */
    @GetMapping(value = "/tupskins/custom/{tupId}", produces = "application/json" )
    public String tupSkinsCustom(@PathVariable int tupId) {
        log.info("sending TradeUpSkins");
        Result<Record> result = QRCS2Skin.getTradeUpSkins(tupId, (byte) 1);
        return ApiUtils.skinResultToJsonArray(result).toString();
    }

    /**
     * Get TradeUpOutcomeSkins.
     *
     * @return {@link JSONArray} of {@link TradeUpOutcomeSkins}s as {@link JSONObject}s.
     */
    @GetMapping(value = "/outskins/{tupId}", produces = "application/json" )
    public String outSkins(@PathVariable int tupId) {
        log.info("sending OutSkins");
        Result<Record> result = QRCS2Skin.getOutSkins(tupId, (byte) 0);
        return ApiUtils.skinResultToJsonArray(result).toString();
    }

    /**
     * Get custom TradeUpOutcomeSkins.
     *
     * @return {@link JSONArray} of {@link TradeUpOutcomeSkins}s as {@link JSONObject}s.
     */
    @GetMapping(value = "/outskins/custom/{tupId}", produces = "application/json" )
    public String outSkinsCustom(@PathVariable int tupId) {
        log.info("sending OutSkins");
        Result<Record> result = QRCS2Skin.getOutSkins(tupId, (byte) 1);
        return ApiUtils.skinResultToJsonArray(result).toString();
    }
}

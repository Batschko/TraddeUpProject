package de.batschko.tradeupproject.api;

import de.batschko.tradeupproject.db.query.QRCS2Skin;
import de.batschko.tradeupproject.utils.ApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SkinsController {

    @GetMapping(value = "/api/skins/tupskins/{tupId}", produces = "application/json" )
    public String tupSkins(@PathVariable int tupId) {
        log.info("sending TradeUpSkins");
        Result<Record> result = QRCS2Skin.getTradeUpSkins(tupId);
        return ApiUtils.skinResultToJsonArray(result).toString();
    }

    @GetMapping(value = "/api/skins/outskins/{tupId}", produces = "application/json" )
    public String outSkins(@PathVariable int tupId) {
        log.info("sending OutSkins");
        Result<Record> result = QRCS2Skin.getOutSkins(tupId);
        return ApiUtils.skinResultToJsonArray(result).toString();
    }
}

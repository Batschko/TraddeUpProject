package de.batschko.tradeupproject.api;

import de.batschko.tradeupproject.db.query.QRCollection;
import de.batschko.tradeupproject.tables.TradeUp;
import de.batschko.tradeupproject.tradeup.CustomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for routes with no specific controller.
 *
 */
@Slf4j
@RestController
public class DefaultController {

    /**
     * Get all Collection names.
     *
     * @return {@link JSONArray} of collection names.
     */
    @GetMapping(value = "/api/collections", produces = "application/json" )
    public List<String> collections() {
        return QRCollection.getAllCollectionNames();
    }

    /**
     * Post calculate a custom {@link TradeUp}.
     * <p>save to db is optional</p>
     *
     * @return {@link JSONObject} calculated TradeUp data.
     */
    @PostMapping("/api/calculator")
    public String calculator(@RequestBody String body) {
        JSONObject json;
        try {
            json = new JSONObject(new JSONTokener(body));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JSONObject jsonObject = CustomGenerator.calculateTup(json, json.getBoolean("saveTup"));

        return jsonObject.toString();
    }
}
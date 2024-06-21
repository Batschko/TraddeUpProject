package de.batschko.tradeupproject.api;

import de.batschko.tradeupproject.db.query.QRCollection;
import de.batschko.tradeupproject.tradeup.CustomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class DefaultController {
    @GetMapping(value = "/api/collections", produces = "application/json" )
    public List<String> collections() {
        return QRCollection.getAllCollectionNames();
    }


    @PostMapping("/api/calculator")
    public String calculator(@RequestBody String body) {
        JSONObject json;
        try {
            json = new JSONObject(new JSONTokener(body));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(json);
        JSONObject jsonObject = CustomGenerator.calculateTup(json);


        return jsonObject.toString();
    }
}
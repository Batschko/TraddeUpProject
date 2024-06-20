package de.batschko.tradeupproject.utils;

import org.jooq.Record;
import org.jooq.Result;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class ApiUtils {
    public static JSONArray tradeupResultToJsonArray(Result<Record> result){
        //field name , data
        List<Map<String, Object>> maps = result.intoMaps();
        // Convert list of maps to JSON array
        JSONArray jsonArray = new JSONArray();
        for (Map<String, Object> map : maps) {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
            jsonObject.put("condition_target", jsonObject.get("condition_target").toString());
            jsonObject.put("rarity", jsonObject.get("rarity").toString());
            jsonObject.put("status", jsonObject.get("status").toString());
            jsonObject.put("modified_date", jsonObject.get("modified_date").toString());
            jsonArray.put(jsonObject);
        }

        return jsonArray;
    }

    public static JSONArray skinResultToJsonArray(Result<Record> result){
        //field name , data
        List<Map<String, Object>> maps = result.intoMaps();
        // Convert list of maps to JSON array
        JSONArray jsonArray = new JSONArray();
        for (Map<String, Object> map : maps) {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
            jsonObject.put("condition", jsonObject.get("condition").toString());
            jsonObject.put("rarity", jsonObject.get("rarity").toString());
            jsonObject.put("modified_date", jsonObject.get("modified_date").toString());
            jsonArray.put(jsonObject);
        }

        return jsonArray;
    }


}

package de.batschko.tradeupproject.webfetchers;

import de.batschko.tradeupproject.db.customtable.StashSkinHolderCustom;
import de.batschko.tradeupproject.db.query.QRCollection;
import de.batschko.tradeupproject.enums.CaseCollection;
import de.batschko.tradeupproject.enums.Rarity;
import de.batschko.tradeupproject.tables.records.StashSkinHolderRecord;
import de.batschko.tradeupproject.utils.SkinUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jsoup Scraper for CSGOStash data
 */
@Slf4j
public class StashScraper {


    private static final String csgoStashUrl = "https://csgostash.com/";

    /**
     * Converts the given string to an integer.
     * <p>This method is used to improve code readability in other methods, as it encapsulates
     * the try-catch block for parsing a string to an integer.</p
     * @param s The string to be converted to an integer.
     * @return The integer value represented by the input string.
     * @throws NumberFormatException if the input string does not contain a parsable integer.
     */
    private static int stringToInt(String s){
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("stringToInt got string "+"s \n"+e);
        }
    }
    /**
     * Converts the given string to a float.
     * <p>This method is used to improve code readability in other methods, as it encapsulates
     * the try-catch block for parsing a string to a float.</p
     * @param s The string to be converted to a float.
     * @return The integer value represented by the input string.
     * @throws NumberFormatException if the input string does not contain a parsable float.
     */
    private static double stringToDouble(String s){
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("stringToFloat got string "+"s \n"+e);
        }
    }


    /**
     * Renames the titles of StashSkinHolderRecords containing special characters
     * based on a predefined mapping.
     * <p>
     * This method iterates over the provided list of StashSkinHolderRecords and updates
     * the titles according to the mapping defined in {@code specialCharsMapping}.
     * Only titles found in the mapping will be updated.
     *
     * @param holderList The list of StashSkinHolderRecords to process.
     */
    private static void renameStashSkinHolderWithSpecialChars(List<StashSkinHolderRecord> holderList){
        Map<String, String> specialCharsMapping = SkinUtils.getSpecialSkinNamesMap();

        for (StashSkinHolderRecord item : holderList) {
            // only update if item.title is in map
            String newTitle = specialCharsMapping.get(item.getTitle());
            if (newTitle != null) {
                item.setTitle(newTitle);
            }
        }
    }

    /***
     * Generates map of case/coll name with corresponding urls
     * @param cc The {@link CaseCollection} specifying whether to generate map of cases or collections.
     * @return A map where the keys are case/collection names with the corresponding urls as values
     */
    public static Map<String,String> getCaseCollectionNamesAndUrls(CaseCollection cc){
        Document document;
        Elements caseColletion;
        Map<String,String> nameUrlMap = new HashMap<>();
        try {
            document = Jsoup.connect(csgoStashUrl).get();
        } catch (IOException e) {
            throw new RuntimeException("getCasesOrCollsWithItemUrls failed to load base url \n "+e);
        }
        switch (cc) {
            case CASE -> caseColletion = document.select("#navbar-expandable > ul > li:nth-child(7)").select("a[href]");
            case COLLECTION ->
                    caseColletion = document.select("#navbar-expandable > ul > li:nth-child(8)").select("a[href]");
            default -> throw new IllegalArgumentException("Unexpected Enum enums.CaseCollection value: " + cc);
        }
        //throw away first element (header)
        caseColletion.removeFirst();
        //throw away 3 containers (souvenier package etc.) only for cases
        if(cc==CaseCollection.CASE){
            caseColletion.removeLast();
            caseColletion.removeLast();
            caseColletion.removeLast();
        }

        for(Element element: caseColletion){
            String url = element.attr("href");
            String name = element.text();

            if(cc==CaseCollection.CASE){
                if(url.contains("/containers/")) continue;
            }
            nameUrlMap.put(name, url);
        }
        return nameUrlMap;
    }

    /**
     * Get Case or Collection item urls as Map &lt;Integer(cc id), List&lt;String(item url)&gt;&gt;
     * @return A map where the keys are case/collection IDs and the values are Lists of item URLs associated with each case/collection.
     * @throws RuntimeException If an error occurs while fetching or processing the case collections.
     */
    private static Map<Integer, List<String>> getCasesOrCollsWithItemUrls(CaseCollection cc){
        Map<String,String> ccNameAndUrls = getCaseCollectionNamesAndUrls(cc);
        Map<Integer, List<String>> resultMap = new HashMap<>();
        Document doc;

        for(Map.Entry<String, String> entry: ccNameAndUrls.entrySet()){
            String url = entry.getValue();
            String name = entry.getKey();
            int ccId = QRCollection.getCollectionId(name);

            //TODO remocve
            if (url.equals("#")){
                continue;
            }
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e) {
                throw new RuntimeException("getCasesOrCollsWithItemUrls error loading url doc"+url+"\n -->"+e);
            }
            Elements skin_urls = doc.select("a[href^=\"https://csgostash.com/skin/\"]");

            //LinkedHashSet to store only unique links and keep order for Skin.top variable
            LinkedHashSet<String> uniqueLinks = new LinkedHashSet<>();
            for (Element link : skin_urls) {
                String href = link.attr("href");
                uniqueLinks.add(href);
            }
            List<String> linksList = new ArrayList<>(uniqueLinks);
            resultMap.put(ccId, linksList);

        }
        return resultMap;
    }



    /**
     * Wrapper for {@link #generateStashSkinHolderList(CaseCollection)}
     * <p>Generates a list of all {@link StashSkinHolderRecord} for cases and collections.</p>
     *
     * @return A list of {@link StashSkinHolderRecord} objects for cases and collections.
     */
    public static List<StashSkinHolderRecord> generateStashSkinHolderListAll(){
        log.info("Generating StashSkinHolder....");
        List<StashSkinHolderRecord> all_holders = new ArrayList<>(generateStashSkinHolderList(CaseCollection.CASE));
        all_holders.addAll(generateStashSkinHolderList(CaseCollection.COLLECTION));
        log.info("Generated "+all_holders.size()+" StashSkinHolder");
        return all_holders;
    }

    /**
     * Generates a list of {@link StashSkinHolderRecord} objects based on the specified {@link CaseCollection} (case or collection).
     *
     * @param cc The {@link CaseCollection} specifying whether to generate list of {@link StashSkinHolderRecord} for cases or collections.
     * @return A list of {@link StashSkinHolderRecord} objects based on the specified {@link CaseCollection}.
     */
    public static List<StashSkinHolderRecord> generateStashSkinHolderList(CaseCollection cc){
        List<StashSkinHolderRecord> holderList = new ArrayList<>();
        Map<Integer,List<String>> caseCollUrls = getCasesOrCollsWithItemUrls(cc);

        Document doc;
        for (Integer collId : caseCollUrls.keySet()) {
            List<String> urls = caseCollUrls.get(collId);
            try {
                doc = Jsoup.connect(urls.getFirst()).get();
            } catch (IOException e) {
                throw new RuntimeException("getCasesOrCollsWithItemUrls error loading url doc"+urls.getFirst()+"\n -->"+e);
            }
            Rarity max_rarity = Rarity.validateRarity(doc.select("div.well.result-box.nomargin a > div > p").first().text().trim());

            // Iterate over the elements of the url list
            for (String url : urls) {
                StashSkinHolderRecord holder = generateStashSkinHolder(collId, url);
                if(holder.getRarity() == max_rarity) holder.setTop((byte)1);
                else holder.setTop((byte)0);
                holderList.add(holder);
                log.debug("Generated Stasholder with id {}", holder.getStashId());
            }
            //min rarity
            holderList.getLast().setBottom((byte) 1);
        }
        renameStashSkinHolderWithSpecialChars(holderList);
        return holderList;
    }


    /**
     * Generate {@link StashSkinHolderRecord}
     *
     * @param caseCollId the caseCollection id
     * @param url        the stash holder url
     * @return {@link StashSkinHolderRecord}
     */
    public static StashSkinHolderRecord generateStashSkinHolder(int caseCollId, String url){
        Matcher matcher = Pattern.compile("/(\\d+)/").matcher(url);
        int stashId;
        if (matcher.find()) {
            stashId =  stringToInt(matcher.group(1));
        } else {
            throw new IllegalArgumentException("No stash ID found in URL: " + url);
        }
        Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new RuntimeException("generateStashSkinHolder "+e);
        }

        double floatMin = (stringToDouble(doc.select("div[title='Minimum Wear (\"Best\")']").first().text()));
        double floatMax = (stringToDouble(doc.select("div[title='Maximum Wear (\"Worst\")']").first().text()));

        // Find the item box
        Element itemBox = doc.select("div.well.result-box.nomargin").first();
        // Extract weapon and title
        String weapon = itemBox.select("h2 > a:nth-child(1)").text().trim();
        String title = itemBox.select("h2 > a:nth-child(2)").text().trim();
        Rarity rarity = Rarity.validateRarity(itemBox.select("a > div > p").first().text().trim());
        String image_url;
        try {
            image_url = itemBox.select("a > img").first().attr("src");
        } catch (NullPointerException e) {
            try {
                image_url = itemBox.select("img").first().attr("src");
            } catch (NullPointerException e2) {
                throw new RuntimeException("generateStashSkinHolder image: " + e2.getMessage());
            }
        }
        return new StashSkinHolderCustom(stashId,caseCollId,weapon,title,rarity,floatMin,floatMax,image_url);
    }

}


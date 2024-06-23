package de.batschko.tradeupproject;

import de.batschko.tradeupproject.db.query.QRInitQueries;
import de.batschko.tradeupproject.db.query.QRTradeUpGenerated;
import de.batschko.tradeupproject.tradeup.Generator;
import de.batschko.tradeupproject.utils.CSMoneyUtils;
import de.batschko.tradeupproject.utils.SkinUtils;
import de.batschko.tradeupproject.utils.TradeUpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;


/**
 * The type Trade up project application cmd.
 */
@SpringBootApplication
@Slf4j
public class TradeUpProjectApplicationCMD {

	static String lastCmdName = "";

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(TradeUpProjectApplicationCMD.class, args);

		runMainMenu();

		int exitCode = SpringApplication.exit(ctx, () -> 0);

		System.exit(exitCode);
    }



	static void printCmdNames(Map<String,String> cmdNames){
		for(Map.Entry<String,String> entry : cmdNames.entrySet()){
			System.out.println(entry.getKey()+" - "+entry.getValue());
		}
	}



	static void runMainMenu() {
		Map<String,String> cmdListNames = new LinkedHashMap<>();
		cmdListNames.put("h", "Help");
		cmdListNames.put("q", "go back");
		cmdListNames.put("1", "TradeUp generation menu");
		cmdListNames.put("2", "CSMoney menu");
		Map<String, Runnable> cmdList = new LinkedHashMap<>(Map.of(
				"h", () -> printCmdNames(cmdListNames),
				"q", System.out::println,
				"1", TradeUpProjectApplicationCMD::runGenerationMenu,
				"2", TradeUpProjectApplicationCMD::runCSMoneyMenu
		));



		Scanner in = new Scanner(System.in);
		String input = "";
		while (!input.equals("q")) {
			System.out.println("Last action: "+lastCmdName);
			printCmdNames(cmdListNames);
			System.out.print(">> ");
			input = in.nextLine();
			if (!cmdList.containsKey(input)) {
				System.out.print("No such command.");
				System.out.println(cmdList);
			} else {
				lastCmdName = cmdListNames.get(input);
				cmdList.get(input).run();
			}
		}
	}


	static void runGenerationMenu(){
		Map<String,String> cmdListNames = new LinkedHashMap<>();
		cmdListNames.put("h", "Help");
		cmdListNames.put("q", "go back");
		cmdListNames.put("1", "loadInitialCaseCollection");
		cmdListNames.put("2", "saveStashHolderToDatabase");
		cmdListNames.put("3", "saveCS2SkinToDatabase");
		cmdListNames.put("4", "priceUpdateByDate");
		cmdListNames.put("5", "priceUpdateMissing");
		cmdListNames.put("6", "floatId 2 generateSingleCollTradeups");
		cmdListNames.put("7", "floatId 4 generateSingleCollTradeups");
		cmdListNames.put("8", "false createTradeUpSkins");
	//	cmdListNames.put("9", "true createTradeUpSkins");
		cmdListNames.put("0", "calculateAllTradeUps");
		Map<String, Runnable> cmdList = new HashMap<>();
		cmdList.put("h", () -> printCmdNames(cmdListNames));
		cmdList.put("q", () -> System.out.println());
		cmdList.put("1", QRInitQueries::loadInitialCaseCollection);
		cmdList.put("2", QRInitQueries::generateAndSaveStashHolderToDatabase);
		cmdList.put("3", QRInitQueries::generateAndSaveCS2SkinToDatabase);
		cmdList.put("4", SkinUtils::priceUpdateByDate);
		cmdList.put("5", SkinUtils::priceUpdateMissing);
		cmdList.put("6", () -> Generator.generateSingleCollTradeUps(2));
		cmdList.put("7", () -> Generator.generateSingleCollTradeUps(4));
		cmdList.put("8", QRTradeUpGenerated::createTradeUpSkins);
		//cmdList.put("9", () -> QRTradeUp.createTradeUpSkins(true));
		cmdList.put("0", TradeUpUtils::calculateAllTradeUps);


		// cmdList.put("0", CSGOBackpackApi::calculateAllTradeUpsBackpackPrice);
		// cmdList.put("4", () -> CSGOBackpackApi.setSkinPricesSpecialChars(21, false));
		// cmdList.put("5", () -> CSGOBackpackApi.setSkinPrices(Integer.MAX_VALUE,21, false));

		Scanner in = new Scanner(System.in);
		String input = "";
		while (!input.equals("q")) {
			System.out.println("Last action: "+lastCmdName);
			printCmdNames(cmdListNames);
			System.out.print(">> ");
			input = in.nextLine();
			if (!cmdList.containsKey(input)) {
				System.out.print("No such command.");
				System.out.println(cmdList);
			} else {
				lastCmdName = cmdListNames.get(input);
				cmdList.get(input).run();
			}
		}

	}



	static void runCSMoneyMenu(){
		Map<String,String> cmdListNames = new LinkedHashMap<>();
		cmdListNames.put("h", "Help");
		cmdListNames.put("q", "go back");
		cmdListNames.put("1", "runCSMoneyFilteredUrlMenu");

		Map<String, Runnable> cmdList = new HashMap<>();
		cmdList.put("h", () -> printCmdNames(cmdListNames));
		cmdList.put("q", System.out::println);
		cmdList.put("1", TradeUpProjectApplicationCMD::runCSMoneyFilteredUrlMenu);
		Scanner in = new Scanner(System.in);
		String input = "";
		while (!input.equals("q")) {
			System.out.println("Last action: "+lastCmdName);
			printCmdNames(cmdListNames);
			System.out.print(">> ");
			input = in.nextLine();
			if (!cmdList.containsKey(input)) {
				System.out.print("No such command.");
				System.out.println(cmdList);
			} else {
				lastCmdName = cmdListNames.get(input);
				cmdList.get(input).run();
			}
		}
	}

	static void runCSMoneyFilteredUrlMenu(){
		int tupId = -1;
		Scanner in = new Scanner(System.in);
		System.out.print("TradeUp id: ");
		tupId = in.nextInt();
		CSMoneyUtils.getCSMoneyUrlFiltered(tupId);
	}

}

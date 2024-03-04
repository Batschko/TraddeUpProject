package de.batschko.tradeupproject;

import de.batschko.tradeupproject.db.query.QRGenerationSettings;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.tradeup.CollectionConditionDistribution;
import de.batschko.tradeupproject.tradeup.TradeUpSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@SpringBootApplication
@Slf4j
public class TradeUpProjectApplication {



	public static void main(String[] args) {
		SpringApplication.run(TradeUpProjectApplication.class, args);

	}





}

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP TABLE IF EXISTS `v_full_tradeup`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_full_tradeup` AS select `t`.`id` AS `id`,`t`.`modified_date` AS `modified_date`,`t`.`stattrak` AS `stattrak`,`t`.`rarity` AS `rarity`,`t`.`condition_target` AS `condition_target`,`t`.`collection_count` AS `collection_count`,`t`.`status` AS `status`,`t`.`negative_loss` AS `negative_loss`,`t`.`float_sum_needed` AS `float_sum_needed`,`t`.`float_dict_id` AS `float_dict_id`,`t`.`classification_id` AS `classification_id`,`t`.`generation_settings_id` AS `generation_settings_id`,`o`.`tradeup_id` AS `tradeup_id`,`o`.`cost` AS `cost`,`o`.`skin_min` AS `skin_min`,`o`.`skin_max` AS `skin_max`,`o`.`skin_avg` AS `skin_avg`,`o`.`loss` AS `loss`,`o`.`value` AS `value`,`o`.`chance_value` AS `chance_value`,`o`.`repeat_factor` AS `repeat_factor`,`o`.`repeat_factor_chance` AS `repeat_factor_chance`,`o`.`hit_chance` AS `hit_chance`,`o`.`float_marker` AS `float_marker`,`o`.`outcome` AS `outcome`,`o`.`amount_sold_avg` AS `amount_sold_avg`,`o`.`category_marker` AS `category_marker`,`g`.`settings` AS `settings` from ((`trade_up` `t` join `trade_up_outcome` `o` on(`t`.`id` = `o`.`tradeup_id`)) join `generation_settings` `g` on(`t`.`generation_settings_id` = `g`.`id`)) ;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

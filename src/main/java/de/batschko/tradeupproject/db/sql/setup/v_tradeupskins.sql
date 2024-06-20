/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP TABLE IF EXISTS `v_tradeupskins`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_tradeupskins` AS select `a`.`trade_up_id` AS `trade_up_id`,`a`.`c_s2_skin_id` AS `c_s2_skin_id`,`b`.`id` AS `id`,`b`.`stash_id` AS `stash_id`,`b`.`collection_id` AS `collection_id`,`b`.`modified_date` AS `modified_date`,`b`.`coll_name` AS `coll_name`,`b`.`rarity` AS `rarity`,`b`.`stattrak` AS `stattrak`,`b`.`weapon` AS `weapon`,`b`.`title` AS `title`,`b`.`condition` AS `condition`,`b`.`float_start` AS `float_start`,`b`.`float_end` AS `float_end`,`b`.`image_url` AS `image_url`,`b`.`top` AS `top`,`b`.`skin_price_id` AS `skin_price_id`,`b`.`price_type` AS `price_type`,`b`.`price` AS `price`,`b`.`amount_sold` AS `amount_sold` from (`test_db_2`.`trade_up_skins` `a` join `test_db_2`.`v_fullcs2skin` `b` on(`a`.`c_s2_skin_id` = `b`.`id`)) ;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

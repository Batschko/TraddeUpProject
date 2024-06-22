/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP TABLE IF EXISTS `v_fullcs2skin`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_fullcs2skin` AS SELECT `t3`.`id` AS `id`,`t1`.`stash_id` AS `stash_id`,`t1`.`collection_id` AS `collection_id`,`t3`.`modified_date` AS `modified_date`,`t2`.`coll_name` AS `coll_name`,`t1`.`rarity` AS `rarity`,`t3`.`stattrak` AS `stattrak`,`t1`.`weapon` AS `weapon`,`t1`.`title` AS `title`,`t3`.`condition` AS `condition`,`t1`.`float_start` AS `float_start`,`t1`.`float_end` AS `float_end`,`t1`.`image_url` AS `image_url`,`t1`.`top` AS `top`,`t3`.`price` AS `price`
FROM ((`stash_skin_holder` `t1`
JOIN `collection` `t2` ON(`t1`.`collection_id` = `t2`.`id`))
JOIN `c_s2_skin` `t3` ON(`t1`.`stash_id` = `t3`.`stash_id`)) ;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

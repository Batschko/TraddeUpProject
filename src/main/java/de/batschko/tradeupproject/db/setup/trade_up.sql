/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE TABLE IF NOT EXISTS `trade_up` (
  `custom` tinyint(4) NOT NULL,
  `id` int(11) NOT NULL,
  `modified_date` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `stattrak` tinyint(1) NOT NULL,
  `rarity` varchar(32) NOT NULL,
  `condition_target` varchar(20) NOT NULL,
  `collection_count` tinyint(1) NOT NULL,
  `status` tinyint(11) NOT NULL,
  `float_sum_needed` float DEFAULT NULL,
  `float_dict_id` int(11) NOT NULL,
  `generation_settings_id` int(11) NOT NULL,
  PRIMARY KEY (`custom`,`id`) USING BTREE,
  UNIQUE KEY `tradeup_unique` (`stattrak`,`rarity`,`condition_target`,`collection_count`,`float_dict_id`,`generation_settings_id`),
  KEY `float_dict_id` (`float_dict_id`),
  KEY `FK_trade_up_generation_settings` (`custom`,`generation_settings_id`),
  CONSTRAINT `FK_trade_up_generation_settings` FOREIGN KEY (`custom`, `generation_settings_id`) REFERENCES `generation_settings` (`custom`, `id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `tradeup_ibfk_1` FOREIGN KEY (`float_dict_id`) REFERENCES `float_dictionary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

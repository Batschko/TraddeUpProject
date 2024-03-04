/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE TABLE IF NOT EXISTS `trade_up` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `modified_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `stattrak` tinyint(1) NOT NULL,
  `rarity` varchar(32) NOT NULL,
  `condition_target` varchar(20) NOT NULL,
  `collection_count` tinyint(1) NOT NULL,
  `status` tinyint(11) NOT NULL,
  `negative_loss` tinyint(1) DEFAULT NULL,
  `float_sum_needed` float DEFAULT NULL,
  `float_dict_id` int(11) NOT NULL,
  `classification_id` int(11) DEFAULT NULL,
  `generation_settings_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tradeup_unique` (`stattrak`,`rarity`,`condition_target`,`collection_count`,`float_dict_id`,`generation_settings_id`),
  KEY `float_dict_id` (`float_dict_id`),
  KEY `classification_id` (`classification_id`),
  KEY `generation_settings_id` (`generation_settings_id`),
  CONSTRAINT `tradeup_ibfk_1` FOREIGN KEY (`float_dict_id`) REFERENCES `float_dictionary` (`id`),
  CONSTRAINT `tradeup_ibfk_2` FOREIGN KEY (`classification_id`) REFERENCES `classification` (`id`) ON DELETE CASCADE,
  CONSTRAINT `tradeup_ibfk_3` FOREIGN KEY (`generation_settings_id`) REFERENCES `generation_settings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

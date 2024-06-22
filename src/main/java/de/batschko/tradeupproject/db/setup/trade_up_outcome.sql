/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE TABLE IF NOT EXISTS `trade_up_outcome` (
  `custom` tinyint(4) NOT NULL,
  `tradeup_id` int(11) NOT NULL,
  `cost` float NOT NULL,
  `skin_min` float NOT NULL,
  `skin_max` float NOT NULL,
  `skin_avg` float NOT NULL,
  `loss` float NOT NULL,
  `value` float NOT NULL,
  `chance_value` float NOT NULL,
  `repeat_factor` float NOT NULL,
  `repeat_factor_chance` float NOT NULL,
  `hit_chance` float NOT NULL,
  `float_marker` tinyint(1) DEFAULT NULL,
  `outcome` float NOT NULL,
  `amount_sold_avg` float DEFAULT NULL,
  `category_marker` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`tradeup_id`,`custom`) USING BTREE,
  KEY `FK_trade_up_outcome_trade_up` (`custom`,`tradeup_id`),
  CONSTRAINT `FK_trade_up_outcome_trade_up` FOREIGN KEY (`custom`, `tradeup_id`) REFERENCES `trade_up` (`custom`, `id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

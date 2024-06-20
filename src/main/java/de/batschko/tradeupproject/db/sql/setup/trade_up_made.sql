/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE TABLE IF NOT EXISTS `trade_up_made` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `modified_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `trade_up_id` int(11) DEFAULT NULL,
  `stattrak` tinyint(4) NOT NULL,
  `rarity` varchar(32) DEFAULT NULL,
  `float_dict_id` int(11) DEFAULT NULL,
  `generation_settings` varchar(500) NOT NULL,
  `c_s2_skin_id` int(11) DEFAULT NULL,
  `skin_name` varchar(500) NOT NULL,
  `condition` varchar(20) NOT NULL DEFAULT '',
  `cost` float NOT NULL,
  `price` float NOT NULL,
  `profit` float GENERATED ALWAYS AS (`price` - `cost`) VIRTUAL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

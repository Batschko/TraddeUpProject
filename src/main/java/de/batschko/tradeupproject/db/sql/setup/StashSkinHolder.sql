/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE TABLE IF NOT EXISTS `stash_skin_holder` (
  `stash_id` int(11) NOT NULL,
  `modified_date` datetime DEFAULT NOW() ON UPDATE NOW(),
  `collection_id` int(11) NOT NULL,
  `weapon` varchar(15) NOT NULL,
  `title` varchar(30) NOT NULL,
  `rarity` varchar(20) NOT NULL,
  `top` tinyint(1) NOT NULL,
  `bottom` tinyint(1) default NULL,
  `float_start` float NOT NULL,
  `float_end` float NOT NULL,
  `image_url` varchar(400) NOT NULL,
  PRIMARY KEY (`stash_id`),
  KEY `collection_id` (`collection_id`),
  CONSTRAINT `stash_skin_holder_ibfk_1` FOREIGN KEY (`collection_id`) REFERENCES `collection` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DELIMITER //
CREATE PROCEDURE `_generate_sequential_id`(
	IN `tbl_name` VARCHAR(255),
	IN `cstm` TINYINT,
	OUT `new_id` INT
)
BEGIN
    DECLARE current_max INT;

    -- Get the current maximum ID for the given table_name and custom
    SET current_max = (SELECT max_id FROM id_sequence WHERE table_name = tbl_name AND custom = cstm FOR UPDATE);

    -- If the source does not exist for this table_name in the id_sequence table, insert it with max_id = 0
    IF current_max IS NULL THEN
        INSERT INTO id_sequence (table_name, custom, max_id) VALUES (tbl_name, cstm, 0);
        SET current_max = 0;
    END IF;

    -- Increment the max_id for the given table_name and custom
    SET new_id = current_max + 1;

    -- Update the max_id in the id_sequence table
    UPDATE id_sequence SET max_id = new_id WHERE table_name = tbl_name AND custom = cstm;
END//
DELIMITER ;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

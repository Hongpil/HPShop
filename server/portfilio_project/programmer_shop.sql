
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";

--
-- Database structure for database `programmer_shop_db`
--
CREATE DATABASE IF NOT EXISTS `programmer_shop_db` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `programmer_shop_db`;
-----------------------------------------------------------
--
-- Table structure for table `user`
--
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` DATETIME ON UPDATE CURRENT_TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `email` VARCHAR(255),
  `name` VARCHAR(255),
  `password` VARCHAR(255),
  `crypto_token` VARCHAR(255),
  `certify_status` BOOLEAN
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-----------------------------------------------------------
--
-- Table structure for table `product_image`
--
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` DATETIME ON UPDATE CURRENT_TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `path` VARCHAR(255),
  `product_id` BIGINT(20),
  INDEX(`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-----------------------------------------------------------
--
-- Table structure for table `product`
--
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` DATETIME ON UPDATE CURRENT_TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `category_id` INT(11) NOT NULL,
  `description` VARCHAR(500),
  `name` VARCHAR(40),
  `price` INT(11) NOT NULL,
  `status` VARCHAR(255),
  `user_id` BIGINT(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

COMMIT;
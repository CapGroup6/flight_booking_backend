CREATE DATABASE `flight_booking` /*!40100 DEFAULT CHARACTER SET utf8mb3 COLLATE utf8mb3_bin */ /*!80016 DEFAULT ENCRYPTION='N' */;

CREATE TABLE `sys_user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(45) COLLATE utf8mb3_bin NOT NULL,
  `password` varchar(100) COLLATE utf8mb3_bin NOT NULL,
  `nick_name` varchar(45) COLLATE utf8mb3_bin DEFAULT NULL,
  `email` varchar(45) COLLATE utf8mb3_bin NOT NULL,
  `phone` varchar(45) COLLATE utf8mb3_bin NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `salt` varchar(45) COLLATE utf8mb3_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `phone_UNIQUE` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
CREATE DATABASE IF NOT EXISTS `vlasje24` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `vlasje24`;

CREATE TABLE IF NOT EXISTS `date` (
    `week_id`  INT NOT NULL AUTO_INCREMENT,
    `date`     DATE NOT NULL,
    PRIMARY KEY (`week_id`)
);

CREATE TABLE IF NOT EXISTS `song` (
    `song_id`     INT          NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(255) NOT NULL,
    `image_url`   VARCHAR(500),
    `preview_url` VARCHAR(500),
    PRIMARY KEY (`song_id`)
);

CREATE TABLE IF NOT EXISTS `artist` (
    `artist_id` INT          NOT NULL AUTO_INCREMENT,
    `name`      VARCHAR(255) NOT NULL,
    PRIMARY KEY (`artist_id`)
);

CREATE TABLE IF NOT EXISTS `artist_of_song` (
    `song_id`      INT     NOT NULL,
    `artist_id`    INT     NOT NULL,
    `artist_order` TINYINT NOT NULL,
    PRIMARY KEY (`song_id`, `artist_id`),
    FOREIGN KEY (`song_id`)   REFERENCES `song`(`song_id`),
    FOREIGN KEY (`artist_id`) REFERENCES `artist`(`artist_id`)
);

CREATE TABLE IF NOT EXISTS `chart` (
    `week_id`  INT NOT NULL,
    `position` INT NOT NULL,
    `song_id`  INT NOT NULL,
    PRIMARY KEY (`week_id`, `position`),
    FOREIGN KEY (`week_id`) REFERENCES `date`(`week_id`),
    FOREIGN KEY (`song_id`) REFERENCES `song`(`song_id`)
);

CREATE TABLE IF NOT EXISTS `user` (
    `user_id`  INT          NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`user_id`)
);

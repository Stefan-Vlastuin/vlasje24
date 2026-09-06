CREATE TABLE `date` (
    `week_id` INT NOT NULL AUTO_INCREMENT,
    `date` DATE NOT NULL,
    PRIMARY KEY (`week_id`)
);

CREATE TABLE `song` (
    `song_id` INT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `image_url` VARCHAR(500),
    `preview_url` VARCHAR(500),
    PRIMARY KEY (`song_id`)
);

CREATE TABLE `artist` (
    `artist_id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`artist_id`)
);

CREATE TABLE `artist_of_song` (
    `song_id` INT NOT NULL,
    `artist_id` INT NOT NULL,
    `artist_order` TINYINT NOT NULL,
    PRIMARY KEY (`song_id`, `artist_id`),
    FOREIGN KEY (`song_id`) REFERENCES `song` (`song_id`),
    FOREIGN KEY (`artist_id`) REFERENCES `artist` (`artist_id`)
);

CREATE TABLE `chart` (
    `week_id` INT NOT NULL,
    `position` INT NOT NULL,
    `song_id` INT NOT NULL,
    PRIMARY KEY (`week_id`, `position`),
    FOREIGN KEY (`week_id`) REFERENCES `date` (`week_id`),
    FOREIGN KEY (`song_id`) REFERENCES `song` (`song_id`)
);

CREATE TABLE `user` (
    `user_id` INT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`user_id`)
);

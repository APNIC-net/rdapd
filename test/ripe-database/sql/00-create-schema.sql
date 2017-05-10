
CREATE DATABASE IF NOT EXISTS whowas_test;

CREATE TABLE IF NOT EXISTS whowas_test.last (
    object_id           INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    sequence_id         INT(10) UNSIGNED NOT NULL,
    timestamp           INT(10) UNSIGNED NOT NULL,
    object_type         TINYINT(3) UNSIGNED NOT NULL,
    object              LONGBLOB NOT NULL,
    pkey                VARCHAR(54) NOT NULL,
    PRIMARY KEY (object_id, sequence_id) 
);

CREATE TABLE IF NOT EXISTS whowas_test.history (
    object_id           INT(10) UNSIGNED NOT NULL,
    sequence_id         INT(10) UNSIGNED NOT NULL,
    timestamp           INT(10) UNSIGNED NOT NULL,
    object_type         TINYINT(3) UNSIGNED NOT NULL,
    object              LONGBLOB NOT NULL,
    pkey                VARCHAR(254) NOT NULL,
    PRIMARY KEY (object_id, sequence_id)
);

CREATE TABLE IF NOT EXISTS whowas_test.serials (
    thread_id           INT(10) UNSIGNED NOT NULL,
    serial_id           INT(11) NOT NULL,
    object_id           INT(8) UNSIGNED NOT NULL,
    sequence_id         INT(10) UNSIGNED NOT NULL,
    atlast              TINYINT(4) UNSIGNED NOT NULL,
    operation           TINYINT(4) UNSIGNED NOT NULL,
    PRIMARY KEY (serial_id)
);


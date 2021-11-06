CREATE TABLE IF NOT EXISTS user (
    id int(11) unsigned NOT NULL AUTO_INCREMENT,
    username varchar(255) default NULL,
    address varchar(255) default NULL,
    password varchar(255) default NULL,
    PRIMARY KEY (id)
);
--) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

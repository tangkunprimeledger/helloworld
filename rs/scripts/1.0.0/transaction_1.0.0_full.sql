use trust;

CREATE TABLE IF NOT EXISTS `core_transaction` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`biz_model` TEXT DEFAULT NULL COMMENT 'the save data create the biz',
	`policy_id` VARCHAR (32) NOT NULL COMMENT 'policy id',
	`lock_time` datetime NOT NULL COMMENT 'the lock time create the tx . use in rs and slave to deal tx',
	`sender` VARCHAR (32) NOT NULL COMMENT 'the rsId if the sender  for the tx',
	`version` VARCHAR (32) NOT NULL COMMENT 'the version create the tx',
	`signature` VARCHAR (255) NOT NULL COMMENT 'the signature for tx create the create rs',
	`status` VARCHAR (32) NOT NULL COMMENT 'the status create the row: 1.INIT 2.ALREADY_SEND 3.FAIL 4.SUCCESS',
	`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
	`update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id` (`tx_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create core transaction';


CREATE TABLE IF NOT EXISTS `core_action` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`type` VARCHAR (32) NOT NULL COMMENT 'action type',
	`index` BIGINT (20) NOT NULL COMMENT 'action index in the transaction',
	`data` MEDIUMTEXT NOT NULL COMMENT 'The actual action . it is storted as a json data',
	`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id_type_index` (`tx_id`,`type`,`index`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create core action';


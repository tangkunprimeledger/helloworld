-- create trust database
CREATE DATABASE
IF NOT EXISTS trust;

USE trust;

CREATE TABLE IF NOT EXISTS `core_transaction` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`policy_id` VARCHAR (32) NOT NULL COMMENT 'policy id',
	`lock_time` datetime(3) DEFAULT NULL COMMENT 'the lock time create the tx . use in rs and slave to deal tx',
	`sender` VARCHAR (32) NOT NULL COMMENT 'the rsId if the sender  for the tx',
	`version` VARCHAR (32) NOT NULL COMMENT 'the version create the tx',
	`biz_model` MEDIUMTEXT DEFAULT NULL COMMENT 'the save data create the biz',
	`action_datas` varchar(4096) DEFAULT NULL COMMENT 'the action datas for tx',
	`sign_datas` varchar(4096) NOT NULL COMMENT 'the signature for tx',
	`status` VARCHAR (32) NOT NULL COMMENT 'the status create the row: 1.INIT 2.NEED_VOTE/WAIT 3.PERSISTED 4.END',
	`execute_result` varchar (24) DEFAULT NULL COMMENT 'tx execute result,0:fail,1:success',
	`error_code` varchar(128) DEFAULT NULL COMMENT 'tx execute error code',
	`error_msg` varchar(256) DEFAULT NULL COMMENT 'tx execute error msg',
	`send_time` datetime(3) NOT NULL COMMENT 'tx send time',
	`create_time` datetime(3) NOT NULL COMMENT 'create time',
	`update_time` datetime(3) DEFAULT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id` (`tx_id`),
	KEY `idx_status` (`status`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create core transaction';


CREATE TABLE IF NOT EXISTS `vote_rule` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`policy_id` VARCHAR (32) NOT NULL COMMENT 'policy id',
	`vote_pattern` VARCHAR (16) NOT NULL COMMENT 'rs vote pattern 1.SYNC 2.ASYNC',
	`callback_type` VARCHAR (16) NOT NULL COMMENT 'callback type of slave 1.ALL 2.SELF',
	`create_time` datetime(3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_policy_id` (`policy_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the vote rule of policy';


CREATE TABLE IF NOT EXISTS `vote_request_record` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`sender` VARCHAR (32) NOT NULL COMMENT 'the rsId of the sender for the tx',
	`tx_data` TEXT NOT NULL COMMENT 'the tx data',
	`sign` VARCHAR (1024) DEFAULT NULL COMMENT 'the sign data of voter',
	`vote_result` VARCHAR (16) NOT NULL COMMENT 'vote result 1.INIT 2.AGREE 3.DISAGREE',
	`create_time` datetime(3) NOT NULL COMMENT 'create time',
	`update_time` datetime(3) DEFAULT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id` (`tx_id`),
	KEY `idx_vote_result` (`vote_result`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the vote request record';


CREATE TABLE IF NOT EXISTS `vote_receipt` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`voter` VARCHAR (32) NOT NULL COMMENT 'the rsId of the sender for the tx',
	`sign`  VARCHAR (1024) DEFAULT NULL COMMENT 'the sign data of voter',
	`vote_result` VARCHAR (16) NOT NULL COMMENT 'vote result 1.AGREE 2.DISAGREE',
	`create_time` datetime(3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id` (`tx_id`,`voter`),
	KEY `idx_tx_id` (`tx_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the vote receipt';


CREATE TABLE IF NOT EXISTS `biz_type` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`policy_id` VARCHAR (32) NOT NULL COMMENT 'policy id',
	`biz_type` VARCHAR (32) NOT NULL COMMENT 'biz type',
	`create_time` datetime(3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_policy_id` (`policy_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table of business type';


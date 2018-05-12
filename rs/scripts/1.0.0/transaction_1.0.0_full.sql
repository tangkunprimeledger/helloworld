use trust;

CREATE TABLE IF NOT EXISTS `core_transaction` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`biz_model` TEXT DEFAULT NULL COMMENT 'the save data create the biz',
	`policy_id` VARCHAR (32) NOT NULL COMMENT 'policy id',
	`lock_time` datetime(3) DEFAULT NULL COMMENT 'the lock time create the tx . use in rs and slave to deal tx',
	`sender` VARCHAR (32) NOT NULL COMMENT 'the rsId if the sender  for the tx',
	`version` VARCHAR (32) NOT NULL COMMENT 'the version create the tx',
	`status` VARCHAR (32) NOT NULL COMMENT 'the status create the row: 1.INIT 2.WAIT 3.VALIDATED 4.PERSISTED 5.END',
	`exe_result` TINYINT (1) DEFAULT NULL COMMENT 'tx execute result,0:fail,1:success',
	`error_code` varchar(128) DEFAULT NULL COMMENT 'tx execute error code',
	`action_data` MEDIUMTEXT DEFAULT NULL COMMENT 'the signature for tx create the create rs',
	`sign_data` MEDIUMTEXT NOT NULL COMMENT 'the signature for tx create the create rs',
	`create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
	`update_time` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id` (`tx_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create core transaction';


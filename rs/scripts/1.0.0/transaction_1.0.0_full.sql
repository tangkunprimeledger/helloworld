use trust;

CREATE TABLE IF NOT EXISTS `core_transaction` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`biz_type` varchar(32) NOT NULL COMMENT 'the business type',
	`policy_id` VARCHAR (32) NOT NULL COMMENT 'policy id',
	`lock_time` datetime(3) DEFAULT NULL COMMENT 'the lock time create the tx . use in rs and slave to deal tx',
	`sender` VARCHAR (32) NOT NULL COMMENT 'the rsId if the sender  for the tx',
	`version` VARCHAR (32) NOT NULL COMMENT 'the version create the tx',
	`biz_model` MEDIUMTEXT DEFAULT NULL COMMENT 'the save data create the biz',
	`action_datas` varchar(4096) DEFAULT NULL COMMENT 'the signature for tx create the create rs',
	`sign_datas` varchar(4096) NOT NULL COMMENT 'the signature for tx create the create rs',
	`status` VARCHAR (32) NOT NULL COMMENT 'the status create the row: 1.INIT 2.WAIT 3.VALIDATED 4.PERSISTED 5.END',
	`execute_result` varchar (24) DEFAULT NULL COMMENT 'tx execute result,0:fail,1:success',
	`error_code` varchar(128) DEFAULT NULL COMMENT 'tx execute error code',
	`create_time` datetime(3) NOT NULL COMMENT 'create time',
	`update_time` datetime(3) DEFAULT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id` (`tx_id`),
	KEY `idx_status` (`status`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create core transaction';


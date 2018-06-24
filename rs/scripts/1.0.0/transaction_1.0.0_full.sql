use trust;


CREATE TABLE
IF NOT EXISTS `request` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`request_id` VARCHAR (64) NOT NULL COMMENT 'request id',
	`data` varchar(12288) NULL COMMENT 'the request data',
	`status` VARCHAR(32) NOT NULL COMMENT 'request status',
	`resp_code` VARCHAR(10) DEFAULT NULL COMMENT 'response code',
	`resp_msg` varchar(2048) DEFAULT NULL COMMENT 'response msg',
	`create_time` datetime (3) NOT NULL COMMENT 'the create time',
	`update_time` datetime (3) NOT NULL COMMENT 'the update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_request_id` (`request_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table request';

CREATE TABLE
IF NOT EXISTS `receivable_bill` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`bill_id` VARCHAR (64) NOT NULL COMMENT 'bill id',
	`state` varchar(4096) NULL COMMENT 'the utxo state data',
	`status` VARCHAR (32) NOT NULL COMMENT 'bill status',
	`holder` VARCHAR (64) NOT NULL COMMENT 'bill holder',
	`tx_id` varchar(64) NOT NULL COMMENT 'transaction id which owns uxto',
	`action_index` BIGINT (20) NOT NULL COMMENT 'action index which owns uxto',
	`index` BIGINT (20) NOT NULL COMMENT 'output index which owns uxto',
	`contract_address` varchar(64) NOT NULL COMMENT 'contract address',
	`create_time` datetime (3) NOT NULL COMMENT 'the create time',
	`update_time` datetime (3) NOT NULL COMMENT 'the update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id_action_index_index` (`tx_id`,`action_index`,`index`),
	UNIQUE KEY `uniq_bill_id` (`bill_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table receivable bill';

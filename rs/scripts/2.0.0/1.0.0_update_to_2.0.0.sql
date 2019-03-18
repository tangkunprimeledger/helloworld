
CREATE TABLE IF NOT EXISTS `core_transaction_process` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`status` VARCHAR (32) NOT NULL COMMENT 'tx status: 1.INIT 2.NEED_VOTE/WAIT 3.PERSISTED 4.END',
	`create_time` datetime(3) NOT NULL COMMENT 'create time',
	`update_time` datetime(3) DEFAULT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id` (`tx_id`),
	KEY `idx_status` (`status`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create core transaction process';

alter table core_transaction drop column `status`;

alter table `core_transaction` add column `tx_type` varchar(16) NOT NULL DEFAULT 'DEFAULT' COMMENT 'the type of transaction' AFTER `block_height`;
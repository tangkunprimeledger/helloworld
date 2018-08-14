CREATE DATABASE IF NOT EXISTS trust;
use trust;
CREATE TABLE IF NOT EXISTS `receivable_bill` (
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


CREATE TABLE IF NOT EXISTS `bankchain_request` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `request_id` varchar(64) NOT NULL COMMENT 'unique request id',
  `biz_type` varchar(64) NOT NULL COMMENT 'business type',
  `status` varchar(50) NOT NULL COMMENT 'request status：INIT PROCESSING FAIL DUPLICATE SUCCESS',
  `resp_code` varchar(10) DEFAULT NULL COMMENT 'response code：RespCodeEnum',
  `resp_msg` varchar(1024) DEFAULT NULL COMMENT 'response message',
  `create_time` datetime (3) NOT NULL COMMENT 'create time',
  `update_time` datetime (3)  NULL  COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_request_id` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment 'banckchain request table';



CREATE TABLE IF NOT EXISTS `identity_request` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `request_id` varchar(64) NOT NULL COMMENT 'unique request id',
  `key` varchar(64) NOT NULL COMMENT 'identity key',
  `value` varchar(8192) NOT NULL COMMENT 'identity value',
  `flag` varchar(10) NOT NULL COMMENT 'modify flag，000 means modify  999 means do nothing',
  `create_time` datetime (3) NOT NULL COMMENT 'create time',
  `update_time` datetime (3)  NULL  COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_request_id` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment 'identity request table';


CREATE TABLE IF NOT EXISTS `identity` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `request_id` varchar(64) NOT NULL COMMENT 'unique request id',
  `key` varchar(64) NOT NULL COMMENT 'identity key',
  `value` varchar(8192) NOT NULL COMMENT 'identity value',
  `create_time` datetime (3) NOT NULL COMMENT 'create time',
  `update_time` datetime (3)  NULL  COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_request_id` (`request_id`),
  UNIQUE KEY `uniq_key` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment 'identity table';


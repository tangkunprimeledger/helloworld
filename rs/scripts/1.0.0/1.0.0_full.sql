-- create trust database
CREATE DATABASE
IF NOT EXISTS trust;

USE trust;


DROP TABLE IF EXISTS `bankchain_request`;
CREATE TABLE `bankchain_request` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `request_id` varchar(64) NOT NULL COMMENT 'unique request id',
  `biz_type` varchar(64) NOT NULL COMMENT 'business type',
  `status` varchar(50) NOT NULL COMMENT 'request status：INIT PROCESSING FAIL DUPLICATE SUCCESS',
  `resp_code` varchar(10) DEFAULT NULL COMMENT 'response code：RespCodeEnum',
  `resp_msg` varchar(1024) DEFAULT NULL COMMENT 'response message',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime  NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_request_id` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment 'banckchain request table';


DROP TABLE IF EXISTS `identity_request`;
CREATE TABLE `identity_request` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `request_id` varchar(64) NOT NULL COMMENT 'unique request id',
  `key` varchar(64) NOT NULL COMMENT 'identity key',
  `value` varchar(8192) NOT NULL COMMENT 'identity value',
  `flag` varchar(10) NOT NULL COMMENT 'modify flag，000 means modify  999 means do nothing',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime  NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_request_id` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment 'identity request table';


DROP TABLE IF EXISTS `identity`;
CREATE TABLE `identity` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `request_id` varchar(64) NOT NULL COMMENT 'unique request id',
  `key` varchar(64) NOT NULL COMMENT 'identity key',
  `value` varchar(8192) NOT NULL COMMENT 'identity value',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime  NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_request_id` (`request_id`),
  UNIQUE KEY `uniq_key` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment 'identity table';

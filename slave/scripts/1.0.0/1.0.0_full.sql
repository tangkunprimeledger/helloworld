-- create trust database
CREATE DATABASE IF NOT EXISTS trust;

USE trust;

-- account init
CREATE TABLE
IF NOT EXISTS `currency_info` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`currency` VARCHAR (24) NOT NULL COMMENT 'currency',
	`remark` VARCHAR (64) DEFAULT NULL COMMENT 'remark',
	`homomorphicPk` TEXT DEFAULT NULL COMMENT 'homomorphicPk',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_currency` (`currency`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create currency info';

CREATE TABLE
IF NOT EXISTS `account_info` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`account_no` VARCHAR (64) NOT NULL COMMENT 'number create account',
	`currency` VARCHAR (24) NOT NULL COMMENT 'currency create account',
	`balance` DECIMAL (28, 10) NOT NULL COMMENT 'balance create account',
	`freeze_amount` DECIMAL (28, 10) NOT NULL COMMENT 'freeze amount create account',
	`fund_direction` VARCHAR (24) NOT NULL COMMENT 'fund direction-DEBIT,CREDIT',
	`detail_no` BIGINT (20) DEFAULT '0' COMMENT 'detail number',
	`detail_freeze_no` BIGINT (20) DEFAULT '0' COMMENT 'freeze detail number',
	`status` VARCHAR (16) DEFAULT 'NORMAL' COMMENT 'status,NORMAL,DESTROY',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	`update_time` datetime (3) DEFAULT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_account_no` (`account_no`),
	KEY `idx_create_time` (`create_time`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create account info';

CREATE TABLE
IF NOT EXISTS `account_dc_record` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`biz_flow_no` VARCHAR (64) NOT NULL COMMENT 'business flow number',
	`account_no` VARCHAR (64) NOT NULL COMMENT 'number create account',
	`dc_flag` VARCHAR (24) NOT NULL COMMENT 'dc flag-DEBIT,CREDIT',
	`amount` DECIMAL (28, 10) NOT NULL COMMENT 'happen amount',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	KEY `uniq_biz_flow_no` (`biz_flow_no`),
	KEY `idx_account_no` (`account_no`),
	KEY `idx_dc_flag` (`dc_flag`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create dc info';

CREATE TABLE
IF NOT EXISTS `account_detail` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`biz_flow_no` VARCHAR (64) NOT NULL COMMENT 'business flow number',
	`account_no` VARCHAR (64) NOT NULL COMMENT 'number create account',
	`block_height` BIGINT (20) NOT NULL COMMENT 'block height',
	`detail_no` BIGINT (20) NOT NULL COMMENT 'detail number',
	`change_direction` VARCHAR (20) NOT NULL COMMENT 'change direction create account-INCREASE,DECREASE',
	`amount` DECIMAL (28, 10) NOT NULL COMMENT 'happen amount',
	`before_amount` DECIMAL (28, 10) NOT NULL COMMENT 'before amount',
	`after_amount` DECIMAL (28, 10) NOT NULL COMMENT 'after amount',
	`remark` VARCHAR (100) DEFAULT NULL COMMENT 'remark',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	KEY `uniq_biz_flow_no` (`biz_flow_no`),
	KEY `idx_account_no` (`account_no`),
	KEY `idx_block_height` (`block_height`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create account detail';

CREATE TABLE
IF NOT EXISTS `account_detail_freeze` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`biz_flow_no` VARCHAR (64) NOT NULL COMMENT 'business flow number',
	`account_no` VARCHAR (64) NOT NULL COMMENT 'number create account',
	`block_height` BIGINT (20) NOT NULL COMMENT 'block height',
	`freeze_detail_no` BIGINT (20) NOT NULL COMMENT 'detail number for freeze',
	`freeze_type` VARCHAR (20) NOT NULL COMMENT 'freeze type-FREEZE,UNFREEZE',
	`amount` DECIMAL (28, 10) NOT NULL COMMENT 'happen amount',
	`before_amount` DECIMAL (28, 10) NOT NULL COMMENT 'before amount',
	`after_amount` DECIMAL (28, 10) NOT NULL COMMENT 'freeze amount',
	`remark` VARCHAR (100) DEFAULT NULL COMMENT 'remark',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	KEY `uniq_biz_flow_no` (`biz_flow_no`),
	KEY `idx_account_no` (`account_no`),
	KEY `idx_block_height` (`block_height`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create account freeze detail';

CREATE TABLE
IF NOT EXISTS `data_identity` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`identity` VARCHAR (64) NOT NULL COMMENT 'identity create data',
	`chain_owner` VARCHAR (24) NOT NULL COMMENT 'chain owner',
	`data_owner` VARCHAR (24) NOT NULL COMMENT 'data owner',
	`create_time` datetime (3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_identity` (`identity`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create data identity';

CREATE TABLE
IF NOT EXISTS `account_freeze_record` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`biz_flow_no` VARCHAR (64) NOT NULL COMMENT 'business flow number',
	`account_no` VARCHAR (64) NOT NULL COMMENT 'number create account',
	`block_height` BIGINT (20) NOT NULL COMMENT 'block height',
	`contract_addr` VARCHAR (64) DEFAULT NULL COMMENT 'the contract address',
	`amount` DECIMAL (28, 10) NOT NULL COMMENT 'the amount for freeze',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	`update_time` datetime (3) DEFAULT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_flow_no_acc_no` (`biz_flow_no`, `account_no`),
	KEY `idx_block_height` (`block_height`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create account freeze record';

-- block init
CREATE TABLE
IF NOT EXISTS `pending_transaction` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`tx_data` TEXT NOT NULL COMMENT 'transaction data',
	`status` VARCHAR (32) NOT NULL COMMENT 'transaction handle status',
	`height` BIGINT (20) DEFAULT NULL COMMENT 'block height',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	`update_time` datetime (3) DEFAULT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	INDEX `idx_pending_transaction` (`tx_id`),
  INDEX `idx_height` (`height`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'pending transaction';

CREATE TABLE
IF NOT EXISTS `block` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`height` BIGINT (20) NOT NULL COMMENT 'block height',
	`version` VARCHAR (32) NOT NULL COMMENT 'version',
	`previous_hash` VARCHAR (64) NOT NULL COMMENT 'previous block hash',
	`block_hash` VARCHAR (64) NOT NULL COMMENT 'block hash',
	`tx_root_hash` VARCHAR (64) NOT NULL COMMENT 'transaction merkle tree root hash',
	`account_root_hash` VARCHAR (64) NOT NULL COMMENT 'account state merkel tree root hash',
	`contract_root_hash` VARCHAR (64) NOT NULL COMMENT 'contract state merkel tree root hash',
	`policy_root_hash` VARCHAR (64) NOT NULL COMMENT 'policy merkle tree root hash',
	`rs_root_hash` VARCHAR (64) NOT NULL COMMENT 'rs merkle tree root hash',
	`tx_receipt_root_hash` VARCHAR (64) NOT NULL COMMENT 'tx receipt merkel tree root hash',
	`ca_root_hash` VARCHAR (64) NOT NULL COMMENT 'ca merkel tree root hash',
	`block_time` datetime (3) NOT NULL COMMENT 'block time',
	`tx_num` INT NOT NULL DEFAULT 0 COMMENT 'transaction num',
	`total_tx_num` BIGINT (20) DEFAULT 0 COMMENT 'total transaction num',
	`total_block_size` DECIMAL(8,2) DEFAULT NULL COMMENT 'total block size,unit:kb',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_block` (`height`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'block';

CREATE TABLE
IF NOT EXISTS `package` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`height` BIGINT (20) NOT NULL COMMENT 'block height',
	`status` VARCHAR (32) NOT NULL COMMENT 'package handle status',
	`callbacked` TINYINT (1) DEFAULT 0 COMMENT 'if or not notify RS',
	`package_time` BIGINT (20) NOT NULL COMMENT 'package time',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	`update_time` datetime (3) DEFAULT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_package` (`height`),
	KEY `idx_status` (`status`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'package';

-- contract init
CREATE TABLE
IF NOT EXISTS `contract` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`address` VARCHAR (64) NOT NULL COMMENT 'contract address',
	`block_height` BIGINT (20) DEFAULT NULL COMMENT 'block height',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'the id create transaction',
	`action_index` INT NOT NULL COMMENT 'the index create action',
	`language` VARCHAR (32) NOT NULL COMMENT 'contract code language',
  `version` VARCHAR(5) NOT NULL COMMENT  '',
	`code` NVARCHAR (8192) NOT NULL COMMENT 'contract code',
	`create_time` DATETIME (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_address` (`address`),
	UNIQUE KEY `uniq_txid_actionindex` (`tx_id`, `action_index`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'smart contract';

CREATE TABLE
IF NOT EXISTS `contract_state` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`address` VARCHAR (64) NOT NULL COMMENT 'contract address',
	`update_time` datetime (3) DEFAULT NULL COMMENT 'update time',
	`state` NVARCHAR (4096) NOT NULL COMMENT 'contract state',
	`key_desc` VARCHAR (256) DEFAULT NULL COMMENT 'the key description',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_address` (`address`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'contract state';

CREATE TABLE
IF NOT EXISTS `account_contract_binding` (
	`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
	`hash` VARCHAR (64) NOT NULL COMMENT 'hash',
	`block_height` BIGINT NOT NULL COMMENT 'the height create block',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'the id create transaction',
	`action_index` INT NOT NULL COMMENT 'the index create action',
	`account_no` VARCHAR (64) NOT NULL COMMENT 'number create account',
	`contract_address` VARCHAR (64) NOT NULL COMMENT 'the address create contract',
	`args` NVARCHAR (1024) COMMENT 'the args create contract invoke',
	`create_time` DATETIME (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_hash` (`hash`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'account contract';

-- policy init
CREATE TABLE
IF NOT EXISTS `policy` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`policy_id` VARCHAR (32) NOT NULL COMMENT 'policyID',
	`policy_name` VARCHAR (64) NOT NULL COMMENT 'policy name',
	`rs_ids` VARCHAR (1024) NOT NULL COMMENT 'the id list create related to rs',
	`decision_type` VARCHAR (16) NOT NULL COMMENT 'the decision type for vote ,1.FULL_VOTE,2.ONE_VOTE',
	`contract_addr` VARCHAR (64) DEFAULT NULL COMMENT 'the contract address for vote rule',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_policy` (`policy_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'business policy';

CREATE TABLE
IF NOT EXISTS `rs_node` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`rs_id` VARCHAR (32) NOT NULL COMMENT 'rs ID',
	`desc` VARCHAR (128) NOT NULL COMMENT 'business RS description',
	`status` VARCHAR (32) NOT NULL COMMENT 'rs status',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	`update_time` datetime (3) DEFAULT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_rs` (`rs_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'rs node info';

CREATE TABLE
IF NOT EXISTS `transaction` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`biz_model` TEXT DEFAULT NULL COMMENT 'the save data create the biz',
	`policy_id` VARCHAR (32) NOT NULL COMMENT 'policy id',
	`lock_time` datetime(3) DEFAULT NULL COMMENT 'the lock time create the tx . use in rs and slave to deal tx',
	`sender` VARCHAR (32) NOT NULL COMMENT 'the rsId if the sender  for the tx',
	`version` VARCHAR (32) NOT NULL COMMENT 'the version create the tx',
	`block_height` BIGINT (20) NOT NULL COMMENT 'the block height create the tx',
	`block_time` datetime (3) NOT NULL COMMENT 'the create time create the block for the tx',
	`send_time` datetime (3) NOT NULL COMMENT 'the transaction create time',
	`action_datas` TEXT DEFAULT NULL COMMENT 'the action list by json',
	`sign_datas` varchar(4096) DEFAULT NULL COMMENT 'the signatures by json',
	`execute_result` varchar(24) DEFAULT NULL COMMENT 'tx execute result,0:fail,1:success',
	`error_code` varchar(128) DEFAULT NULL COMMENT 'tx execute error code',
	`tx_type` varchar(16) NOT NULL DEFAULT 'DEFAULT' COMMENT 'the type of transaction',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id` (`tx_id`),
	INDEX `idx_block_height` (`block_height`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create transaction';

CREATE TABLE
IF NOT EXISTS `tx_out` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`index` INT (10) NOT NULL COMMENT 'index for the out in the transaction',
	`action_index` INT (10) NOT NULL COMMENT 'index for the action create the out in the transaction',
	`identity` VARCHAR (64) NOT NULL COMMENT 'identity id for the attribution create the row:data owner and chain owner',
	`state_class` VARCHAR (255) NOT NULL COMMENT 'the state class name',
	`state` TEXT NOT NULL COMMENT 'sate data',
	`contract_address` VARCHAR (64) NOT NULL COMMENT 'contract address',
	`status` VARCHAR (32) NOT NULL COMMENT 'the status create the out: 1.UNSPENT 2.SPENT',
	`s_tx_id` VARCHAR (64) DEFAULT NULL COMMENT 'the transaction id for spent the out',
	`create_time` datetime (3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT 'create time',
	`update_time` datetime (3) NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id_index_action_index` (`tx_id`,`index`,`action_index`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create tx out';


CREATE TABLE IF NOT EXISTS `queued_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `message_digest` varchar(128) NOT NULL COMMENT 'message digest',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `idx_message_digest` (`message_digest`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table apply_queue';


CREATE TABLE IF NOT EXISTS `queued_apply_delay` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `message_digest` varchar(128) NOT NULL COMMENT 'message digest',
  `apply_time` bigint(20) NOT NULL,
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `idx_message_digest` (`message_digest`),
  KEY `index_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table apply_delay_queue';


CREATE TABLE IF NOT EXISTS `queued_receive_gc` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `message_digest` varchar(128) NOT NULL COMMENT 'message digest',
  `gc_time` bigint(1) unsigned NOT NULL DEFAULT '0' COMMENT 'gc time',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `idx_message_digest` (`message_digest`),
  KEY `index_gc_time` (`gc_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table receive_gc_queue';


CREATE TABLE IF NOT EXISTS `queued_send` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `message_digest` varchar(128) NOT NULL COMMENT 'message digest',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `idx_message_digest` (`message_digest`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table send_queue';


CREATE TABLE IF NOT EXISTS `queued_send_delay` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `message_digest` varchar(128) NOT NULL COMMENT 'message digest',
  `send_time` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'requeued to send',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `idx_message_digest` (`message_digest`),
  KEY `index_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table send_delay_queue';


CREATE TABLE IF NOT EXISTS `queued_send_gc` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `message_digest` varchar(128) NOT NULL COMMENT 'message digest',
  `gc_time` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'gc time',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `idx_message_digest` (`message_digest`),
  KEY `index_gc_time` (`gc_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table send_gc_queue';


CREATE TABLE IF NOT EXISTS `receive_command` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `message_digest` varchar(128) NOT NULL COMMENT 'message digest',
  `valid_command` varchar(3072) NOT NULL COMMENT 'valid command',
  `command_class` varchar(255) NOT NULL DEFAULT '' COMMENT 'command class',
  `node_name` varchar(255) NOT NULL DEFAULT '' COMMENT 'node name',
  `receive_node_num` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT 'num of receive node',
  `apply_threshold` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT 'threshold to apply',
  `gc_threshold` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT 'threshold to gc',
  `status` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0-normal，1-add to apply queue，2-add to gc queue',
  `retry_apply_num` mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT 'count of retry apply',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_message_digest` (`message_digest`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table receive_command';


CREATE TABLE IF NOT EXISTS `receive_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `message_digest` varchar(128) NOT NULL COMMENT 'message digest',
  `from_node_name` varchar(64) NOT NULL COMMENT 'from node name',
  `command_sign` varchar(512) NOT NULL DEFAULT '' COMMENT 'command sign',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_message_digest_to_node_name` (`message_digest`,`from_node_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table receive_node';


CREATE TABLE IF NOT EXISTS `send_command` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `message_digest` varchar(128) NOT NULL COMMENT 'message digest',
  `valid_command` varchar(3072) NOT NULL COMMENT 'valid command',
  `node_name` varchar(255) NOT NULL DEFAULT '' COMMENT 'node name',
  `command_sign` varchar(512) NOT NULL DEFAULT '' COMMENT 'command sign',
  `command_class` varchar(255) NOT NULL DEFAULT '' COMMENT 'command class',
  `ack_node_num` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT 'num of  ack node',
  `gc_threshold` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT 'threshold to gc',
  `status` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0-add to send queue，1-add to gc queue',
  `retry_send_num` mediumint(5) unsigned NOT NULL DEFAULT '0' COMMENT 'count of retry',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_message_digest` (`message_digest`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table send_command';


CREATE TABLE IF NOT EXISTS `send_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `message_digest` varchar(128) NOT NULL COMMENT 'message digest',
  `to_node_name` varchar(64) NOT NULL DEFAULT '' COMMENT 'from node name',
  `status` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0-wait to send , 1- ack',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_message_digest_to_node_name` (`message_digest`,`to_node_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table receive_node';

CREATE TABLE IF NOT EXISTS `ca` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `version` varchar(32) NOT NULL COMMENT 'version',
  `period` datetime(3) NOT NULL COMMENT 'period',
  `valid` TINYINT (1) NOT NULL COMMENT 'valid flag TRUE/FALSE',
  `pub_key` varchar(1024) NOT NULL COMMENT 'pub key',
  `user` varchar(32) NOT NULL COMMENT 'CA user',
  `usage` varchar(64) COMMENT 'CA usage',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_node_use` (`user`,`usage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table which holds CA info';


CREATE TABLE IF NOT EXISTS `config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `version` varchar(32) NOT NULL COMMENT 'version',
  `valid` TINYINT (1) NOT NULL COMMENT 'valid flag TRUE/FALSE',
  `pub_key` varchar(1024) NOT NULL COMMENT 'pub key',
  `pri_key` varchar(2048) NOT NULL COMMENT 'pri key',
  `usage` varchar(30) NOT NULL COMMENT 'usage of pub/priKey',
  `tmp_pub_key` varchar(1024) COMMENT 'temp pub key',
  `tmp_pri_key` varchar(2048) COMMENT 'temp pri key',
  `node_name` varchar(32) NOT NULL COMMENT 'node name',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_node_use` (`node_name`,`usage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table which holds nodeself configuration';



CREATE TABLE IF NOT EXISTS `cluster_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `cluster_name` varchar(32) NOT NULL COMMENT 'cluster name',
  `node_num` int NOT NULL COMMENT 'node num',
  `fault_num` int NOT NULL COMMENT 'fault num',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_cluster` (`cluster_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table which holds cluster configuration';


CREATE TABLE IF NOT EXISTS `cluster_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `node_name` varchar(32) NOT NULL COMMENT 'node name',
  `p2p_status` TINYINT (1) NOT NULL COMMENT 'p2p status',
  `rs_status` TINYINT (1) NOT NULL COMMENT 'rs status',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_node` (`node_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='the table which holds cluster node configuration';


CREATE TABLE IF NOT EXISTS `system_property` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`key` VARCHAR (190) NOT NULL COMMENT 'property key',
	`value` VARCHAR (1024) NOT NULL COMMENT 'property value',
	`desc` VARCHAR (255) DEFAULT NULL COMMENT 'desc for the ',
	`create_time` datetime (3) NOT NULL COMMENT 'the create time',
	`update_time` datetime (3) NOT NULL COMMENT 'the update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_key` (`key`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table for system property';

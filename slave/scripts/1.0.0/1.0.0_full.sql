-- create trust database
create database trust;

use trust;

-- account init
CREATE TABLE IF NOT EXISTS `currency_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `currency` varchar(24) NOT NULL COMMENT 'currency',
  `remark` varchar(64) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_currency` (`currency`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='the table of currency info';

CREATE TABLE IF NOT EXISTS `account_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `account_no` varchar(64) NOT NULL COMMENT 'number of account',
  `currency` varchar(24) NOT NULL COMMENT 'currency of account',
  `balance` decimal(28,10) NOT NULL COMMENT 'balance of account',
  `freeze_amount` decimal(28,10) NOT NULL COMMENT 'freeze amount of account',
  `fund_direction` varchar(24) NOT NULL COMMENT 'fund direction-DEBIT,CREDIT',
  `detail_no` bigint(20) DEFAULT '0' COMMENT 'detail number',
  `detail_freeze_no` bigint(20) DEFAULT '0' COMMENT 'freeze detail number',
  `status` varchar(16) DEFAULT 'NORMAL' COMMENT 'status,NORMAL,DESTROY',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_account_no` (`account_no`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='the table of account info';

CREATE TABLE IF NOT EXISTS `account_dc_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `biz_flow_no` varchar(64) NOT NULL COMMENT 'business flow number',
  `account_no` varchar(64) NOT NULL COMMENT 'number of account',
  `dc_flag` varchar(24) NOT NULL COMMENT 'dc flag-DEBIT,CREDIT',
  `amount` decimal(28,10) NOT NULL COMMENT 'happen amount',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `uniq_biz_flow_no` (`biz_flow_no`),
  KEY `idx_account_no` (`account_no`),
  KEY `idx_dc_flag` (`dc_flag`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='the table of dc info';

CREATE TABLE IF NOT EXISTS `account_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `biz_flow_no` varchar(64) NOT NULL COMMENT 'business flow number',
  `account_no` varchar(64) NOT NULL COMMENT 'number of account',
  `block_height` bigint(20) NOT NULL COMMENT 'block height',
  `detail_no` bigint(20) NOT NULL COMMENT 'detail number',
  `change_direction` varchar(20) NOT NULL COMMENT 'change direction of account-INCREASE,DECREASE',
  `amount` decimal(28,10) NOT NULL COMMENT 'happen amount',
  `before_amount` decimal(28,10) NOT NULL COMMENT 'before amount',
  `after_amount` decimal(28,10) NOT NULL COMMENT 'after amount',
  `remark` varchar(100) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `uniq_biz_flow_no` (`biz_flow_no`),
  KEY `idx_account_no` (`account_no`),
  KEY `idx_block_height` (`block_height`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='the table of account detail';

CREATE TABLE IF NOT EXISTS `account_detail_freeze` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `biz_flow_no` varchar(64) NOT NULL COMMENT 'business flow number',
  `account_no` varchar(64) NOT NULL COMMENT 'number of account',
  `block_height` bigint(20) NOT NULL COMMENT 'block height',
  `freeze_detail_no` bigint(20) NOT NULL COMMENT 'detail number for freeze',
  `freeze_type` varchar(20) NOT NULL COMMENT 'freeze type-FREEZE,UNFREEZE',
  `amount` decimal(28,10) NOT NULL COMMENT 'happen amount',
  `before_amount` decimal(28,10) NOT NULL COMMENT 'before amount',
  `after_amount` decimal(28,10) NOT NULL COMMENT 'freeze amount',
  `remark` varchar(100) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `uniq_biz_flow_no` (`biz_flow_no`),
  KEY `idx_account_no` (`account_no`),
  KEY `idx_block_height` (`block_height`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='the table of account freeze detail';

CREATE TABLE IF NOT EXISTS `data_identity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `identity` varchar(64) NOT NULL COMMENT 'identity of data',
  `chain_owner` varchar(24) NOT NULL COMMENT 'chain owner',
  `data_owner` varchar(24) NOT NULL COMMENT 'data owner',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_identity` (`identity`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='the table of data identity';

CREATE TABLE IF NOT EXISTS `account_freeze_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `biz_flow_no` varchar(64) NOT NULL COMMENT 'business flow number',
  `account_no` varchar(64) NOT NULL COMMENT 'number of account',
  `block_height` bigint(20) NOT NULL COMMENT 'block height',
  `contract_addr` varchar(64) DEFAULT NULL COMMENT 'the contract address',
  `amount` decimal(28,10) NOT NULL COMMENT 'the amount for freeze',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_flow_no_acc_no` (`biz_flow_no`,`account_no`),
  KEY `idx_block_height` (`block_height`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='the table of account freeze record';


-- block init
CREATE TABLE IF NOT EXISTS `pending_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `tx_id` varchar(64) NOT NULL COMMENT 'transaction id',
  `tx_data` blob NOT NULL COMMENT 'transaction data',
  `status` varchar(32) NOT NULL COMMENT 'transaction handle status',
  `height` bigint(20) DEFAULT NULL COMMENT 'block height',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_pending_transation` (`tx_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='pending transaction';

CREATE TABLE IF NOT EXISTS `block` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `height` bigint(20) NOT NULL COMMENT 'block height',
  `version` varchar(32) NOT NULL COMMENT 'version',
  `previous_hash` varchar(64) NOT NULL COMMENT 'previous block hash',
  `block_hash` varchar(64) NOT NULL COMMENT 'block hash',
  `tx_root_hash` varchar(64) NOT NULL COMMENT 'transaction merkle tree root hash',
  `account_root_hash` varchar(64) NOT NULL COMMENT 'account state merkel tree root hash',
  `contract_root_hash` varchar(64) NOT NULL COMMENT 'contract state merkel tree root hash',
  `policy_root_hash` varchar(64) NOT NULL COMMENT 'policy merkle tree root hash',
  `rs_root_hash` varchar(64) NOT NULL COMMENT 'rs merkle tree root hash',
  `tx_receipt_root_hash` varchar(64) NOT NULL COMMENT 'tx receipt merkel tree root hash',
  `block_time` datetime(3) NOT NULL COMMENT 'block time',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_block` (`height`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='block';

CREATE TABLE IF NOT EXISTS `package` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `height` bigint(20) NOT NULL COMMENT 'block height',
  `status` varchar(32) NOT NULL COMMENT 'package handle status',
  `callbacked` tinyint(1) DEFAULT 0 COMMENT 'if or not notify RS',
  `package_time` bigint(20) NOT NULL COMMENT 'package time',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_package` (`height`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='package';

CREATE TABLE IF NOT EXISTS `block_header` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `height` bigint(20) NOT NULL COMMENT 'block height',
  `type` varchar(64) NOT NULL COMMENT 'header type',
  `header_data` varchar(2048) NOT NULL COMMENT 'block header data',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_block_header` (`height`,`type`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='block header';


-- contract init
CREATE TABLE IF NOT EXISTS `contract` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `address` VARCHAR(64) NOT NULL COMMENT 'contract address',
  `language` VARCHAR(32) NOT NULL COMMENT 'contract code language',
  `code` NVARCHAR(2048) NOT NULL COMMENT 'contract code',
  `create_time` DATETIME(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_address` (`address`)
) ENGINE = InnoDB  DEFAULT CHARSET = utf8mb4  COMMENT = 'smart contract';

CREATE TABLE IF NOT EXISTS `contract_state` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `address` VARCHAR(64) NOT NULL COMMENT 'contract address',
  `state` NVARCHAR(4096) NOT NULL COMMENT 'contract state',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_address` (`address`)
) ENGINE = InnoDB  DEFAULT CHARSET = utf8mb4  COMMENT = 'contract state';

CREATE TABLE IF NOT EXISTS `account_contract_binding` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `hash` VARCHAR(64) NOT NULL COMMENT 'hash',
  `block_height` BIGINT NOT NULL COMMENT 'the height of block',
  `tx_id` VARCHAR(64) NOT NULL COMMENT 'the id of transaction',
  `action_index` INT NOT NULL COMMENT 'the index of action',
  `account_no` VARCHAR(64) NOT NULL COMMENT 'number of account',
  `contract_address` VARCHAR(64) NOT NULL COMMENT 'the address of contract',
  `args` NVARCHAR(1024) COMMENT 'the args of contract invoke',
  `create_time` DATETIME(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_hash` (`hash`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'account contract';


-- merkle init
CREATE TABLE IF NOT EXISTS `merkle_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `uuid` varchar(64) NOT NULL COMMENT 'node unique id',
  `node_hash` varchar(64) NOT NULL COMMENT 'node hash',
  `index` bigint(20) NOT NULL COMMENT 'the index in the current level, start with 0',
  `level` int NOT NULL COMMENT 'current level, start with 1',
  `parent` VARCHAR(64) COMMENT 'the parent node uuid of the current node',
  `tree_type` varchar(32) NOT NULL COMMENT 'type，ACCOUNT or TX or CONTRACT',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_idx` (`level`,`tree_type`,`index`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='merkle node, this store the whole node info of merkle tree';

CREATE TABLE IF NOT EXISTS `merkle_tree` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `root_hash` varchar(64) NOT NULL COMMENT 'root hash',
  `total_level` int NOT NULL COMMENT 'the total level of the merkle tree, start with 1',
  `max_index` BIGINT (20) NOT NULL COMMENT 'the max index of leaf level of the merkle tree, start with 0',
  `tree_type` varchar(32) NOT NULL COMMENT 'type，ACCOUNT or TX or CONTRACT',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  `update_time` datetime(3) NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_tree_type` (`tree_type`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='merkle tree, this store the statistical information of a merkle tree';


-- policy init
CREATE TABLE IF NOT EXISTS `policy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `policy_id` varchar(32) NOT NULL COMMENT 'policyID',
  `policy_name` varchar(64) NOT NULL COMMENT 'policy name',
  `rs_ids` varchar(1024) NOT NULL COMMENT 'the id list of related to rs',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_policy` (`policy_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='business policy';

CREATE TABLE IF NOT EXISTS `rs_pub_key` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `rs_id` varchar(32) NOT NULL COMMENT 'rs ID',
  `pub_key` varchar(255) NOT NULL COMMENT 'public key',
  `desc` varchar(128) NOT NULL COMMENT 'business RS description',
  `create_time` datetime(3) NOT NULL COMMENT 'create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_rs` (`rs_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='the related between rs and public key';

-- utxo init

CREATE TABLE IF NOT EXISTS `transaction` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`biz_model` MEDIUMTEXT DEFAULT NULL COMMENT 'the save data of the biz',
	`policy_id` VARCHAR (32) NOT NULL COMMENT 'policy id',
	`lock_time` datetime NOT NULL COMMENT 'the lock time of the tx . use in rs and slave to deal tx',
	`sender` VARCHAR (32) NOT NULL COMMENT 'the rsId if the sender  for the tx',
	`version` VARCHAR (32) NOT NULL COMMENT 'the version of the tx',
	`block_height` BIGINT (20) NOT NULL COMMENT 'the block height of the tx',
	`block_time` datetime NOT NULL COMMENT 'the create time of the block for the tx',
	`sign_datas` MEDIUMTEXT DEFAULT NULL COMMENT 'the signatures by json',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id` (`tx_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table of transaction';

CREATE TABLE IF NOT EXISTS `action` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`type` VARCHAR (32) NOT NULL COMMENT 'action type',
	`index` INT (10) NOT NULL COMMENT 'action index in the transaction',
	`data` MEDIUMTEXT NOT NULL COMMENT 'The actual action . it is storted as a json data',
	`create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id_type_index` (`tx_id`,`type`,`index`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table of action';

CREATE TABLE IF NOT EXISTS `tx_out` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`index` INT (10) NOT NULL COMMENT 'index for the out in the transaction',
	`action_index` INT (10) NOT NULL COMMENT 'index for the action of the out in the transaction',
	`identity` VARCHAR (64) NOT NULL COMMENT 'identity id for the attribution of the row:data owner and chain owner',
	`state_class` VARCHAR (255) NOT NULL COMMENT 'the state class name',
	`state` TEXT NOT NULL COMMENT 'sate data',
	`contract` MEDIUMTEXT NOT NULL COMMENT 'contract script',
	`status` VARCHAR (32) NOT NULL COMMENT 'the status of the out: 1.UNSPENT 2.SPENT',
	`s_tx_id` VARCHAR (64) DEFAULT NULL COMMENT 'the transaction id for spent the out',
	`create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'create time',
	`update_time` datetime(3) NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id_index_action_index` (`tx_id`,`index`,`action_index`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table of tx out';

INSERT INTO `block` (`height`, `version`, `previous_hash`, `block_hash`, `tx_root_hash`, `account_root_hash`, `contract_root_hash`, `policy_root_hash`, `rs_root_hash`, `tx_receipt_root_hash`, `block_time`, `create_time`)
VALUE (1, '1.0.0', '0', 'Genesis-Block', 'NO_TREE', 'NO_TREE', 'NO_TREE', 'NO_TREE', 'NO_TREE', 'NO_TREE', now(3), now(3));
-- create trust database
CREATE DATABASE
IF NOT EXISTS trust;

USE trust;

-- account init
CREATE TABLE
IF NOT EXISTS `currency_info` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`currency` VARCHAR (24) NOT NULL COMMENT 'currency',
	`remark` VARCHAR (64) DEFAULT NULL COMMENT 'remark',
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
	`tx_data` VARCHAR (4096) NOT NULL COMMENT 'transaction data',
	`status` VARCHAR (32) NOT NULL COMMENT 'transaction handle status',
	`height` BIGINT (20) DEFAULT NULL COMMENT 'block height',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	`update_time` datetime (3) DEFAULT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_pending_transation` (`tx_id`)
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
	`block_time` datetime (3) NOT NULL COMMENT 'block time',
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
	UNIQUE KEY `uniq_package` (`height`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'package';

CREATE TABLE
IF NOT EXISTS `block_header` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`height` BIGINT (20) NOT NULL COMMENT 'block height',
	`type` VARCHAR (64) NOT NULL COMMENT 'header type',
	`header_data` VARCHAR (2048) NOT NULL COMMENT 'block header data',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_block_header` (`height`, `type`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'block header';

-- contract init
CREATE TABLE
IF NOT EXISTS `contract` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`address` VARCHAR (64) NOT NULL COMMENT 'contract address',
	`language` VARCHAR (32) NOT NULL COMMENT 'contract code language',
	`code` NVARCHAR (2048) NOT NULL COMMENT 'contract code',
	`create_time` DATETIME (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_address` (`address`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'smart contract';

CREATE TABLE
IF NOT EXISTS `contract_state` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`address` VARCHAR (64) NOT NULL COMMENT 'contract address',
	`state` NVARCHAR (4096) NOT NULL COMMENT 'contract state',
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

-- merkle init
CREATE TABLE
IF NOT EXISTS `merkle_node` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`uuid` VARCHAR (64) NOT NULL COMMENT 'node unique id',
	`node_hash` VARCHAR (64) NOT NULL COMMENT 'node hash',
	`index` BIGINT (20) NOT NULL COMMENT 'the index in the current level, start with 0',
	`level` INT NOT NULL COMMENT 'current level, start with 1',
	`parent` VARCHAR (64) COMMENT 'the parent node uuid create the current node',
	`tree_type` VARCHAR (32) NOT NULL COMMENT 'type，ACCOUNT or TX or CONTRACT',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	`update_time` datetime (3) NOT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_idx` (`level`,`tree_type`,`index`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'merkle node, this store the whole node info create merkle tree';

CREATE TABLE
IF NOT EXISTS `merkle_tree` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`root_hash` VARCHAR (64) NOT NULL COMMENT 'root hash',
	`total_level` INT NOT NULL COMMENT 'the total level create the merkle tree, start with 1',
	`max_index` BIGINT (20) NOT NULL COMMENT 'the max index create leaf level create the merkle tree, start with 0',
	`tree_type` VARCHAR (32) NOT NULL COMMENT 'type，ACCOUNT or TX or CONTRACT',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	`update_time` datetime (3) NOT NULL COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tree_type` (`tree_type`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'merkle tree, this store the statistical information create a merkle tree';

-- policy init
CREATE TABLE
IF NOT EXISTS `policy` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`policy_id` VARCHAR (32) NOT NULL COMMENT 'policyID',
	`policy_name` VARCHAR (64) NOT NULL COMMENT 'policy name',
	`rs_ids` VARCHAR (1024) NOT NULL COMMENT 'the id list create related to rs',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_policy` (`policy_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'business policy';

CREATE TABLE
IF NOT EXISTS `rs_pub_key` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`rs_id` VARCHAR (32) NOT NULL COMMENT 'rs ID',
	`pub_key` VARCHAR (255) NOT NULL COMMENT 'public key',
	`desc` VARCHAR (128) NOT NULL COMMENT 'business RS description',
	`create_time` datetime (3) NOT NULL COMMENT 'create time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_rs` (`rs_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the related between rs and public key';

CREATE TABLE
IF NOT EXISTS `transaction` (
	`id` BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`tx_id` VARCHAR (64) NOT NULL COMMENT 'transaction id',
	`biz_model` MEDIUMTEXT DEFAULT NULL COMMENT 'the save data create the biz',
	`policy_id` VARCHAR (32) NOT NULL COMMENT 'policy id',
	`lock_time` datetime NOT NULL COMMENT 'the lock time create the tx . use in rs and slave to deal tx',
	`sender` VARCHAR (32) NOT NULL COMMENT 'the rsId if the sender  for the tx',
	`version` VARCHAR (32) NOT NULL COMMENT 'the version create the tx',
	`block_height` BIGINT (20) NOT NULL COMMENT 'the block height create the tx',
	`block_time` datetime (3) NOT NULL COMMENT 'the create time create the block for the tx',
	`action_datas` MEDIUMTEXT DEFAULT NULL COMMENT 'the action list by json',
	`sign_datas` varchar(4096) DEFAULT NULL COMMENT 'the signatures by json',
	`execute_result` varchar(24) DEFAULT NULL COMMENT 'tx execute result,0:fail,1:success',
	`error_code` varchar(128) DEFAULT NULL COMMENT 'tx execute error code',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id` (`tx_id`)
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
	`contract` MEDIUMTEXT NOT NULL COMMENT 'contract script',
	`status` VARCHAR (32) NOT NULL COMMENT 'the status create the out: 1.UNSPENT 2.SPENT',
	`s_tx_id` VARCHAR (64) DEFAULT NULL COMMENT 'the transaction id for spent the out',
	`create_time` datetime (3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT 'create time',
	`update_time` datetime (3) NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT 'update time',
	PRIMARY KEY (`id`),
	UNIQUE KEY `uniq_tx_id_index_action_index` (`tx_id`,`index`,`action_index`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'the table create tx out';

INSERT INTO `block` (`height`, `version`, `previous_hash`, `block_hash`, `tx_root_hash`, `account_root_hash`, `contract_root_hash`, `policy_root_hash`, `rs_root_hash`, `tx_receipt_root_hash`, `block_time`, `create_time`)
VALUE (1, 'v1.0', '0', '48f662666b5ad8869c21026d588ba5024d47cdaa67334ce83bd088cad55b58f4', 'NO_TREE', 'NO_TREE', 'NO_TREE', 'NO_TREE', 'NO_TREE', 'NO_TREE', '2018-04-27 12:00:00.000', now(3));
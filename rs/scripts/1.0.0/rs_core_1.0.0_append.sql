
alter table `core_transaction` add column `tx_type` varchar(16) NOT NULL DEFAULT 'DEFAULT' COMMENT 'the type of transaction' AFTER `block_height`;
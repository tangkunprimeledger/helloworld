
ALTER TABLE `package` ADD INDEX `idx_status` (`status`);

alter table config add column `usage` varchar(30) not null default 'consensus' comment 'usage of pub/priKey value can be biz or consensus' after `pri_key`;
alter table config drop index uniq_node;
alter table config add UNIQUE KEY `uniq_node_use` (`node_name`,`usage`);

alter table ca drop index uniq_pub_key;
alter table ca add UNIQUE KEY `uniq_node_use` (`user`,`usage`);

alter table `transaction` add column `tx_type` varchar(16) NOT NULL DEFAULT 'DEFAULT' COMMENT 'the type of transaction' after `error_code`;

alter table config modify column pub_key varchar(1024);
alter table config modify column pri_key varchar(2048);

alter table ca modify column pub_key varchar(1024);

alter table config modify column tmp_pub_key varchar(1024);
alter table config modify column tmp_pri_key varchar(2048);


alter table currency_info add column `homomorphicPk` TEXT DEFAULT NULL COMMENT 'homomorphicPk';
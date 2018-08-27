
ALTER TABLE `package` ADD INDEX `idx_status` (`status`);

alter table config add column `usage` varchar(30) not null default 'consensus' comment 'usage of pub/priKey value can be biz or consensus' after `pri_key`;
alter table config drop index uniq_node;
alter table config add UNIQUE KEY `uniq_node_use` (`node_name`,`usage`);
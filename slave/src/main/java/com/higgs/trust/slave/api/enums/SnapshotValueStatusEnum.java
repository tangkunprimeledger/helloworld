package com.higgs.trust.slave.api.enums;

import lombok.Getter;

/**
 * Snapshot value status enum
 *
 * @author lingchao
 * @create 2018年04月09日16:28
 */
@Getter
public enum SnapshotValueStatusEnum {
    INSERT("INSERT", "insert data for Snapshot"),
    UPDATE("UPDATE", "update data for Snapshot");

    String code;
    String desc;

    SnapshotValueStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}

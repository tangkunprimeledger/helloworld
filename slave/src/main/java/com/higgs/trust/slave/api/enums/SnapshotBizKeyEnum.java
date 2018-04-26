package com.higgs.trust.slave.api.enums;

import lombok.Getter;

/**
 * Snapshot core key enum
 *
 * @author lingchao
 * @create 2018年04月09日16:28
 */
@Getter
public enum SnapshotBizKeyEnum {
    ACCOUNT("ACCOUNT", "account core Snapshot"),
    FREEZE("FREEZE", "freeze core Snapshot"),
    CONTRACT("CONTRACT", "contract core Snapshot"),
    ACCOUNT_CONTRACT_BIND("ACCOUNT_CONTRACT_BIND", "account contract binding Snapshot"),
    CONTRACT_SATE("CONTRACTSATE", "contract state Snapshot"),
    DATA_IDENTITY("DATA_IDENTITY", "dataIdentity core Snapshot"),
    UTXO("UTXO", "UTXO core Snapshot"), MANAGE("MANAGE", "manage  Snapshot"),
    MERKLE_TREE("MERKLE_TREE", "merkle tree  Snapshot"),
    OTHER("OTHER", "other  Snapshot"),;

    String code;
    String desc;

    SnapshotBizKeyEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SnapshotBizKeyEnum getByEnum(SnapshotBizKeyEnum snapshotBizKeyEnum) {
        for (SnapshotBizKeyEnum item : values()) {
            if (snapshotBizKeyEnum == item) {
                return item;
            }
        }
        return null;
    }

}

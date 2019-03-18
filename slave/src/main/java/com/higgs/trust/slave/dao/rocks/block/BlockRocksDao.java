package com.higgs.trust.slave.dao.rocks.block;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.block.BlockPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class BlockRocksDao extends RocksBaseDao <BlockPO> {
    @Override protected String getColumnFamilyName() {
        return "block";
    }

    public void save(BlockPO blockPO) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[BlockRocksDao.save] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        txPut(tx, String.valueOf(blockPO.getHeight()), blockPO);
    }

    public List<Long> getLimitHeight(List<String> blockHeights) {
        if (CollectionUtils.isEmpty(blockHeights)) {
            log.error("[BlockRocksDao.getLimitHeight] blockHeights is empty");
            return null;
        }

        Map<String, BlockPO> resultMap = multiGet(blockHeights);
        if (MapUtils.isEmpty(resultMap)) {
            return null;
        }

        List<Long> heights = new ArrayList<>(resultMap.size());
        for (String key : resultMap.keySet()) {
            if (!StringUtils.isEmpty(key)) {
                heights.add(Long.parseLong(key));
            }
        }
        return heights;
    }

    public List<BlockPO> queryBlocks(long startHeight, int size) {
        if (startHeight < 1 || size < 0) {
            log.error("[BlockRocksDao.queryBlocks] startHeight or size is invalid, startHeight={}, size={}", startHeight, size);
            return null;
        }
        List<String> blockHeights = new ArrayList<>(size);
        while (size-- > 0) {
            blockHeights.add(String.valueOf(startHeight++));
        }

        Map<String, BlockPO> resultMap = multiGet(blockHeights);

        if (MapUtils.isEmpty(resultMap)) {
            return null;
        }

        List<BlockPO> blockPOS = new ArrayList<>(resultMap.size());
        for (String key : resultMap.keySet()) {
            blockPOS.add(resultMap.get(key));
        }

        // sort by height asc
        Collections.sort(blockPOS, new Comparator<BlockPO>() {
            @Override public int compare(BlockPO po1, BlockPO po2) {
                return po1.getHeight().compareTo(po2.getHeight());
            }
        });

        return blockPOS;
    }
}

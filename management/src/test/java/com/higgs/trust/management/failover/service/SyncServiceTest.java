package com.higgs.trust.management.failover.service;

import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.management.exception.ManagementError;
import com.higgs.trust.management.failover.config.FailoverProperties;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.context.PackContext;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.stubbing.Answer;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Matchers.any;
import static org.testng.Assert.assertEquals;

@RunWith(PowerMockRunner.class) public class SyncServiceTest {

    @InjectMocks @Autowired SyncService syncService;
    @Mock FailoverProperties properties;
    @Mock SyncPackageCache cache;
    @Mock BlockService blockService;
    @Mock BlockRepository blockRepository;
    @Mock BlockSyncService blockSyncService;
    @Mock PackageService packageService;
    @Mock NodeState nodeState;
    @Mock TransactionTemplate txNested;

    long currentHeight = 1;
    @Mock BlockHeader header;

    @Mock List<Block> blocks;

    @BeforeMethod public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        Mockito.reset(properties, nodeState);
        Mockito.when(nodeState.isState(NodeStateEnum.AutoSync, NodeStateEnum.ArtificialSync)).thenReturn(true);
        Mockito.when(blockRepository.getMaxHeight()).thenReturn(currentHeight);
        Mockito.when(properties.getHeaderStep()).thenReturn(10);
    }

    @Test public void testSyncNotState() {
        Mockito.when(nodeState.isState(NodeStateEnum.AutoSync, NodeStateEnum.ArtificialSync)).thenReturn(false);
        syncService.autoSync();
        Mockito.verify(blockRepository, Mockito.times(0)).getMaxHeight();
    }

    @Test public void testSyncGetClusterHeightFailed() {
        Mockito.when(blockSyncService.getClusterHeight(Matchers.anyInt())).thenReturn(null);
        try {
            syncService.autoSync();
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_CONSENSUS_GET_RESULT_FAILED);
        }
    }

    @Test public void testSyncInThreshold() {
        Mockito.when(blockSyncService.getClusterHeight(Matchers.anyInt())).thenReturn(101L);
        Mockito.when(properties.getThreshold()).thenReturn(100);
        syncService.autoSync();
    }

    @Test public void testSyncOutThreshold() {
        Mockito.when(blockSyncService.getClusterHeight(Matchers.anyInt())).thenReturn(102L);
        Mockito.when(properties.getThreshold()).thenReturn(100);
        syncService.autoSync();
    }

    @Test public void testSyncWithParamNotState() {
        long startHeight = currentHeight + 1;
        int size = 10;
        Mockito.when(nodeState.isState(NodeStateEnum.AutoSync, NodeStateEnum.ArtificialSync)).thenReturn(false);
        try {
            syncService.sync(Matchers.anyLong(), Matchers.anyInt());
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_STATE_NOT_ALLOWED);
        }
        Mockito.verify(blockRepository, Mockito.times(0)).getMaxHeight();
    }

    @Test public void testSyncWithParamHeightNotCurrent() {
        long startHeight = currentHeight + 1;
        int size = 10;
        try {
            syncService.sync(currentHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_START_HEIGHT_ERROR);
        }
    }

    @Test public void testSyncWithParamGetHeadersFailed() {
        long startHeight = currentHeight + 1;
        int size = 10;
        Integer times = 3;
        Mockito.when(properties.getTryTimes()).thenReturn(times);
        Mockito.when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        Mockito.when(blockSyncService.getHeaders(startHeight, size)).thenReturn(null, Collections.emptyList());
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }

        //try times test
        Mockito.reset(properties);
        Mockito.when(properties.getTryTimes()).thenReturn(times);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }
        Mockito.verify(properties, Mockito.times(times)).getTryTimes();

        //validating headers failed
        Mockito.when(blockSyncService.getHeaders(Matchers.anyLong(), Matchers.anyInt()))
            .thenAnswer((Answer<List<BlockHeader>>)invocation -> {
                Object[] arguments = invocation.getArguments();
                return mockHeaders((Long)arguments[0], (int)arguments[1]);
            });
        Mockito.when(blockSyncService.validating(Matchers.anyString(), Matchers.anyList())).thenReturn(false);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }

        //bft validating failed
        Mockito.when(blockSyncService.getHeaders(Matchers.anyLong(), Matchers.anyInt()))
            .thenAnswer((Answer<List<BlockHeader>>)invocation -> {
                Object[] arguments = invocation.getArguments();
                return mockHeaders((Long)arguments[0], (int)arguments[1]);
            });
        Mockito.when(blockSyncService.validating(Matchers.anyString(), Matchers.anyList())).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(Matchers.any())).thenReturn(null, false);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }

        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }
    }

    @Test public void testSyncWithParamGetBlocksFailed() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        Mockito.when(properties.getTryTimes()).thenReturn(times);
        Mockito.when(properties.getBlockStep()).thenReturn(blockStep);
        Mockito.when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        Mockito.when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        Mockito.when(blockSyncService.validating(Matchers.anyString(), Matchers.anyList())).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(Matchers.any())).thenReturn(true);
        Mockito.when(blockSyncService.getBlocks(Matchers.anyLong(), Matchers.anyInt()))
            .thenReturn(null, Collections.emptyList());

        //block null,empty
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_GET_VALIDATING_BLOCKS_FAILED);
        }
    }

    @Test public void testSyncWithParamGetBlocksBftFailed() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        Mockito.when(properties.getTryTimes()).thenReturn(times);
        Mockito.when(properties.getBlockStep()).thenReturn(blockStep);
        Mockito.when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        Mockito.when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        Mockito.when(blockSyncService.validating(Matchers.anyString(), Matchers.anyList())).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(Matchers.any())).thenReturn(true);

        List<Block> blocks = mockBlocks(startHeight, size);
        Mockito.when(blockSyncService.getBlocks(Matchers.anyLong(), Matchers.anyInt())).thenReturn(blocks);
        Mockito.when(blockSyncService.validatingBlocks(Matchers.any(), Matchers.any())).thenReturn(false);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_GET_VALIDATING_BLOCKS_FAILED);
        }
        Mockito.verify(blockSyncService, Mockito.times(times)).getBlocks(Matchers.anyLong(), Matchers.anyInt());
    }

    @Test public void testSyncWithParamGetBlockValid() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        Mockito.when(properties.getTryTimes()).thenReturn(times);
        Mockito.when(properties.getBlockStep()).thenReturn(blockStep);
        Mockito.when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        Mockito.when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        Mockito.when(blockSyncService.validating(Matchers.anyString(), Matchers.anyList())).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(Matchers.any())).thenReturn(true);

        List<Block> blocks = mockBlocks(startHeight, size);
        Mockito.when(blockSyncService.getBlocks(Matchers.anyLong(), Matchers.anyInt())).thenReturn(blocks);
        Mockito.when(blockSyncService.validatingBlocks(Matchers.any(), Matchers.any())).thenReturn(true);
        Mockito.when(blockService.compareBlockHeader(Matchers.any(), Matchers.any())).thenReturn(false);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_SYNC_BLOCK_PERSIST_RESULT_INVALID);
        }
        Mockito.verify(blockService, Mockito.times(1)).compareBlockHeader(Matchers.any(), Matchers.any());
    }

    @Test public void testSyncBlock() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        Mockito.when(properties.getTryTimes()).thenReturn(times);
        Mockito.when(properties.getBlockStep()).thenReturn(blockStep);
        Mockito.when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        Mockito.when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        Mockito.when(blockSyncService.validating(Matchers.anyString(), Matchers.anyList())).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(Matchers.any())).thenReturn(true);

        Mockito.when(blockSyncService.getBlocks(Matchers.anyLong(), Matchers.anyInt()))
            .thenAnswer((Answer<List<Block>>)invocation -> {
                Object[] arguments = invocation.getArguments();
                return mockBlocks((Long)arguments[0], (int)arguments[1]);
            });
        Mockito.when(blockSyncService.validatingBlocks(Matchers.any(), Matchers.any())).thenReturn(true);
        Mockito.when(blockService.compareBlockHeader(Matchers.any(), Matchers.any())).thenReturn(true);
        PackContext pack = Mockito.mock(PackContext.class);
        Block block = Mockito.mock(Block.class);
        BlockHeader theader = Mockito.mock(BlockHeader.class);
        Mockito.when(block.getBlockHeader()).thenReturn(theader);
        Mockito.when(pack.getCurrentBlock()).thenReturn(block);
        Mockito.when(packageService.createPackContext(Matchers.any())).thenReturn(pack);
        syncService.sync(startHeight, size);
//        Mockito.verify(packageService, Mockito.times(size)).persisting(Matchers.any());
    }

    @Test public void testSyncBlockValidatingFailed() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        Mockito.when(properties.getTryTimes()).thenReturn(times);
        Mockito.when(properties.getBlockStep()).thenReturn(blockStep);
        Mockito.when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        Mockito.when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        Mockito.when(blockSyncService.validating(Matchers.anyString(), Matchers.anyList())).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(Matchers.any())).thenReturn(true);

        Mockito.when(blockSyncService.getBlocks(Matchers.anyLong(), Matchers.anyInt()))
            .thenAnswer((Answer<List<Block>>)invocation -> {
                Object[] arguments = invocation.getArguments();
                return mockBlocks((Long)arguments[0], (int)arguments[1]);
            });
        Mockito.when(blockSyncService.validatingBlocks(Matchers.any(), Matchers.any())).thenReturn(true);
        Mockito.when(blockService.compareBlockHeader(Matchers.any(), Matchers.any())).thenReturn(true, false);
        PackContext pack = Mockito.mock(PackContext.class);
        Block block = Mockito.mock(Block.class);
        BlockHeader theader = Mockito.mock(BlockHeader.class);
        Mockito.when(block.getBlockHeader()).thenReturn(theader);
        Mockito.when(pack.getCurrentBlock()).thenReturn(block);
        Mockito.when(packageService.createPackContext(Matchers.any())).thenReturn(pack);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_SYNC_BLOCK_PERSIST_RESULT_INVALID);
        }
    }

    @Test public void testSyncBlockPersistingFailed() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        Mockito.when(properties.getTryTimes()).thenReturn(times);
        Mockito.when(properties.getBlockStep()).thenReturn(blockStep);
        Mockito.when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        Mockito.when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        Mockito.when(blockSyncService.validating(Matchers.anyString(), Matchers.anyList())).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(Matchers.any())).thenReturn(true);

        Mockito.when(blockSyncService.getBlocks(Matchers.anyLong(), Matchers.anyInt()))
            .thenAnswer((Answer<List<Block>>)invocation -> {
                Object[] arguments = invocation.getArguments();
                return mockBlocks((Long)arguments[0], (int)arguments[1]);
            });
        Mockito.when(blockSyncService.validatingBlocks(Matchers.any(), Matchers.any())).thenReturn(true);
        Mockito.when(blockService.compareBlockHeader(Matchers.any(), Matchers.any())).thenReturn(true, true, false);
        PackContext pack = Mockito.mock(PackContext.class);
        Block block = Mockito.mock(Block.class);
        BlockHeader theader = Mockito.mock(BlockHeader.class);
        Mockito.when(block.getBlockHeader()).thenReturn(theader);
        Mockito.when(pack.getCurrentBlock()).thenReturn(block);
        Mockito.when(packageService.createPackContext(Matchers.any())).thenReturn(pack);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), ManagementError.MANAGEMENT_FAILOVER_SYNC_BLOCK_PERSIST_RESULT_INVALID);
        }
    }

    @Test public void testSync() {
        int times = 3, headerStep = 20, blockStep = 10;
        long clusterHeight = 100L, cacheMinHeight = 160;
        AtomicLong blockHeight = new AtomicLong(currentHeight);
        AtomicInteger getHeaderTime = new AtomicInteger();
        Mockito.when(blockRepository.getMaxHeight()).thenReturn(blockHeight.longValue()).thenAnswer(invocation -> {
            return blockHeight.addAndGet(getHeaderTime.incrementAndGet() % 2 == 0 ? headerStep : 0);
        });
        Mockito.when(properties.getThreshold()).thenReturn(50);
        Mockito.when(blockSyncService.getClusterHeight(Matchers.anyInt())).thenReturn(clusterHeight);
        Mockito.when(cache.getMinHeight()).thenReturn(clusterHeight, cacheMinHeight);
        Mockito.when(properties.getTryTimes()).thenReturn(times);
        Mockito.when(properties.getBlockStep()).thenReturn(blockStep);
        Mockito.when(properties.getHeaderStep()).thenReturn(headerStep);
        Mockito.when(blockRepository.getBlockHeader(Matchers.anyLong())).thenAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            BlockHeader header = Mockito.mock(BlockHeader.class);
            Mockito.when(header.getHeight()).thenReturn((Long)arguments[0]);
            return header;
        });
        Mockito.when(blockSyncService.getHeaders(Matchers.anyLong(), Matchers.anyInt()))
            .thenAnswer((Answer<List<BlockHeader>>)invocation -> {
                Object[] arguments = invocation.getArguments();
                return mockHeaders((Long)arguments[0], (int)arguments[1]);
            });
        Mockito.when(blockSyncService.validating(Matchers.anyString(), Matchers.anyList())).thenReturn(true);
        Mockito.when(blockSyncService.bftValidating(Matchers.any())).thenReturn(true);

        Mockito.when(blockSyncService.getBlocks(Matchers.anyLong(), Matchers.anyInt()))
            .thenAnswer((Answer<List<Block>>)invocation -> {
                Object[] arguments = invocation.getArguments();
                return mockBlocks((Long)arguments[0], (int)arguments[1]);
            });
        Mockito.when(blockSyncService.validatingBlocks(Matchers.any(), Matchers.any())).thenReturn(true);
        Mockito.when(blockService.compareBlockHeader(Matchers.any(), Matchers.any())).thenReturn(true);
        PackContext pack = Mockito.mock(PackContext.class);
        Block block = Mockito.mock(Block.class);
        BlockHeader theader = Mockito.mock(BlockHeader.class);
        Mockito.when(block.getBlockHeader()).thenReturn(theader);
        Mockito.when(pack.getCurrentBlock()).thenReturn(block);
        Mockito.when(packageService.createPackContext(Matchers.any())).thenReturn(pack);
        syncService.autoSync();
    }

    private List<BlockHeader> mockHeaders(long startHeight, int size) {
        List<BlockHeader> headers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            BlockHeader mock = Mockito.mock(BlockHeader.class);
            Mockito.when(mock.getHeight()).thenReturn(startHeight + i);
            headers.add(mock);
        }
        return headers;
    }

    private List<Block> mockBlocks(long startHeight, int size) {
        List<Block> headers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Block block = Mockito.mock(Block.class);
            BlockHeader header = Mockito.mock(BlockHeader.class);
            Mockito.when(header.getHeight()).thenReturn(startHeight + i);
            Mockito.when(block.getBlockHeader()).thenReturn(header);
            headers.add(block);
        }
        return headers;
    }
}
package com.higgs.trust.slave.core.service.failover;

import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.context.PackContext;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;
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

    long currentHeight = 1;
    @Mock BlockHeader header;

    @Mock List<Block> blocks;

    @BeforeMethod public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        reset(properties, nodeState);
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(true);
        when(nodeState.isState(NodeStateEnum.AutoSync, NodeStateEnum.ArtificialSync)).thenReturn(true);
        when(blockRepository.getMaxHeight()).thenReturn(currentHeight);
    }

    @Test public void testSyncNotState() {
        when(nodeState.isState(NodeStateEnum.AutoSync)).thenReturn(false);
        syncService.sync();
        verify(blockRepository, times(0)).getMaxHeight();
    }

    @Test public void testSyncGetClusterHeightFailed() {
        when(blockSyncService.getClusterHeight(any())).thenReturn(null);
        syncService.sync();
        verify(nodeState, times(1)).changeState(NodeStateEnum.AutoSync, NodeStateEnum.Offline);
        verify(nodeState, times(0)).isState(NodeStateEnum.AutoSync, NodeStateEnum.ArtificialSync);
    }

    @Test public void testSyncInThreshold() {
        when(blockSyncService.getClusterHeight(any())).thenReturn(101L);
        when(properties.getThreshold()).thenReturn(100);
        syncService.sync();
        verify(nodeState, times(1)).changeState(NodeStateEnum.AutoSync, NodeStateEnum.Running);
        verify(nodeState, times(0)).isState(NodeStateEnum.AutoSync, NodeStateEnum.ArtificialSync);
    }

    @Test public void testSyncOutThreshold() {
        when(blockSyncService.getClusterHeight(any())).thenReturn(102L);
        when(properties.getThreshold()).thenReturn(100);
        syncService.sync();
        verify(nodeState, times(1)).isState(NodeStateEnum.AutoSync, NodeStateEnum.ArtificialSync);
        verify(nodeState, times(1)).changeState(NodeStateEnum.AutoSync, NodeStateEnum.Offline);
    }

    @Test public void testSyncWithParamNotState() {
        long startHeight = currentHeight + 1;
        int size = 10;
        when(nodeState.isState(NodeStateEnum.AutoSync, NodeStateEnum.ArtificialSync)).thenReturn(false);
        try {
            syncService.sync(anyLong(), anyInt());
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_STATE_NOT_ALLOWED);
        }
        verify(blockRepository, times(0)).getMaxHeight();
    }

    @Test public void testSyncWithParamHeightNotCurrent() {
        long startHeight = currentHeight + 1;
        int size = 10;
        try {
            syncService.sync(currentHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_START_HEIGHT_ERROR);
        }
    }

    @Test public void testSyncWithParamGetHeadersFailed() {
        long startHeight = currentHeight + 1;
        int size = 10;
        Integer times = 3;
        when(properties.getTryTimes()).thenReturn(times);
        when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        when(blockSyncService.getHeaders(startHeight, size)).thenReturn(null, Collections.emptyList());
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }

        //try times test
        reset(properties);
        when(properties.getTryTimes()).thenReturn(times);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }
        verify(properties, times(times)).getTryTimes();

        //validating headers failed
        when(blockSyncService.getHeaders(anyLong(), anyInt())).thenAnswer((Answer<List<BlockHeader>>)invocation -> {
            Object[] arguments = invocation.getArguments();
            return mockHeaders((Long)arguments[0], (int)arguments[1]);
        });
        when(blockSyncService.validating(anyString(), anyList())).thenReturn(false);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }

        //bft validating failed
        when(blockSyncService.getHeaders(anyLong(), anyInt())).thenAnswer((Answer<List<BlockHeader>>)invocation -> {
            Object[] arguments = invocation.getArguments();
            return mockHeaders((Long)arguments[0], (int)arguments[1]);
        });
        when(blockSyncService.validating(anyString(), anyList())).thenReturn(true);
        when(blockSyncService.bftValidating(any())).thenReturn(null, false);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }

        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }
    }

    @Test public void testSyncWithParamGetBlocksFailed() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        when(properties.getTryTimes()).thenReturn(times);
        when(properties.getBlockStep()).thenReturn(blockStep);
        when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        when(blockSyncService.validating(anyString(), anyList())).thenReturn(true);
        when(blockSyncService.bftValidating(any())).thenReturn(true);
        when(blockSyncService.getBlocks(anyLong(), anyInt())).thenReturn(null, Collections.emptyList());

        //block null,empty
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_BLOCKS_FAILED);
        }
    }

    @Test public void testSyncWithParamGetBlocksBftFailed() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        when(properties.getTryTimes()).thenReturn(times);
        when(properties.getBlockStep()).thenReturn(blockStep);
        when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        when(blockSyncService.validating(anyString(), anyList())).thenReturn(true);
        when(blockSyncService.bftValidating(any())).thenReturn(true);

        List<Block> blocks = mockBlocks(startHeight, size);
        when(blockSyncService.getBlocks(anyLong(), anyInt())).thenReturn(blocks);
        when(blockSyncService.validatingBlocks(any(), any())).thenReturn(false);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_BLOCKS_FAILED);
        }
        verify(blockSyncService, times(times)).getBlocks(anyLong(), anyInt());
    }

    @Test public void testSyncWithParamGetBlockValid() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        when(properties.getTryTimes()).thenReturn(times);
        when(properties.getBlockStep()).thenReturn(blockStep);
        when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        when(blockSyncService.validating(anyString(), anyList())).thenReturn(true);
        when(blockSyncService.bftValidating(any())).thenReturn(true);

        List<Block> blocks = mockBlocks(startHeight, size);
        when(blockSyncService.getBlocks(anyLong(), anyInt())).thenReturn(blocks);
        when(blockSyncService.validatingBlocks(any(), any())).thenReturn(true);
        when(blockService.compareBlockHeader(any(), any())).thenReturn(false);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_SYNC_BLOCK_VALIDATING_FAILED);
        }
        verify(blockService, times(1)).compareBlockHeader(any(), any());
    }

    @Test public void testSyncBlock() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        when(properties.getTryTimes()).thenReturn(times);
        when(properties.getBlockStep()).thenReturn(blockStep);
        when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        when(blockSyncService.validating(anyString(), anyList())).thenReturn(true);
        when(blockSyncService.bftValidating(any())).thenReturn(true);

        when(blockSyncService.getBlocks(anyLong(), anyInt())).thenAnswer((Answer<List<Block>>)invocation -> {
            Object[] arguments = invocation.getArguments();
            return mockBlocks((Long)arguments[0], (int)arguments[1]);
        });
        when(blockSyncService.validatingBlocks(any(), any())).thenReturn(true);
        when(blockService.compareBlockHeader(any(), any())).thenReturn(true);
        PackContext pack = mock(PackContext.class);
        Block block = mock(Block.class);
        BlockHeader theader = mock(BlockHeader.class);
        when(block.getBlockHeader()).thenReturn(theader);
        when(pack.getCurrentBlock()).thenReturn(block);
        when(packageService.createPackContext(any())).thenReturn(pack);
        syncService.sync(startHeight, size);
        verify(packageService, times(size)).persisting(any());
    }

    @Test public void testSyncBlockValidatingFailed() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        when(properties.getTryTimes()).thenReturn(times);
        when(properties.getBlockStep()).thenReturn(blockStep);
        when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        when(blockSyncService.validating(anyString(), anyList())).thenReturn(true);
        when(blockSyncService.bftValidating(any())).thenReturn(true);

        when(blockSyncService.getBlocks(anyLong(), anyInt())).thenAnswer((Answer<List<Block>>)invocation -> {
            Object[] arguments = invocation.getArguments();
            return mockBlocks((Long)arguments[0], (int)arguments[1]);
        });
        when(blockSyncService.validatingBlocks(any(), any())).thenReturn(true);
        when(blockService.compareBlockHeader(any(), any())).thenReturn(true, false);
        PackContext pack = mock(PackContext.class);
        Block block = mock(Block.class);
        BlockHeader theader = mock(BlockHeader.class);
        when(block.getBlockHeader()).thenReturn(theader);
        when(pack.getCurrentBlock()).thenReturn(block);
        when(packageService.createPackContext(any())).thenReturn(pack);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_SYNC_BLOCK_VALIDATING_FAILED);
        }
    }

    @Test public void testSyncBlockPersistingFailed() {
        long startHeight = currentHeight + 1;
        int size = 100, times = 3, blockStep = 10;
        List<BlockHeader> headers = mockHeaders(startHeight, size);
        when(properties.getTryTimes()).thenReturn(times);
        when(properties.getBlockStep()).thenReturn(blockStep);
        when(blockRepository.getBlockHeader(currentHeight)).thenReturn(header);
        when(blockSyncService.getHeaders(startHeight, size)).thenReturn(headers);
        when(blockSyncService.validating(anyString(), anyList())).thenReturn(true);
        when(blockSyncService.bftValidating(any())).thenReturn(true);

        when(blockSyncService.getBlocks(anyLong(), anyInt())).thenAnswer((Answer<List<Block>>)invocation -> {
            Object[] arguments = invocation.getArguments();
            return mockBlocks((Long)arguments[0], (int)arguments[1]);
        });
        when(blockSyncService.validatingBlocks(any(), any())).thenReturn(true);
        when(blockService.compareBlockHeader(any(), any())).thenReturn(true, true, false);
        PackContext pack = mock(PackContext.class);
        Block block = mock(Block.class);
        BlockHeader theader = mock(BlockHeader.class);
        when(block.getBlockHeader()).thenReturn(theader);
        when(pack.getCurrentBlock()).thenReturn(block);
        when(packageService.createPackContext(any())).thenReturn(pack);
        try {
            syncService.sync(startHeight, size);
        } catch (SlaveException e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_SYNC_BLOCK_PERSIST_RESULT_INVALID);
        }
    }

    @Test public void testSync() {
        int times = 3, headerStep = 20, blockStep = 10;
        long clusterHeight = 100L, cacheMinHeight = 160;
        AtomicLong blockHeight = new AtomicLong(currentHeight);
        AtomicInteger getHeaderTime = new AtomicInteger();
        when(blockRepository.getMaxHeight()).thenReturn(blockHeight.longValue()).thenAnswer(invocation -> {
            return blockHeight.addAndGet(getHeaderTime.incrementAndGet() % 2 == 0 ? headerStep : 0);
        });
        when(properties.getThreshold()).thenReturn(50);
        when(blockSyncService.getClusterHeight(any())).thenReturn(clusterHeight);
        when(cache.getMinHeight()).thenReturn(SyncPackageCache.INIT_HEIGHT, cacheMinHeight);
        when(properties.getTryTimes()).thenReturn(times);
        when(properties.getBlockStep()).thenReturn(blockStep);
        when(properties.getHeaderStep()).thenReturn(headerStep);
        when(blockRepository.getBlockHeader(anyLong())).thenAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            BlockHeader header = mock(BlockHeader.class);
            when(header.getHeight()).thenReturn((Long)arguments[0]);
            return header;
        });
        when(blockSyncService.getHeaders(anyLong(), anyInt())).thenAnswer((Answer<List<BlockHeader>>)invocation -> {
            Object[] arguments = invocation.getArguments();
            return mockHeaders((Long)arguments[0], (int)arguments[1]);
        });
        when(blockSyncService.validating(anyString(), anyList())).thenReturn(true);
        when(blockSyncService.bftValidating(any())).thenReturn(true);

        when(blockSyncService.getBlocks(anyLong(), anyInt())).thenAnswer((Answer<List<Block>>)invocation -> {
            Object[] arguments = invocation.getArguments();
            return mockBlocks((Long)arguments[0], (int)arguments[1]);
        });
        when(blockSyncService.validatingBlocks(any(), any())).thenReturn(true);
        when(blockService.compareBlockHeader(any(), any())).thenReturn(true);
        PackContext pack = mock(PackContext.class);
        Block block = mock(Block.class);
        BlockHeader theader = mock(BlockHeader.class);
        when(block.getBlockHeader()).thenReturn(theader);
        when(pack.getCurrentBlock()).thenReturn(block);
        when(packageService.createPackContext(any())).thenReturn(pack);
        syncService.sync();
        verify(nodeState, times(1)).changeState(NodeStateEnum.AutoSync, NodeStateEnum.Running);
    }

    private List<BlockHeader> mockHeaders(long startHeight, int size) {
        List<BlockHeader> headers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            BlockHeader mock = mock(BlockHeader.class);
            when(mock.getHeight()).thenReturn(startHeight + i);
            headers.add(mock);
        }
        return headers;
    }

    private List<Block> mockBlocks(long startHeight, int size) {
        List<Block> headers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Block block = mock(Block.class);
            BlockHeader header = mock(BlockHeader.class);
            when(header.getHeight()).thenReturn(startHeight + i);
            when(block.getBlockHeader()).thenReturn(header);
            headers.add(block);
        }
        return headers;
    }
}
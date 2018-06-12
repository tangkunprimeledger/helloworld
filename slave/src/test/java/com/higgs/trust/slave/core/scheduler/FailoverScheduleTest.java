package com.higgs.trust.slave.core.scheduler;

import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.FailoverExecption;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.failover.BlockSyncService;
import com.higgs.trust.slave.core.service.failover.FailoverProperties;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@RunWith(PowerMockRunner.class) public class FailoverScheduleTest {

    @InjectMocks @Autowired FailoverSchedule failoverSchedule;

    @Mock BlockSyncService blockSyncService;
    @Mock BlockService blockService;
    @Mock PackageService packageService;
    @Mock BlockRepository blockRepository;
    @Mock PackageRepository packageRepository;
    @Mock NodeState nodeState;
    @Mock FailoverProperties properties;
    @Mock Package pack;
    @Mock Block currentBlock;
    @Mock BlockHeader currentHeader;
    @Mock TransactionTemplate txNested;
    private String currentHash = "preHash";

    @BeforeClass public void before() {
        MockitoAnnotations.initMocks(this);
        when(currentBlock.getBlockHeader()).thenReturn(currentHeader);
    }

    @BeforeMethod public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        when(currentBlock.getBlockHeader()).thenReturn(currentHeader);
        when(currentHeader.getBlockHash()).thenReturn(currentHash);
        when(txNested.execute(any(TransactionCallbackWithoutResult.class))).thenAnswer(invocation -> {
            TransactionCallback o = (TransactionCallback)invocation.getArguments()[0];
            return o.doInTransaction(new SimpleTransactionStatus());
        });
    }

    @Test public void testFailoverNotRunning() {
        when(nodeState.isState(NodeStateEnum.Running)).thenReturn(false);
        failoverSchedule.failover();
        verify(blockRepository, times(0)).getBlockHeader(anyLong());
    }

    @Test public void testFailoverNotHeight() {
        long height = 1L;
        when(nodeState.isState(NodeStateEnum.Running)).thenReturn(true);
        when(blockService.getMaxHeight()).thenReturn(height);
        when(packageRepository.getMinHeight(height + 1, Collections.singleton(PackageStatusEnum.RECEIVED.getCode())))
            .thenReturn(null, height + 2);
        failoverSchedule.failover();
        verify(blockRepository, times(0)).getBlockHeader(anyLong());
        verify(packageRepository, times(0)).load(anyLong());

        Package pack = mock(Package.class);

        when(packageRepository.load(height + 1)).thenReturn(pack);
        failoverSchedule.failover();
        verify(blockRepository, times(0)).getBlockHeader(anyLong());

        when(pack.getStatus()).thenReturn(PackageStatusEnum.RECEIVED);
        failoverSchedule.failover();
        verify(blockRepository, times(0)).getBlockHeader(anyLong());

        when(pack.getStatus()).thenReturn(PackageStatusEnum.FAILOVER);
        failoverSchedule.failover();
        verify(blockRepository, times(0)).getBlockHeader(anyLong());
    }

    @Test public void testFailoverNotPackage() {
        long height = 1L;
        when(nodeState.isState(NodeStateEnum.Running)).thenReturn(true);
        when(blockService.getMaxHeight()).thenReturn(height);
        when(packageRepository.getMinHeight(height + 1, Collections.singleton(PackageStatusEnum.RECEIVED.getCode())))
            .thenReturn(height + 2);
        when(packageRepository.load(height + 1)).thenReturn(null);

        failoverSchedule.failover();

        verify(blockRepository, times(0)).getBlockHeader(anyLong());
    }

    @Test public void testFailoverStep() {
        long height = 2L;
        int times = 5;
        when(properties.getFailoverStep()).thenReturn(times);
        when(nodeState.isState(NodeStateEnum.Running)).thenReturn(true);
        when(nodeState.isState(NodeStateEnum.Running, NodeStateEnum.ArtificialSync)).thenReturn(true);
        when(blockService.getMaxHeight()).thenReturn(height - 1);

        when(packageRepository.getMinHeight(height, Collections.singleton(PackageStatusEnum.RECEIVED.getCode())))
            .thenReturn(height + 1);
        when(packageRepository.load(height)).thenReturn(null);
        when(blockRepository.getBlockHeader(height - 1)).thenReturn(currentHeader);

        ArrayList<Block> blocks = new ArrayList<>();
        Block block = mock(Block.class);
        BlockHeader header = mock(BlockHeader.class);
        when(header.getHeight()).thenReturn(height);
        when(block.getBlockHeader()).thenReturn(header);
        blocks.add(block);
        when(blockSyncService.getBlocks(height, 1)).thenReturn(blocks);
        doReturn(true).when(blockSyncService).validating(currentHash, block);

        when(blockService.getHeader(height)).thenReturn(header);
        PackContext context = mock(PackContext.class);
        when(packageService.createPackContext(any())).thenReturn(context);
        when(context.getCurrentBlock()).thenReturn(block);

        when(blockService.compareBlockHeader(header, header)).thenReturn(true);
        failoverSchedule.failover();
        verify(properties, times(times)).getFailoverStep();
    }

    @Test public void testFailoverSLAVEException() {
        long height = 2L;
        when(nodeState.isState(NodeStateEnum.Running)).thenReturn(true);
        when(nodeState.isState(NodeStateEnum.Running, NodeStateEnum.ArtificialSync)).thenReturn(true);
        when(blockService.getMaxHeight()).thenReturn(height - 1);

        when(packageRepository.getMinHeight(height, Collections.singleton(PackageStatusEnum.RECEIVED.getCode())))
            .thenReturn(height + 1);
        when(packageRepository.load(height)).thenReturn(null);
        when(blockRepository.getBlockHeader(height - 1)).thenReturn(currentHeader);

        ArrayList<Block> blocks = new ArrayList<>();
        Block block = mock(Block.class);
        BlockHeader header = mock(BlockHeader.class);
        when(header.getHeight()).thenReturn(height);
        when(block.getBlockHeader()).thenReturn(header);
        blocks.add(block);
        when(blockSyncService.getBlocks(height, 1)).thenReturn(blocks);
        doReturn(true).when(blockSyncService).validating(currentHash, block);

        when(blockService.getHeader(height)).thenReturn(header);
        PackContext context = mock(PackContext.class);
        when(packageService.createPackContext(any())).thenReturn(context);
        when(context.getCurrentBlock()).thenReturn(block);

        when(blockService.getHeader(height)).thenReturn(header);
        when(blockService.compareBlockHeader(header, header)).thenReturn(true, false);
        failoverSchedule.failover();
        verify(nodeState, times(1)).changeState(NodeStateEnum.Running, NodeStateEnum.Offline);
    }

    @Test public void testFailoverOtherException() {
        when(nodeState.isState(NodeStateEnum.Running)).thenReturn(true);
        when(nodeState.isState(NodeStateEnum.Running, NodeStateEnum.ArtificialSync)).thenReturn(true);
        when(blockService.getMaxHeight()).thenThrow(Exception.class);

        failoverSchedule.failover();
        verify(nodeState, times(0)).changeState(NodeStateEnum.Running, NodeStateEnum.Offline);
        verify(properties, times(0)).getFailoverStep();
    }

    @Test public void testFailoverHeightFalse() {
        long height = 1L;
        ArrayList<Block> blocks = new ArrayList<>();
        Block block = mock(Block.class);
        blocks.add(block);
        when(blockSyncService.getBlocks(height, 1)).thenReturn(blocks);
        when(blockRepository.getBlockHeader(height - 1)).thenReturn(currentHeader);
        doReturn(true).when(blockSyncService).validating(currentHash, block);

        //不存在比较高的初始package
        when(nodeState.isState(NodeStateEnum.Running, NodeStateEnum.ArtificialSync)).thenReturn(true);
        when(packageRepository.getMinHeight(height, Collections.singleton(PackageStatusEnum.RECEIVED.getCode())))
            .thenReturn(null, height + 1);

        assertFalse(failoverSchedule.failover(height));

        //当前处理高度package不为FAILOVER
        when(packageRepository.load(height)).thenReturn(pack);
        when(pack.getStatus()).thenReturn(PackageStatusEnum.RECEIVED);
        assertFalse(failoverSchedule.failover(height));

        //package不存在，插入失败
        when(packageRepository.load(height)).thenReturn(null);
        doThrow(Exception.class).when(packageRepository).save(any());
        assertFalse(failoverSchedule.failover(height));
    }

    @Test public void testFailoverHeight() {
        long height = 2L;
        when(nodeState.isState(NodeStateEnum.Running, NodeStateEnum.ArtificialSync)).thenReturn(true);
        when(packageRepository.getMinHeight(height, Collections.singleton(PackageStatusEnum.RECEIVED.getCode())))
            .thenReturn(height + 1);
        when(packageRepository.load(height)).thenReturn(null);
        when(blockRepository.getBlockHeader(height - 1)).thenReturn(currentHeader);
        when(blockSyncService.getBlocks(height, 1)).thenReturn(null, new ArrayList<>());
        int times = 5;
        when(properties.getTryTimes()).thenReturn(times);

        try {
            failoverSchedule.failover(height);
        } catch (FailoverExecption e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_BLOCKS_FAILED);
        }
        verify(blockSyncService, times(times)).getBlocks(height, 1);
    }

    @Test public void testFailoverBlock() {
        long height = 2L;
        when(nodeState.isState(NodeStateEnum.Running, NodeStateEnum.ArtificialSync)).thenReturn(true);
        when(packageRepository.getMinHeight(height, Collections.singleton(PackageStatusEnum.RECEIVED.getCode())))
            .thenReturn(height + 1);
        when(packageRepository.load(height)).thenReturn(null);
        when(blockRepository.getBlockHeader(height - 1)).thenReturn(currentHeader);

        ArrayList<Block> blocks = new ArrayList<>();
        Block block = mock(Block.class);
        BlockHeader header = mock(BlockHeader.class);
        when(header.getHeight()).thenReturn(height);
        when(block.getBlockHeader()).thenReturn(header);
        blocks.add(block);
        when(blockSyncService.getBlocks(height, 1)).thenReturn(blocks);
        doReturn(true).when(blockSyncService).validating(currentHash, block);


        //no validate header
        when(blockService.getHeader(height)).thenReturn(null);
        try {
            failoverSchedule.failover(height);
        } catch (FailoverExecption e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_CONSENSUS_VALIDATE_NOT_EXIST);
        }

        //header compare: validating failed, has validate header
        when(blockService.getHeader(height)).thenReturn(header);
        PackContext context = mock(PackContext.class);
        when(packageService.createPackContext(any())).thenReturn(context);
        when(context.getCurrentBlock()).thenReturn(block);

        when(blockService.compareBlockHeader(header, header)).thenReturn(false);
        try {
            failoverSchedule.failover(height);
        } catch (FailoverExecption e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_BLOCK_VALIDATE_RESULT_INVALID);
        }

        //header compare: validating passed, no persist header
        when(blockService.compareBlockHeader(header, header)).thenReturn(true);
        when(blockService.getHeader(height)).thenReturn(null);
        try {
            failoverSchedule.failover(height);
        } catch (FailoverExecption e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_CONSENSUS_PERSIST_NOT_EXIST);
        }

        //header compare: validating passed，persisting failed
        when(blockService.getHeader(height)).thenReturn(header);
        when(blockService.compareBlockHeader(header, header)).thenReturn(true, false);
        try {
            failoverSchedule.failover(height);
        } catch (FailoverExecption e) {
            assertEquals(e.getCode(), SlaveErrorEnum.SLAVE_FAILOVER_BLOCK_PERSIST_RESULT_INVALID);
        }
        //header compare:  validating passed，persisting passed
        when(blockService.compareBlockHeader(header, header)).thenReturn(true);
        assertTrue(failoverSchedule.failover(height));
    }

}
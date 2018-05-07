package com.higgs.trust.slave.core.service.failover;

import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.block.hash.TxRootHashBuilder;
import com.higgs.trust.slave.core.service.consensus.cluster.ClusterService;
import com.higgs.trust.slave.integration.block.BlockChainClient;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.StateRootHash;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@RunWith(PowerMockRunner.class) public class BlockSyncServiceTest {

    @InjectMocks @Autowired private BlockSyncService blockSyncService;

    @Mock private BlockService blockService;

    @Mock private BlockChainClient blockChainClient;

    @Mock private TxRootHashBuilder txRootHashBuilder;

    @Mock private NodeState nodeState;

    @Mock private ClusterService clusterService;

    @BeforeClass public void before() {
        MockitoAnnotations.initMocks(this);
    }

    private BlockHeader mockFirstHeader() {
        BlockHeader header = mock(BlockHeader.class);
        when(header.getHeight()).thenReturn(0L);
        when(header.getBlockHash()).thenReturn("0");
        when(header.getPreviousHash()).thenReturn("0");
        StateRootHash stateRootHash = mock(StateRootHash.class);
        when(header.getStateRootHash()).thenReturn(stateRootHash);
        when(stateRootHash.getTxRootHash()).thenReturn("0");
        when(blockService.buildBlockHash(header)).thenReturn("0");
        return header;
    }

    private List<BlockHeader> mockHeaders(int size) {
        List<BlockHeader> headers = new ArrayList<>();
        BlockHeader header = mockFirstHeader();
        headers.add(header);
        int i = 1;
        while (i++ < size) {
            header = mockHeader(header);
            headers.add(header);
        }
        return headers;
    }

    private BlockHeader mockHeader(BlockHeader preHeader) {
        BlockHeader header = mock(BlockHeader.class);
        Long preHeight = preHeader.getHeight();
        when(header.getPreviousHash()).thenReturn(""+preHeight);
        long height = preHeight + 1;
        when(header.getHeight()).thenReturn(height);
        when(header.getBlockHash()).thenReturn("" + height);
        StateRootHash stateRootHash = mock(StateRootHash.class);
        when(header.getStateRootHash()).thenReturn(stateRootHash);
        when(stateRootHash.getTxRootHash()).thenReturn("" + height);
        when(blockService.buildBlockHash(header)).thenReturn("" + height);
        return header;
    }

    private List<Block> mockBlocks(int size) {
        List<Block> blocks = new ArrayList<>();
        Block block = mock(Block.class);
        BlockHeader header = mockFirstHeader();
        when(block.getBlockHeader()).thenReturn(header);
        List<SignedTransaction> singedTxList = mock(List.class);
        when(block.getSignedTxList()).thenReturn(singedTxList);
        Long height = header.getHeight();
        when(txRootHashBuilder.buildTxs(singedTxList)).thenReturn("" + height);
        blocks.add(block);
        int i = 1;
        while (i++ < size) {
            block = mockBlock(block);
            blocks.add(block);
        }
        return blocks;
    }

    private Block mockBlock(Block preBlock) {
        Block block = mock(Block.class);
        BlockHeader preHeader = preBlock.getBlockHeader();
        BlockHeader blockHeader = mockHeader(preHeader);
        when(block.getBlockHeader()).thenReturn(blockHeader);
        List<SignedTransaction> singedTxList = mock(List.class);
        when(block.getSignedTxList()).thenReturn(singedTxList);
        Long height = blockHeader.getHeight();
        when(txRootHashBuilder.buildTxs(singedTxList)).thenReturn("" + height);
        return block;
    }

    @Test public void testValidatingHeader() {
        BlockHeader header = mock(BlockHeader.class);
        when(header.getHeight()).thenReturn(0L);
        when(header.getBlockHash()).thenReturn("0");
        when(blockService.buildBlockHash(header)).thenReturn("0", "1");
        assertTrue(blockSyncService.validating(header));
        assertFalse(blockSyncService.validating(header));
    }

    @Test public void testValidatingHeaderWithPer() {
        List<BlockHeader> headers = mockHeaders(3);
        BlockHeader header0 = headers.get(0);
        assertTrue(blockSyncService.validating(header0.getBlockHash(), headers.get(1)));
        assertTrue(blockSyncService.validating(headers.get(1).getBlockHash(), headers.get(2)));
        assertFalse(blockSyncService.validating(header0.getBlockHash(), headers.get(2)));
        assertFalse(blockSyncService.validating("", headers.get(2)));
    }

    @Test public void testValidatingHeaders() {
        List<BlockHeader> headers = mockHeaders(10);
        BlockHeader header0 = headers.get(0);
        assertTrue(blockSyncService.validating(header0.getPreviousHash(), headers));
        assertTrue(blockSyncService.validating(headers));
        headers.remove(5);
        assertFalse(blockSyncService.validating(header0.getPreviousHash(), headers));
        assertFalse(blockSyncService.validating(null, headers));
        assertFalse(blockSyncService.validating("", headers));
    }

    @Test public void testValidatingBlock() {
        List<Block> blocks = mockBlocks(2);
        assertTrue(blockSyncService.validating(blocks.get(0)));

        BlockHeader blockHeader = blocks.get(0).getBlockHeader();
        when(blockService.buildBlockHash(blockHeader)).thenReturn("1");
        assertFalse(blockSyncService.validating(blocks.get(0)));

        assertTrue(blockSyncService.validating(blocks.get(1)));

        List<SignedTransaction> signedTxList = blocks.get(1).getSignedTxList();
        when(txRootHashBuilder.buildTxs(signedTxList)).thenReturn("0");
        assertFalse(blockSyncService.validating(blocks.get(1)));
    }

    @Test public void testValidatingBlockPre() {
        List<Block> blocks = mockBlocks(1);
        Block block = blocks.get(0);
        assertTrue(blockSyncService.validating(block.getBlockHeader().getBlockHash(), block));
        assertFalse(blockSyncService.validating("sdf", block));
        assertFalse(blockSyncService.validating("sdf", block));
        assertFalse(blockSyncService.validating("", block));
        assertFalse(blockSyncService.validating(null, block));
    }

    @Test public void testValidatingBlocks() {
        List<Block> bloks = mockBlocks(10);
        String previousHash = bloks.get(0).getBlockHeader().getPreviousHash();
        boolean result = blockSyncService.validatingBlocks(previousHash, bloks);
        assertTrue(result);

        bloks.remove(5);
        assertFalse(blockSyncService.validatingBlocks(previousHash, bloks));

        assertFalse(blockSyncService.validatingBlocks(previousHash, null));
        assertFalse(blockSyncService.validatingBlocks("", bloks));
        assertFalse(blockSyncService.validatingBlocks(null, null));
    }
}
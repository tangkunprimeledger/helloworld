import io.atomix.protocols.raft.partition.impl.RaftNamespaces;
import io.atomix.storage.StorageLevel;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.utils.serializer.Serializer;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author suimi
 * @date 2018/8/23
 */
public class TestSegment {

    @Test public void testLoadSegment() {

        Serializer se =Serializer.using(RaftNamespaces.RAFT_STORAGE);
        SegmentedJournal segmentedJournal =
            new SegmentedJournal("raft-group-partition-1", StorageLevel.DISK, new File("e:/tmp/3"), se, 1024  *256* 10,
                1024, 1024 * 256, 0, 0);
    }
}

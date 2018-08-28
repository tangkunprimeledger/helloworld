import io.atomix.protocols.raft.partition.impl.RaftNamespaces;
import io.atomix.storage.StorageLevel;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.utils.serializer.Namespace;
import io.atomix.utils.serializer.Namespaces;
import io.atomix.utils.serializer.Serializer;
import org.bouncycastle.util.encoders.Hex;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author suimi
 * @date 2018/8/23
 */
public class TestSegment {

    @Test public void testLoadSegment() {

        Serializer se = Serializer.using(RaftNamespaces.RAFT_STORAGE);
        SegmentedJournal segmentedJournal =
            new SegmentedJournal("raft-group-partition-1", StorageLevel.DISK, new File("e:/tmp/3"), se, 1024 * 256 * 10,
                1024, 1024 * 256, 0, 0);
    }

    @Test public void testKryoCompatibility() {
        Serializer se = Serializer.using(
            Namespace.builder().register(Namespaces.BASIC).register(TestA.class).setRegistrationRequired(false).setCompatible(true)
                .build());
        TestB testB = new TestB();
        testB.setB("b name");

        TestA testA = new TestA();
        testA.setB(testB);
        testA.setName("a name");
        byte[] encode = se.encode(testA);
        System.out.println("encode = " + Hex.toHexString(encode));
        byte[] decode = Hex.decode("3a010282626e616de514010054657374c201018262070162206e616de50000070161206e616de500");
        Object obj = se.decode(decode);
        System.out.println("obj = " + obj);
    }
}

package tributary;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tributary.api.ConsumerController;
import tributary.api.Message;
import tributary.api.ProducerController;
import tributary.api.TributaryController;
import tributary.api.enums.AllocationType;
import tributary.api.enums.RebalancingType;
import tributary.api.res.ConsumerGroupResponse;
import tributary.api.res.TopicResponse;
import tributary.cli.TributaryCLI;

public class TributaryTest {
    // UNIT TESTS
    // TESTS ON SEPARATE FEATURES IN THE SYSTEM
    @Test
    public void testClusterCreate() {
        assertDoesNotThrow(() -> new TributaryController());
    }

    @Test
    public void testClusterTopicCreate() {
        TributaryController c = new TributaryController();

        c.createTopic("new", null);
        assertTrue((c.showTopic("new")).getTopicId().equals("new"));
    }

    @Test
    public void testClusterPartitionCreate() {
        TributaryController c = new TributaryController();

        c.createTopic("new", null);

        for (int i = 0; i < 5; i++) {
            c.createPartition(String.valueOf(i), "new");

            assertTrue(c.showTopic("new").getPartitionIds().size() == i + 1);
        }
    }

    @Test
    public void testClusterMultipleTopicPartitions() {
        TributaryController c = new TributaryController();

        c.createTopic("new", null);
        c.createTopic("new2", String.class);
        c.createTopic("new3", String.class);

        assertTrue((c.showTopic("new")).getTopicId().equals("new"));
        assertTrue((c.showTopic("new2")).getTopicId().equals("new2"));
        assertTrue((c.showTopic("new3")).getTopicId().equals("new3"));

        for (int i = 0; i < 5; i++) {
            c.createPartition(String.valueOf(i), "new");
            c.createPartition(String.valueOf(i), "new2");
            c.createPartition(String.valueOf(i), "new3");

            assertTrue(c.showTopic("new").getPartitionIds().size() == i + 1);
            assertTrue(c.showTopic("new2").getPartitionIds().size() == i + 1);
            assertTrue(c.showTopic("new3").getPartitionIds().size() == i + 1);
        }
    }

    @Test
    public void testConsumerAssign() {
        TributaryController c = new TributaryController();

        c.createTopic("new", null);
        c.createPartition("1", "new");

        ConsumerController con = new ConsumerController("g1", "new", RebalancingType.RoundRobin);
        ConsumerController con2 = new ConsumerController("g2", "new", RebalancingType.RoundRobin);

        con.createConsumer("con1");

        ConsumerGroupResponse res1 = con.showConsumerGroup();
        assertTrue(res1.getConsumerIds().contains("con1"));
        ConsumerGroupResponse res2 = con2.showConsumerGroup();
        assertTrue(res2.getConsumerIds().size() == 0);

    }

    @Test
    public void testProducerProduce() {
        TributaryController c = new TributaryController();

        c.createTopic("new", String.class);
        c.createPartition("1", "new");

        ProducerController p = new ProducerController("prod1", AllocationType.Manual, String.class);

        Message<?> msg = new Message<String>(LocalDateTime.now(), "msg1", "1", "hello human");
        p.produce("new", msg);

        assertTrue(c.showTopic("new").getMessages("1").size() == 1);
    }

    @Test
    public void testConsumerConsume() {
        TributaryController c = new TributaryController();

        c.createTopic("new", null);
        c.createPartition("1", "new");

        ProducerController p = new ProducerController("prod1", AllocationType.Manual, String.class);
        Message<?> msg = new Message<String>(LocalDateTime.now(), "msg1", "1", "hello human");
        p.produce("new", msg);

        ConsumerController con = new ConsumerController("g1", "new", RebalancingType.RoundRobin);
        con.createConsumer("con1");

        assertTrue(con.consume("con1", "1") != null);
    }

    @Test
    public void testConsumerPlayback() {
        TributaryController c = new TributaryController();

        c.createTopic("new", null);
        c.createPartition("1", "new");
        c.createTopic("new2", null);
        c.createPartition("2", "new2");

        ProducerController p = new ProducerController("prod1", AllocationType.Manual, String.class);
        ProducerController p2 = new ProducerController("prod2", AllocationType.Manual, String.class);
        ProducerController p3 = new ProducerController("prod3", AllocationType.Manual, String.class);
        Message<?> msg = new Message<String>(LocalDateTime.now(), "msg1", "1", "hello human");
        p.produce("new", msg);
        Message<?> msg2 = new Message<Integer>(LocalDateTime.now(), "msg2", "1", 100);
        p2.produce("new", msg2);
        Message<?> msg3 = new Message<Double>(LocalDateTime.now(), "msg3", "2", 101.01);
        p3.produce("new2", msg3);

        ConsumerController con = new ConsumerController("g1", "new", RebalancingType.RoundRobin);
        con.createConsumer("con1");

        con.consume("con1", "1");
        con.consume("con1", "1");
        assertTrue(con.playback("con1", "1", 0).size() == 2);
    }

    @Test
    public void testConsumerRemove() {
        TributaryController c = new TributaryController();

        c.createTopic("new", null);
        c.createPartition("1", "new");

        ConsumerController con = new ConsumerController("g1", "new", RebalancingType.RoundRobin);

        con.createConsumer("con1");

        ConsumerGroupResponse res1 = con.showConsumerGroup();
        assertTrue(res1.getConsumerIds().contains("con1"));

        con.removeConsumer("con1");

        ConsumerGroupResponse res2 = con.showConsumerGroup();
        assertTrue(res2.getConsumerIds().size() == 0);
    }

    @Test
    public void testPartitionDelete() {
        TributaryController t = new TributaryController();

        t.createTopic("top", Integer.class);
        t.createPartition("par", "top");
        t.createPartition("par2", "top");
        t.createPartition("par3", "top");
        t.createPartition("par4", "top");
        t.createPartition("par5", "top");
        t.createPartition("par6", "top");
        t.createPartition("par7", "top");

        ConsumerController c = new ConsumerController("gro", "top", RebalancingType.Range);

        c.createConsumer("con");
        c.createConsumer("con2");
        c.createConsumer("con3");

        ConsumerGroupResponse res4 = c.showConsumerGroup();
        List<String> expected3 = new ArrayList<>();
        expected3.add("par");
        expected3.add("par2");
        expected3.add("par3");
        List<String> expected4 = new ArrayList<>();
        expected4.add("par4");
        expected4.add("par5");
        List<String> expected5 = new ArrayList<>();
        expected5.add("par6");
        expected5.add("par7");
        assertTrue(res4.getPartitionIds("con").containsAll(expected3));
        assertTrue(res4.getPartitionIds("con").size() == 3);
        assertTrue(res4.getPartitionIds("con2").containsAll(expected4));
        assertTrue(res4.getPartitionIds("con2").size() == 2);
        assertTrue(res4.getPartitionIds("con3").containsAll(expected5));
        assertTrue(res4.getPartitionIds("con3").size() == 2);

        t.removePartition("par5");

        ConsumerGroupResponse res5 = c.showConsumerGroup();
        List<String> expected6 = new ArrayList<>();
        expected6.add("par");
        expected6.add("par2");
        List<String> expected7 = new ArrayList<>();
        expected7.add("par3");
        expected7.add("par4");
        List<String> expected8 = new ArrayList<>();
        expected8.add("par6");
        expected8.add("par7");
        assertTrue(res5.getPartitionIds("con").containsAll(expected6));
        assertTrue(res5.getPartitionIds("con").size() == 2);
        assertTrue(res5.getPartitionIds("con2").containsAll(expected7));
        assertTrue(res5.getPartitionIds("con2").size() == 2);
        assertTrue(res5.getPartitionIds("con3").containsAll(expected8));
        assertTrue(res5.getPartitionIds("con3").size() == 2);
    }

    @Test
    public void testMessageLifecycle() {
        TributaryController t = new TributaryController();

        t.createTopic("top", Integer.class);
        t.createPartition("par", "top");

        ProducerController p = new ProducerController("pro", AllocationType.Manual, Integer.class);

        ConsumerController c = new ConsumerController("gro", "top", RebalancingType.Range);
        c.createConsumer("con");
        ConsumerGroupResponse res1 = c.showConsumerGroup();
        assertTrue(res1.getConsumerIds().contains("con"));

        Message<?> msg = new Message<Integer>(LocalDateTime.now(), "msg1", "par", 100);
        p.produce("top", msg);

        TopicResponse res2 = t.showTopic("top");
        assertTrue(res2.getPartitionWithMessage(msg) != null);

        c.consume("con", "par");

        TopicResponse res3 = t.showTopic("top");
        assertTrue(res3.getMessages("par").size() == 0);

        List<Message<?>> res4 = c.playback("con", "par", 1);
        assertTrue(res4.contains(msg));
    }

    @Test
    public void testRandomAllocation() {
        TributaryController t = new TributaryController();

        t.createTopic("top", Integer.class);
        t.createPartition("par", "top");
        t.createPartition("par2", "top");
        t.createPartition("par3", "top");
        t.createPartition("par4", "top");
        t.createPartition("par5", "top");
        t.createPartition("par6", "top");
        t.createPartition("par7", "top");

        ProducerController p = new ProducerController("pro", AllocationType.Random, Integer.class);

        Message<?> msg = new Message<Integer>(LocalDateTime.now(), "msg1", "1", 100);
        Message<?> msg2 = new Message<Integer>(LocalDateTime.now(), "msg2", "1", 100);
        Message<?> msg3 = new Message<Integer>(LocalDateTime.now(), "msg3", "1", 100);
        p.produce("top", msg);
        p.produce("top", msg2);
        p.produce("top", msg3);

        TopicResponse res = t.showTopic("top");
        assertTrue(res.getPartitionWithMessage(msg) != null);
        assertTrue(res.getPartitionWithMessage(msg2) != null);
        assertTrue(res.getPartitionWithMessage(msg3) != null);
    }

    @Test
    public void testManualAllocation() {
        TributaryController t = new TributaryController();

        t.createTopic("top", Integer.class);
        t.createPartition("par", "top");
        t.createPartition("par2", "top");
        t.createPartition("par3", "top");
        t.createPartition("par4", "top");
        t.createPartition("par5", "top");
        t.createPartition("par6", "top");
        t.createPartition("par7", "top");

        ProducerController p = new ProducerController("pro", AllocationType.Manual, Integer.class);

        Message<?> msg = new Message<Integer>(LocalDateTime.now(), "msg1", "par2", 100);
        Message<?> msg2 = new Message<Integer>(LocalDateTime.now(), "msg2", "par", 100);
        Message<?> msg3 = new Message<Integer>(LocalDateTime.now(), "msg3", "par7", 100);
        Message<?> msg4 = new Message<Integer>(LocalDateTime.now(), "msg4", "par3", 100);
        Message<?> msg5 = new Message<Integer>(LocalDateTime.now(), "msg5", "par6", 100);
        p.produce("top", msg);
        p.produce("top", msg2);
        p.produce("top", msg3);
        p.produce("top", msg4);
        p.produce("top", msg5);

        TopicResponse res = t.showTopic("top");
        assertTrue(res.getPartitionWithMessage(msg) == "par2");
        assertTrue(res.getPartitionWithMessage(msg2) == "par");
        assertTrue(res.getPartitionWithMessage(msg3) == "par7");
        assertTrue(res.getPartitionWithMessage(msg4) == "par3");
        assertTrue(res.getPartitionWithMessage(msg5) == "par6");
    }

    @Test
    public void testRangeRebalance() {
        TributaryController t = new TributaryController();

        t.createTopic("top", Integer.class);
        t.createPartition("par", "top");
        t.createPartition("par2", "top");

        ConsumerController c = new ConsumerController("gro", "top", RebalancingType.Range);

        c.createConsumer("con");
        ConsumerGroupResponse res1 = c.showConsumerGroup();
        List<String> expected = new ArrayList<>();
        expected.add("par");
        expected.add("par2");
        assertTrue(res1.getPartitionIds("con").containsAll(expected));
        assertTrue(res1.getPartitionIds("con").size() == 2);

        c.createConsumer("con2");
        ConsumerGroupResponse res2 = c.showConsumerGroup();
        List<String> expected1 = new ArrayList<>();
        expected1.add("par");
        List<String> expected2 = new ArrayList<>();
        expected2.add("par2");
        assertTrue(res2.getPartitionIds("con").containsAll(expected1));
        assertTrue(res2.getPartitionIds("con").size() == 1);
        assertTrue(res2.getPartitionIds("con2").containsAll(expected2));
        assertTrue(res2.getPartitionIds("con2").size() == 1);

        c.createConsumer("con3");
        ConsumerGroupResponse res3 = c.showConsumerGroup();
        assertTrue(res3.getConsumerIds().contains("con"));
        assertTrue(res3.getPartitionIds("con").containsAll(expected1));
        assertTrue(res3.getPartitionIds("con").size() == 1);
        assertTrue(res3.getPartitionIds("con2").containsAll(expected2));
        assertTrue(res3.getPartitionIds("con2").size() == 1);
        assertTrue(res3.getPartitionIds("con3").size() == 0);

        t.createPartition("par3", "top");
        t.createPartition("par4", "top");
        t.createPartition("par5", "top");
        t.createPartition("par6", "top");
        t.createPartition("par7", "top");

        ConsumerGroupResponse res4 = c.showConsumerGroup();
        List<String> expected3 = new ArrayList<>();
        expected3.add("par");
        expected3.add("par2");
        expected3.add("par3");
        List<String> expected4 = new ArrayList<>();
        expected4.add("par4");
        expected4.add("par5");
        List<String> expected5 = new ArrayList<>();
        expected5.add("par6");
        expected5.add("par7");
        assertTrue(res4.getPartitionIds("con").containsAll(expected3));
        assertTrue(res4.getPartitionIds("con").size() == 3);
        assertTrue(res4.getPartitionIds("con2").containsAll(expected4));
        assertTrue(res4.getPartitionIds("con2").size() == 2);
        assertTrue(res4.getPartitionIds("con3").containsAll(expected5));
        assertTrue(res4.getPartitionIds("con3").size() == 2);

        c.removeConsumer("con2");

        ConsumerGroupResponse res5 = c.showConsumerGroup();
        List<String> expected6 = new ArrayList<>();
        expected6.add("par");
        expected6.add("par2");
        expected6.add("par3");
        expected6.add("par4");
        List<String> expected7 = new ArrayList<>();
        expected7.add("par5");
        expected7.add("par6");
        expected7.add("par7");
        assertTrue(res5.getPartitionIds("con").containsAll(expected6));
        assertTrue(res5.getPartitionIds("con").size() == 4);
        assertTrue(res5.getPartitionIds("con3").containsAll(expected7));
        assertTrue(res5.getPartitionIds("con3").size() == 3);
    }

    @Test
    public void testRoundRobinRebalance() {
        TributaryController t = new TributaryController();

        t.createTopic("top", Integer.class);
        t.createPartition("par", "top");
        t.createPartition("par2", "top");

        ConsumerController c = new ConsumerController("gro", "top", RebalancingType.RoundRobin);

        c.createConsumer("con");
        ConsumerGroupResponse res1 = c.showConsumerGroup();
        List<String> expected = new ArrayList<>();
        expected.add("par");
        expected.add("par2");
        assertTrue(res1.getPartitionIds("con").containsAll(expected));
        assertTrue(res1.getPartitionIds("con").size() == 2);

        c.createConsumer("con2");
        ConsumerGroupResponse res2 = c.showConsumerGroup();
        List<String> expected1 = new ArrayList<>();
        expected1.add("par");
        List<String> expected2 = new ArrayList<>();
        expected2.add("par2");
        assertTrue(res2.getPartitionIds("con").containsAll(expected1));
        assertTrue(res2.getPartitionIds("con").size() == 1);
        assertTrue(res2.getPartitionIds("con2").containsAll(expected2));
        assertTrue(res2.getPartitionIds("con2").size() == 1);

        c.createConsumer("con3");
        ConsumerGroupResponse res3 = c.showConsumerGroup();
        assertTrue(res3.getConsumerIds().contains("con"));
        assertTrue(res3.getPartitionIds("con").containsAll(expected1));
        assertTrue(res3.getPartitionIds("con").size() == 1);
        assertTrue(res3.getPartitionIds("con2").containsAll(expected2));
        assertTrue(res3.getPartitionIds("con2").size() == 1);
        assertTrue(res3.getPartitionIds("con3").size() == 0);

        t.createPartition("par3", "top");
        t.createPartition("par4", "top");
        t.createPartition("par5", "top");
        t.createPartition("par6", "top");
        t.createPartition("par7", "top");

        ConsumerGroupResponse res4 = c.showConsumerGroup();
        List<String> expected3 = new ArrayList<>();
        expected3.add("par");
        expected3.add("par4");
        expected3.add("par7");
        List<String> expected4 = new ArrayList<>();
        expected4.add("par2");
        expected4.add("par5");
        List<String> expected5 = new ArrayList<>();
        expected5.add("par3");
        expected5.add("par6");
        assertTrue(res4.getPartitionIds("con").containsAll(expected3));
        assertTrue(res4.getPartitionIds("con").size() == 3);
        assertTrue(res4.getPartitionIds("con2").containsAll(expected4));
        assertTrue(res4.getPartitionIds("con2").size() == 2);
        assertTrue(res4.getPartitionIds("con3").containsAll(expected5));
        assertTrue(res4.getPartitionIds("con3").size() == 2);

        c.removeConsumer("con2");

        ConsumerGroupResponse res5 = c.showConsumerGroup();
        List<String> expected6 = new ArrayList<>();
        expected6.add("par");
        expected6.add("par3");
        expected6.add("par5");
        expected6.add("par7");
        List<String> expected7 = new ArrayList<>();
        expected7.add("par2");
        expected7.add("par4");
        expected7.add("par6");
        assertTrue(res5.getPartitionIds("con").containsAll(expected6));
        assertTrue(res5.getPartitionIds("con").size() == 4);
        assertTrue(res5.getPartitionIds("con3").containsAll(expected7));
        assertTrue(res5.getPartitionIds("con3").size() == 3);
    }

    @Test
    public void testRuntimeRebalanceChange() {
        TributaryController t = new TributaryController();

        t.createTopic("top", Integer.class);
        t.createPartition("par", "top");
        t.createPartition("par2", "top");
        t.createPartition("par3", "top");
        t.createPartition("par4", "top");
        t.createPartition("par5", "top");
        t.createPartition("par6", "top");
        t.createPartition("par7", "top");

        ConsumerController c = new ConsumerController("gro", "top", RebalancingType.RoundRobin);

        c.createConsumer("con");
        c.createConsumer("con2");
        c.createConsumer("con3");

        ConsumerGroupResponse res = c.showConsumerGroup();
        List<String> expected = new ArrayList<>();
        expected.add("par");
        expected.add("par4");
        expected.add("par7");
        List<String> expected2 = new ArrayList<>();
        expected2.add("par2");
        expected2.add("par5");
        List<String> expected3 = new ArrayList<>();
        expected3.add("par3");
        expected3.add("par6");
        assertTrue(res.getPartitionIds("con").containsAll(expected));
        assertTrue(res.getPartitionIds("con").size() == 3);
        assertTrue(res.getPartitionIds("con2").containsAll(expected2));
        assertTrue(res.getPartitionIds("con2").size() == 2);
        assertTrue(res.getPartitionIds("con3").containsAll(expected3));
        assertTrue(res.getPartitionIds("con3").size() == 2);

        c.setRebalancingStrategy(RebalancingType.Range);

        ConsumerGroupResponse res2 = c.showConsumerGroup();
        List<String> expected4 = new ArrayList<>();
        expected4.add("par");
        expected4.add("par2");
        expected4.add("par3");
        List<String> expected5 = new ArrayList<>();
        expected5.add("par4");
        expected5.add("par5");
        List<String> expected6 = new ArrayList<>();
        expected6.add("par6");
        expected6.add("par7");
        assertTrue(res2.getPartitionIds("con").containsAll(expected4));
        assertTrue(res2.getPartitionIds("con").size() == 3);
        assertTrue(res2.getPartitionIds("con2").containsAll(expected5));
        assertTrue(res2.getPartitionIds("con2").size() == 2);
        assertTrue(res2.getPartitionIds("con3").containsAll(expected6));
        assertTrue(res2.getPartitionIds("con3").size() == 2);
    }

    @Test
    public void testParallelConsume() {
        TributaryController t = new TributaryController();
        t.createTopic("top", Integer.class);
        t.createPartition("par", "top");

        ConsumerController c1 = new ConsumerController("gro", "top", RebalancingType.RoundRobin);
        ConsumerController c2 = new ConsumerController("gro2", "top", RebalancingType.RoundRobin);

        Map<String, ConsumerController> cliConController = TributaryCLI.getConsumerMap();
        cliConController.put("gro", c1);
        cliConController.put("gro2", c2);

        c1.createConsumer("con");
        c2.createConsumer("con2");

        ProducerController p = new ProducerController("pro", AllocationType.Random, Integer.class);
        Map<String, ProducerController> cliProdController = TributaryCLI.getProducerMap();
        cliProdController.put("gro", p);

        Message<?> msg = new Message<Integer>(LocalDateTime.now(), "msg1", "1", 100);
        Message<?> msg2 = new Message<Integer>(LocalDateTime.now(), "msg2", "2", 100);
        Message<?> msg3 = new Message<Integer>(LocalDateTime.now(), "msg3", "3", 100);
        Message<?> msg4 = new Message<Integer>(LocalDateTime.now(), "msg4", "4", 100);
        Message<?> msg5 = new Message<Integer>(LocalDateTime.now(), "msg5", "5", 100);
        Message<?> msg6 = new Message<Integer>(LocalDateTime.now(), "msg1", "6", 100);
        Message<?> msg7 = new Message<Integer>(LocalDateTime.now(), "msg2", "7", 100);
        Message<?> msg8 = new Message<Integer>(LocalDateTime.now(), "msg3", "8", 100);
        Message<?> msg9 = new Message<Integer>(LocalDateTime.now(), "msg4", "9", 100);
        Message<?> msg10 = new Message<Integer>(LocalDateTime.now(), "msg5", "10", 100);
        p.produce("top", msg);
        p.produce("top", msg2);
        p.produce("top", msg3);
        p.produce("top", msg4);
        p.produce("top", msg5);
        p.produce("top", msg6);
        p.produce("top", msg7);
        p.produce("top", msg8);
        p.produce("top", msg9);
        p.produce("top", msg10);

        String cmd = "parallel consume (con, par), (con2, par), (con, par), (con2, par), (con, par), "
                + "(con2, par), (con, par), (con2, par), (con, par), (con2, par)";

        TributaryCLI.parallelConsume(cmd.split("\\s+"));

        TopicResponse res = t.showTopic("top");
        assertTrue(res.getMessages("par").size() == 0);
    }

    @Test
    public void testParallelProduce() {
        TributaryController t = new TributaryController();
        t.createTopic("top", Integer.class);
        t.createPartition("par", "top");

        ProducerController p = new ProducerController("pro", AllocationType.Random, Integer.class);
        Map<String, ProducerController> cliProdController = TributaryCLI.getProducerMap();
        cliProdController.put("pro", p);

        String cmd = "parallel produce (pro, top, testInt, msg1), (pro, top, testInt, msg2), "
                + "(pro, top, testInt, msg3), (pro, top, testInt, msg4), (pro, top, testInt, msg5), "
                + "(pro, top, testInt, msg6), (pro, top, testInt, msg7), (pro, top, testInt, msg8), "
                + "(pro, top, testInt, msg9), (pro, top, testInt, msg10)";

        TributaryCLI.parallelProduce(cmd.split("\\s+"));

        TopicResponse res = t.showTopic("top");
        assertTrue(res.getMessages("par").size() == 10);
    }
}

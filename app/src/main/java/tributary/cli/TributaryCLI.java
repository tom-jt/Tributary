package tributary.cli;

import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONObject;

import tributary.api.ConsumerController;
import tributary.api.Message;
import tributary.api.ProducerController;
import tributary.api.TributaryController;
import tributary.api.enums.AllocationType;
import tributary.api.enums.RebalancingType;
import tributary.api.res.ConsumerGroupResponse;
import tributary.api.res.TopicResponse;

public class TributaryCLI {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";

    private static TributaryController tributaryController = new TributaryController();
    private static Map<String, ProducerController> producerControllers = new HashMap<>();
    private static Map<String, ConsumerController> consumerControllers = new HashMap<>();

    public static void main(String[] args) {
        System.out.println(ANSI_GREEN_BACKGROUND + "Tributary CLI Verson 1.0" + ANSI_RESET);

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("\n" + ANSI_GREEN + "> Please input a command: " + ANSI_RESET);
            String cmd = sc.nextLine();

            if (cmd.equals("exit")) {
                break;
            }
            parseCommand(cmd);
        }

        sc.close();
        System.out.println("Shutting down Tributary CLI...");
    }

    private static void parseCommand(String cmd) {
        String[] args = cmd.split("\\s+");

        switch (args[0]) {
        case "create":
            create(args);
            break;
        case "delete":
            deleteConsumer(args);
            break;
        case "produce":
            produce(args);
            break;
        case "consume":
            consume(args);
            break;
        case "show":
            show(args);
            break;
        case "parallel":
            parallel(args);
            break;
        case "set":
            setConsumerGroupRebalancing(args);
            break;
        case "playback":
            playback(args);
            break;
        default:
            parseFail();
            break;
        }
    }

    private static void parseFail() {
        System.out.println("Unknown command");
    }

    private static void create(String[] args) {
        switch (args[1]) {
        case "topic":
            createTopic(args);
            break;
        case "partition":
            createPartition(args);
            break;
        case "consumer":
            if (args[2].equals("group")) {
                createConsumerGroup(args);
            } else {
                createConsumer(args);
            }
            break;
        case "producer":
            createProducer(args);
            break;
        default:
            parseFail();
            break;
        }
    }

    /*
     * create topic <id> <type>
     */
    private static void createTopic(String[] args) {
        tributaryController.createTopic(args[2], parseClass(args[3]));
        System.out.println(String.format("Created topic of type [%s] with id [%s]", args[3], args[2]));
    }

    /*
     * create partition <topic> <id>
     */
    private static void createPartition(String[] args) {
        tributaryController.createPartition(args[3], args[2]);
        System.out.println(String.format("Created partition with id [%s] in topic with id [%s]", args[3], args[2]));

    }

    /*
     * create consumer group <id> <topic> <rebalancing>
     */
    private static void createConsumerGroup(String[] args) {
        ConsumerController newGroup = new ConsumerController(args[3], args[4], RebalancingType.valueOf(args[5]));
        consumerControllers.put(args[3], newGroup);
        System.out.println(String.format("Created group with id [%s] in topic with id [%s]\nRebalancing is set to [%s]",
                args[3], args[4], args[5]));
    }

    /*
     * create consumer <group> <id>
     */
    private static void createConsumer(String[] args) {
        ConsumerController group = consumerControllers.get(args[2]);
        group.createConsumer(args[3]);
        System.out.println(String.format("Created consumer with id [%s] in group with id [%s]", args[3], args[2]));
    }

    /*
     * create producer <id> <type> <allocation>
     */
    private static void createProducer(String[] args) {
        ProducerController producer = new ProducerController(args[2], AllocationType.valueOf(args[4]),
                parseClass(args[3]));
        producerControllers.put(args[2], producer);
        System.out.println(String.format("Created producer with id [%s] of type [%s]\nAllocation is set to [%s]",
                args[2], args[3], args[4]));
    }

    private static Class<?> parseClass(String str) {
        return str.equals("Integer") ? Integer.class : String.class;
    }

    /*
     * delete consumer <consumer>
     */
    private static void deleteConsumer(String[] args) {
        if (!args[1].equals("consumer")) {
            parseFail();
        }

        List<ConsumerController> groups = new ArrayList<>(consumerControllers.values());
        for (ConsumerController group : groups) {
            if (group.removeConsumer(args[2])) {
                System.out.println(String.format("Removed consumer with id [%s]", args[2]));
                return;
            }
        }

        System.out.println(String.format("No consumer with id [%s] was found", args[2]));
    }

    /*
     * produce event <producer> <topic> <eventFilename> <OPTIONAL eventId> <OPTIONAL partition>
     */
    private static void produce(String[] args) {
        if (!args[1].equals("event")) {
            parseFail();
        }

        ProducerController producer = producerControllers.get(args[2]);

        Message<?> msg = loadMsg(loadJSON(args[4]), args.length >= 6 ? args[5] : null,
                args.length >= 7 ? args[6] : null);
        producer.produce(args[3], msg);

        TopicResponse res = new TopicResponse(tributaryController.showTopic(args[3]));
        System.out.println(String.format("Created event with id [%s] in partition with id [%s]", msg.getId(),
                res.getPartitionWithMessage(msg)));
    }

    private static Message<?> loadMsg(JSONObject obj, String overrideId, String overrideKey) {
        String id = overrideId == null ? obj.getString("id") : overrideId;
        String key = overrideKey == null ? obj.getString("key") : overrideKey;
        LocalDateTime localDateTime = LocalDateTime.now();

        if (obj.getString("type").equals("Integer")) {
            return new Message<Integer>(localDateTime, id, key, obj.getInt("value"));
        } else {
            return new Message<String>(localDateTime, id, key, obj.getString("value"));
        }
    }

    private static JSONObject loadJSON(String filename) {
        String file = String.format("/%s.json", filename);
        try {
            return new JSONObject(loadResourceFile(file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void consume(String[] args) {
        if (args[1].equals("event")) {
            consumeEvent(args);
        } else if (args[1].equals("events")) {
            consumeEvents(args);
        } else {
            parseFail();
        }
    }

    /*
     * consume event <consumer> <partition>
     */
    private static void consumeEvent(String[] args) {
        List<ConsumerController> groups = new ArrayList<>(consumerControllers.values());
        for (ConsumerController group : groups) {
            ConsumerGroupResponse res = new ConsumerGroupResponse(group.showConsumerGroup());
            if (res.getConsumerIds().contains(args[2])) {
                Message<?> msg = group.consume(args[2], args[3]);
                if (msg == null) {
                    System.out.println(String.format(
                            "Consumer [%s] cannot consume from partition [%s] because it is empty", args[2], args[3]));
                } else {
                    System.out.println(String.format(
                            "Consumer [%s] consumed event [%s] with data [%s] of type [%s] created at time [%s]",
                            args[2], msg.getId(), msg.getValue(), msg.getType(), msg.getDateTimeCreated()));
                }
                break;
            }
        }
    }

    /*
     * consume events <consumer> <partition> <number of events>
     */
    private static void consumeEvents(String[] args) {
        for (int i = 0; i < Integer.parseInt(args[4]); i++) {
            consumeEvent(args);
        }
    }

    private static void show(String[] args) {
        if (args[1].equals("topic")) {
            showTopic(args);
        } else if (args[1].equals("consumer") && args[2].equals("group")) {
            showConsumerGroup(args);
        } else {
            parseFail();
        }
    }

    /*
     * show topic <topic>
     */
    private static void showTopic(String[] args) {
        TopicResponse res = new TopicResponse(tributaryController.showTopic(args[2]));
        String topicId = res.getTopicId();
        Class<?> topicType = res.getTopicType();
        List<String> partitions = res.getPartitionIds();

        System.out.println(String.format("\nTopic [%s]", topicId));
        System.out.println(String.format("Type: [%s]", topicType));

        for (String partition : partitions) {
            System.out.println(String.format("\n | Partition [%s]", partition));
            List<Message<?>> msgs = res.getMessages(partition);
            if (msgs.size() == 0) {
                System.out.println("    | Wow, such empty :(");
            }

            for (Message<?> msg : msgs) {
                System.out
                        .println(String.format("    | Event [%s] contains data [%s] of type [%s] created at time [%s]",
                                msg.getId(), msg.getValue(), msg.getType(), msg.getDateTimeCreated()));
            }
        }
    }

    /*
     * show consumer group <group>
     */
    private static void showConsumerGroup(String[] args) {
        ConsumerController group = consumerControllers.get(args[3]);
        ConsumerGroupResponse res = new ConsumerGroupResponse(group.showConsumerGroup());

        System.out.println(String.format("\nConsumer Group [%s]", res.getConsumerGroupId()));
        System.out.println(String.format("Assigned to topic [%s]", res.getTopicId()));
        System.out.println(String.format("With rebalancing strategy [%s]", res.getRebalancingType()));

        for (String consumerId : res.getConsumerIds()) {
            System.out.println(String.format("\n | Consumer [%s] is assigned to", consumerId));

            List<String> partitionIds = res.getPartitionIds(consumerId);
            if (partitionIds.size() == 0) {
                System.out.println("    | Wow, such empty :(");
            }
            for (String partitionId : partitionIds) {
                System.out.println(String.format("    | Partition [%s]", partitionId));
            }
        }
    }

    private static void parallel(String[] args) {
        if (args[1].equals("produce")) {
            parallelProduce(args);
        } else if (args[1].equals("consume")) {
            parallelConsume(args);
        } else {
            parseFail();
        }
    }

    /*
    * parallel produce (<producer>, <topic>, <eventFilename>, <OPTIONAL eventId>, <OPTIONAL partition>), (...REPEAT)
    */
    public static void parallelProduce(String[] args) {
        int i = 2;
        while (i < args.length) {
            int end = i;
            while (!args[end].endsWith("),") && !args[end].endsWith(")")) {
                end++;
            }

            String[] newArgs = Arrays.copyOfRange(args, i, end + 1);
            for (int j = 0; j < newArgs.length; j++) {
                newArgs[j] = newArgs[j].replaceAll(",|\\)|\\(", "");
            }
            i += newArgs.length;

            ProducerController producer = producerControllers.get(newArgs[0]);

            Message<?> msg = loadMsg(loadJSON(newArgs[2]), newArgs.length >= 4 ? newArgs[3] : null,
                    newArgs.length >= 5 ? newArgs[4] : null);

            new ParallelProducer(tributaryController, producer, newArgs[1], msg).start();
        }

        try {
            Thread.sleep(100L);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * parallel consume (<consumer>, <partition>), (...REPEAT)
     */
    public static void parallelConsume(String[] args) {
        int i = 2;
        while (i < args.length) {
            int end = i;
            while (!args[end].endsWith("),") && !args[end].endsWith(")")) {
                end++;
            }

            String[] newArgs = Arrays.copyOfRange(args, i, end + 1);
            for (int j = 0; j < newArgs.length; j++) {
                newArgs[j] = newArgs[j].replaceAll(",|\\)|\\(", "");
            }
            i += newArgs.length;

            List<ConsumerController> groups = new ArrayList<>(consumerControllers.values());
            ConsumerController myGroup = null;
            for (ConsumerController group : groups) {
                ConsumerGroupResponse res = new ConsumerGroupResponse(group.showConsumerGroup());
                if (res.getConsumerIds().contains(newArgs[0])) {
                    myGroup = group;
                }
            }

            new ParallelConsumer(newArgs[0], newArgs[1], myGroup).start();
        }

        try {
            Thread.sleep(100L);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * set consumer group rebalancing <group> <rebalancing>
     */
    private static void setConsumerGroupRebalancing(String[] args) {
        if (!args[1].equals("consumer") || !args[2].equals("group") || !args[3].equals("rebalancing")) {
            parseFail();
        }

        ConsumerController group = consumerControllers.get(args[4]);
        group.setRebalancingStrategy(RebalancingType.valueOf(args[5]));
        System.out.println(String.format("Rebalancing for group [%s] has been updated to [%s]", args[4], args[5]));
    }

    /*
     * playback <consumer> <partition> <offset>
     */
    private static void playback(String[] args) {
        List<ConsumerController> groups = new ArrayList<>(consumerControllers.values());
        for (ConsumerController group : groups) {
            ConsumerGroupResponse res = new ConsumerGroupResponse(group.showConsumerGroup());
            if (res.getConsumerIds().contains(args[1])) {
                System.out.println(
                        String.format("Playing back events consumed by [%s] from partition [%s] on offset [%s]",
                                args[1], args[2], args[3]));

                List<Message<?>> msgs = group.playback(args[1], args[2], Integer.parseInt(args[3]));
                if (msgs.size() == 0) {
                    System.out.println("    | Wow, such empty :(");
                }

                for (Message<?> msg : msgs) {
                    System.out.println(
                            String.format("\n | Event [%s] contains data [%s] of type [%s] created at time [%s]",
                                    msg.getId(), msg.getValue(), msg.getType(), msg.getDateTimeCreated()));
                }
            }
        }
    }

    /**
    * Taken from assignment-ii FileLoader.java
    *
    * Loads a resource file given a certain path that is relative to resources/
    * for example `/dungeons/maze.json`. Will add a `/` prefix to path if it's not
    * specified.
    *
    * @precondiction path exists as a file
    * @param path Relative to resources/ will add an implicit `/` prefix if not
    *             given.
    * @return The textual content of the given file.
    * @throws IOException If some other IO exception.
    */
    private static String loadResourceFile(String path) throws IOException {
        if (!path.startsWith("/"))
            path = "/" + path;
        return new String(TributaryCLI.class.getResourceAsStream(path).readAllBytes());
    }

    /*
     * function used for testing purposes only (TributaryTest.java)
     */
    public static Map<String, ConsumerController> getConsumerMap() {
        return consumerControllers;
    }

    /*
     * function used for testing purposes only (TributaryTest.java)
     */
    public static Map<String, ProducerController> getProducerMap() {
        return producerControllers;
    }
}

package voldemort.cluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import voldemort.VoldemortException;
import voldemort.annotations.concurrency.Threadsafe;
import voldemort.annotations.jmx.JmxGetter;
import voldemort.annotations.jmx.JmxManaged;

import com.google.common.base.Objects;

/**
 * A representation of the voldemort cluster
 * 
 * @author jay
 * 
 */
@Threadsafe
@JmxManaged(description = "Metadata about the physical servers on which the Voldemort cluster runs")
public class Cluster implements Serializable {

    private static final long serialVersionUID = 1;

    private final String name;
    private final int numberOfTags;
    private final Map<Integer, Node> nodesById;

    public Cluster(String name, List<Node> nodes) {
        this.name = Objects.nonNull(name);
        this.nodesById = new LinkedHashMap<Integer, Node>(nodes.size());
        for (Node node : nodes) {
            if (nodesById.containsKey(node.getId()))
                throw new IllegalArgumentException("Node id " + node.getId()
                                                   + " appears twice in the node list.");
            nodesById.put(node.getId(), node);
        }
        this.numberOfTags = getNumberOfTags(nodes);
    }

    private int getNumberOfTags(List<Node> nodes) {
        List<Integer> tags = new ArrayList<Integer>();
        for (Node node : nodes)
            tags.addAll(node.getPartitionIds());
        Collections.sort(tags);
        for (int i = 0; i < numberOfTags; i++) {
            if (tags.get(i).longValue() != i)
                throw new IllegalArgumentException("Invalid tag assignment.");
        }
        return tags.size();
    }

    @JmxGetter(name = "name", description = "The name of the cluster")
    public String getName() {
        return name;
    }

    public Collection<Node> getNodes() {
        return nodesById.values();
    }

    public Node getNodeById(int id) {
        Node node = nodesById.get(id);
        if (node == null)
            throw new VoldemortException("No such node in cluster: " + id);
        return node;
    }

    @JmxGetter(name = "numberOfNodes", description = "The number of nodes in the cluster.")
    public int getNumberOfNodes() {
        return nodesById.size();
    }

    public int getNumberOfTags() {
        return numberOfTags;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Cluster('");
        builder.append(getName());
        builder.append("', [");
        for (Node n : getNodes()) {
            builder.append(n.toString());
            builder.append(", ");
        }
        builder.append("])");

        return builder.toString();
    }

}
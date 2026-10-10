package com.yourownai.vectordb;

import com.yourownai.model.*;

import java.util.*;

/**
 * Provides KdTreeIndex behavior.
 */
public class KdTreeIndex implements VectorIndex {
    private final Map<Long, VectorItem> items = new HashMap<>();
    private volatile Node root;

    /**
     * Represents Node data.
     */
    private record Node(VectorItem item, int axis, Node left, Node right) {
    }

    /**
     * Executes add.
     */
    public synchronized void add(VectorItem item) {
        items.put(item.id(), item);
        rebuild();
    }

    /**
     * Executes delete.
     */
    public synchronized void delete(long id) {
        items.remove(id);
        rebuild();
    }

    /**
     * Executes size.
     */
    public int size() {
        return items.size();
    }

    /**
     * Executes rebuild.
     */
    private void rebuild() {
        List<VectorItem> list = new ArrayList<>(items.values());
        root = build(list, 0);
    }

    /**
     * Executes build.
     */
    private Node build(List<VectorItem> list, int depth) {
        if (list.isEmpty()) {
            return null;
        }
        int axis = depth % list.get(0).values().length;
        list.sort(Comparator.comparingDouble(v -> v.values()[axis]));
        int mid = list.size() / 2;
        return new Node(list.get(mid), axis, build(new ArrayList<>(list.subList(0, mid)), depth + 1), build(new ArrayList<>(list.subList(mid + 1, list.size())), depth + 1));
    }

    /**
     * Executes search.
     */
    public List<ScoredVector> search(float[] query, int k, DistanceMetric metric) {
        if (metric != DistanceMetric.EUCLIDEAN || root == null) {
            BruteForceIndex brute = new BruteForceIndex();
            items.values().forEach(brute::add);
            return brute.search(query, k, metric);
        }
        PriorityQueue<ScoredVector> best = new PriorityQueue<>(Comparator.comparingDouble(ScoredVector::distance).reversed());
        search(root, query, k, best);
        return best.stream().sorted(Comparator.comparingDouble(ScoredVector::distance)).toList();
    }

    /**
     * Executes search.
     */
    private void search(Node n, float[] q, int k, PriorityQueue<ScoredVector> best) {
        if (n == null) {
            return;
        }
        double d = VectorMath.euclidean(q, n.item.values());
        if (best.size() < k) best.offer(new ScoredVector(n.item, d));
        else if (d < best.peek().distance()) {
            best.poll();
            best.offer(new ScoredVector(n.item, d));
        }
        int a = n.axis;
        double diff = q[a] - n.item.values()[a];
        Node near = diff < 0 ? n.left : n.right, far = diff < 0 ? n.right : n.left;
        search(near, q, k, best);
        if (best.size() < k || diff * diff < best.peek().distance() * best.peek().distance()) search(far, q, k, best);
    }
}

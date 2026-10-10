package com.yourownai.vectordb;

import com.yourownai.model.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Educational HNSW implementation. It favors clarity and deterministic behavior over exotic optimizations.
 */

/**
 * Provides HnswIndex behavior.
 */
public class HnswIndex implements VectorIndex {
    private final int m;
    private final int efConstruction;
    private volatile int maxLevel = -1;
    private volatile long entryPoint = -1;
    private final Map<Long, Node> nodes = new HashMap<>();
    /**
     * Executes ReentrantReadWriteLock.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Represents Candidate data.
     */
    private record Candidate(long id, double distance) {
    }

    /**
     * Provides Node behavior.
     */
    private static final class Node {
        final VectorItem item;
        final List<Set<Long>> neighbors;

        Node(VectorItem item, int level) {
            this.item = item;
            neighbors = new ArrayList<>(level + 1);
            for (int i = 0; i <= level; i++) {
                neighbors.add(new HashSet<>());
            }
        }
    }

    /**
     * Executes HnswIndex.
     */
    public HnswIndex(int m, int efConstruction) {
        this.m = Math.max(2, m);
        this.efConstruction = Math.max(this.m, efConstruction);
    }

    /**
     * Executes size.
     */
    public int size() {
        lock.readLock().lock();
        try {
            return nodes.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Executes add.
     */
    public void add(VectorItem item) {
        lock.writeLock().lock();
        try {
            addUnlocked(item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Executes delete.
     */
    public void delete(long id) {
        lock.writeLock().lock();
        try {
            if (nodes.remove(id) == null) {
                return;
            }
            List<VectorItem> remaining = nodes.values().stream().map(node -> node.item).toList();
            nodes.clear();
            entryPoint = -1;
            maxLevel = -1;
            remaining.forEach(this::addUnlocked);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Executes addUnlocked.
     */
    private void addUnlocked(VectorItem item) {
        if (nodes.containsKey(item.id())) {
            return;
        }
        int level = randomLevel();
        Node n = new Node(item, level);
        nodes.put(item.id(), n);
        if (entryPoint < 0) {
            entryPoint = item.id();
            maxLevel = level;
            return;
        }
        long ep = entryPoint;
        for (int l = maxLevel; l > level; l--) {
            ep = greedy(ep, item.values(), l, DistanceMetric.COSINE);
        }
        for (int l = Math.min(level, maxLevel); l >= 0; l--) {
            List<Long> candidates = searchLayer(item.values(), ep, efConstruction, l, DistanceMetric.COSINE);
            List<Long> selected = selectNearest(candidates, item.values(), m, DistanceMetric.COSINE);
            for (long id : selected) {
                connect(n, id, l);
                connect(nodes.get(id), item.id(), l);
            }
            if (!selected.isEmpty()) {
                ep = selected.get(0);
            }
        }
        if (level > maxLevel) {
            entryPoint = item.id();
            maxLevel = level;
        }
    }

    /**
     * Executes connect.
     */
    private void connect(Node a, long id, int level) {
        if (a == null || level >= a.neighbors.size()) {
            return;
        }
        Set<Long> s = a.neighbors.get(level);
        s.add(id);
        while (s.size() > m) {
            long worst = s.stream()
                    .max(Comparator.comparingDouble(
                            x -> VectorMath.distance(
                                    a.item.values(),
                                    nodes.get(x).item.values(),
                                    DistanceMetric.COSINE)))
                    .orElse(id);
            s.remove(worst);
        }
    }

    /**
     * Executes randomLevel.
     */
    private int randomLevel() {
        int level = 0;
        while (ThreadLocalRandom.current().nextDouble() < 0.5 && level < 32) {
            level++;
        }
        return level;
    }

    /**
     * Executes greedy.
     */
    private long greedy(long current, float[] q, int level, DistanceMetric metric) {
        boolean changed = true;
        while (changed) {
            changed = false;
            double cd = VectorMath.distance(q, nodes.get(current).item.values(), metric);
            for (long n : nodes.get(current).neighbors.get(level)) {
                double d = VectorMath.distance(q, nodes.get(n).item.values(), metric);
                if (d < cd) {
                    cd = d;
                    current = n;
                    changed = true;
                }
            }
        }
        return current;
    }

    /**
     * Executes searchLayer.
     */
    private List<Long> searchLayer(float[] q, long entry, int ef, int level, DistanceMetric metric) {
        if (entry < 0 || !nodes.containsKey(entry) || level >= nodes.get(entry).neighbors.size()) {
            return List.of();
        }
        Comparator<Candidate> cmp = Comparator.comparingDouble(Candidate::distance);
        PriorityQueue<Candidate> candidates = new PriorityQueue<>(cmp);
        PriorityQueue<Candidate> results = new PriorityQueue<>(cmp.reversed());
        Set<Long> visited = new HashSet<>();
        double d = VectorMath.distance(q, nodes.get(entry).item.values(), metric);
        candidates.add(new Candidate(entry, d));
        results.add(new Candidate(entry, d));
        visited.add(entry);
        while (!candidates.isEmpty()) {
            Candidate c = candidates.poll();
            double worst = results.peek().distance();
            if (c.distance() > worst) break;
            for (long id : nodes.get(c.id()).neighbors.get(level)) {
                if (!visited.add(id)) continue;
                double nd = VectorMath.distance(q, nodes.get(id).item.values(), metric);
                if (results.size() < ef || nd < results.peek().distance()) {
                    candidates.add(new Candidate(id, nd));
                    results.add(new Candidate(id, nd));
                    if (results.size() > ef) results.poll();
                }
            }
        }
        List<Long> out = new ArrayList<>();
        while (!results.isEmpty()) out.add(results.poll().id());
        return out;
    }

    /**
     * Executes selectNearest.
     */
    private List<Long> selectNearest(Collection<Long> ids, float[] q, int count, DistanceMetric metric) {
        return ids.stream().sorted(Comparator.comparingDouble(id -> VectorMath.distance(q, nodes.get(id).item.values(), metric))).limit(count).toList();
    }

    /**
     * Executes search.
     */
    public List<ScoredVector> search(float[] query, int k, DistanceMetric metric) {
        lock.readLock().lock();
        try {
            if (nodes.isEmpty() || k <= 0) {
                return List.of();
            }
            if (metric != DistanceMetric.COSINE) {
                return nodes.values().stream()
                        .map(n -> new ScoredVector(n.item, VectorMath.distance(query, n.item.values(), metric)))
                        .sorted(Comparator.comparingDouble(ScoredVector::distance))
                        .limit(k)
                        .toList();
            }
            long ep = entryPoint;
            for (int l = maxLevel; l > 0; l--) {
                ep = greedy(ep, query, l, metric);
            }
            List<Long> ids = searchLayer(query, ep, Math.max(k, efConstruction), 0, metric);
            return ids.stream()
                    .map(id -> new ScoredVector(nodes.get(id).item, VectorMath.distance(query, nodes.get(id).item.values(), metric)))
                    .sorted(Comparator.comparingDouble(ScoredVector::distance))
                    .limit(k)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }
}

/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.overlay;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeMultimap;
import se.kth.id2203.Util.MurmurHasher;
import se.kth.id2203.bootstrapping.NodeAssignment;
import se.kth.id2203.networking.NetAddress;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class LookupTable implements NodeAssignment {

    private static final long serialVersionUID = -8766981433378303267L;

    private final TreeMultimap<Integer, NetAddress> partitions = TreeMultimap.create();

    public Collection<NetAddress> lookup(String key) {
        int keyHash = key.hashCode();
        Integer partition = partitions.keySet().floor(keyHash);
        if (partition == null) {
            partition = partitions.keySet().last();
        }
        return partitions.get(partition);
    }

    public Collection<NetAddress> get(String key) {

        int hashedKey = Math.abs(MurmurHasher.keyToHash(key));
        int moddedKey = hashedKey % partitions.keySet().size();
        int partition = partitions.keySet().last();
        if (this.partitions.containsKey(moddedKey)) partition = moddedKey;
        Collection<NetAddress> p = partitions.get(partition);
        return partitions.get(partition);
    }

    public Collection<NetAddress> getNodes() {
        return partitions.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LookupTable(\n");
        for (Integer key : partitions.keySet()) {
            sb.append(key);
            sb.append(" -> ");
            sb.append(Iterables.toString(partitions.get(key)));
            sb.append("\n");
        }
        sb.append(")");
        return sb.toString();
    }

    static LookupTable generate(ImmutableSet<NetAddress> nodes) {
        LookupTable lut = new LookupTable();
        lut.partitions.putAll(0, nodes);
        return lut;
    }

    static LookupTable createEmpty() {
        LookupTable lut = new LookupTable();
        return lut;
    }

    static LookupTable generatePartitionedTable(ImmutableSet<NetAddress> nodes, int partitions, int partitionSize) {
        LookupTable lut = new LookupTable();
        int nodeCount = 0;
        int partition = 0;

        for(NetAddress netAddress : nodes) {
            lut.addNode(netAddress, partition);
            nodeCount++;
            if(nodeCount >= partitionSize) {
                partition++;
                nodeCount = 0;
            }
        }

        return lut;
    }

    public NavigableSet<NetAddress> getPartition(NetAddress na) {
        for(Integer key : partitions.keySet()) {
            if(partitions.get(key).contains(na)) {
                return new TreeSet<>(partitions.get(key));
            }
        }
        return null;
    }


    void addNode(NetAddress node, int key) {
        this.partitions.get(key).add(node);
    }

}

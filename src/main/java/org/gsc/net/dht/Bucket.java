/*
 * GSC (Global Social Chain), a blockchain fit for mass adoption and
 * a sustainable token economy model, is the decentralized global social
 * chain with highly secure, low latency, and near-zero fee transactional system.
 *
 * gsc-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * License GSC-Core is under the GNU General Public License v3. See LICENSE.
 */

package org.gsc.net.dht;

import java.util.ArrayList;
import java.util.List;

public class Bucket {

    public static int MAX_KADEMLIA_K = 5;

    // if bit = 1 go left
    Bucket left;

    // if bit = 0 go right
    Bucket right;

    String name;

    List<Peer> peers = new ArrayList<>();


    public Bucket(String name) {
        this.name = name;
    }


    public void add(Peer peer) {

        if (peer == null) {
            throw new Error("Not a leaf");
        }

        if (peers == null) {

            if (peer.nextBit(name) == 1) {
                left.add(peer);
            } else {
                right.add(peer);
            }

            return;
        }

        peers.add(peer);

        if (peers.size() > MAX_KADEMLIA_K) {
            splitBucket();
        }
    }

    public void splitBucket() {
        left = new Bucket(name + "1");
        right = new Bucket(name + "0");

        for (Peer id : peers) {
            if (id.nextBit(name) == 1) {
                left.add(id);
            } else {
                right.add(id);
            }
        }

        this.peers = null;
    }


    public Bucket left() {
        return left;
    }

    public Bucket right() {
        return right;
    }


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(name).append("\n");

        if (peers == null) {
            return sb.toString();
        }

        for (Peer id : peers) {
            sb.append(id.toBinaryString()).append("\n");
        }

        return sb.toString();
    }


    public void traverseTree(DoOnTree doOnTree) {

        if (left != null) {
            left.traverseTree(doOnTree);
        }
        if (right != null) {
            right.traverseTree(doOnTree);
        }

        doOnTree.call(this);
    }

    //tree operations

    public interface DoOnTree {

        void call(Bucket bucket);
    }


    public static class SaveLeaf implements DoOnTree {

        List<Bucket> leafs = new ArrayList<>();

        @Override
        public void call(Bucket bucket) {
            if (bucket.peers != null) {
                leafs.add(bucket);
            }
        }

        public List<Bucket> getLeafs() {
            return leafs;
        }

        public void setLeafs(List<Bucket> leafs) {
            this.leafs = leafs;
        }
    }

    public String getName() {
        return name;
    }

    public List<Peer> getPeers() {
        return peers;
    }
}

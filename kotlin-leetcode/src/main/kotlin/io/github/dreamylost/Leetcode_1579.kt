/* Licensed under Apache-2.0 @梦境迷离 */
package io.github.dreamylost

/**
 * 1579. 保证图可完全遍历
 * Alice 和 Bob 共有一个无向图，其中包含 n 个节点和 3  种类型的边：
 * 类型 1：只能由 Alice 遍历。
 * 类型 2：只能由 Bob 遍历。
 * 类型 3：Alice 和 Bob 都可以遍历。
 * 给你一个数组 edges ，其中 edges[i] = [typei, ui, vi] 表示节点 ui 和 vi 之间存在类型为 typei 的双向边。请你在保证图仍能够被 Alice和 Bob 完全遍历的前提下，找出可以删除的最大边数。如果从任何节点开始，Alice 和 Bob 都可以到达所有其他节点，则认为图是可以完全遍历的。
 * 返回可以删除的最大边数，如果 Alice 和 Bob 无法完全遍历图，则返回 -1 。
 *
 * @author 梦境迷离
 * @since 2021/1/27
 * @version 1.0
 */
object Leetcode_1579 {
    /**
     * 1076 ms,100.00%
     * 124.5 MB,100.00%
     */
    fun maxNumEdgesToRemove(n: Int, edges: Array<IntArray>): Int {
        if (n <= 1) {
            return 0
        }
        val uf1 = UnionFind(n)
        val uf2 = UnionFind(n)
        var common = 0
        // 删除公共边
        for (i in edges.indices) {
            if (edges[i][0] == 3) {
                val union1 = uf1.union(edges[i][1], edges[i][2])
                val union2 = uf2.union(edges[i][1], edges[i][2])
                if (union1 && union2) {
                    common++
                } else if (union1 || union2) {
                    common--
                }
            }
        }
        // 删除独有边
        for (i in edges.indices) {
            if (edges[i][0] == 1) {
                uf1.union(edges[i][1], edges[i][2])
            } else if (edges[i][0] == 2) {
                uf2.union(edges[i][1], edges[i][2])
            }
        }
        return if (uf1.getDeleteCount() == -1 || uf2.getDeleteCount() == -1) {
            -1
        } else {
            // 返回两个人删除的边和减去多删除的边
            uf1.getDeleteCount() + uf2.getDeleteCount() - common
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val ret = maxNumEdgesToRemove(
            4,
            arrayOf(
                arrayOf(3, 1, 2),
                arrayOf(3, 2, 3), arrayOf(1, 1, 3), arrayOf(1, 2, 4), arrayOf(1, 1, 2), arrayOf(2, 3, 4)
            ).map { it.toIntArray() }.toTypedArray()
        )
        print(ret)
    }
}

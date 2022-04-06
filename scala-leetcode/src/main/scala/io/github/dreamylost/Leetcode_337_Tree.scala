/* Licensed under Apache-2.0 (C) All Contributors */
package io.github.dreamylost

/**
 * 间隔遍历
 *
 *     3
 *    / \
 *   2   3
 *    \   \
 *     3   1
 *
 * 337. House Robber III (Medium)
 *
 * Maximum amount of money the thief can rob = 3 + 3 + 1 = 7.
 *
 * @author 梦境迷离
 * @time 2018年8月10日
 * @version v1.0
 */
object Leetcode_337_Tree {

  /**
   * 对于一个以 root 为根节点的二叉树而言，如果尝试偷取 root 节点，那么势必不能偷取其左右子节点，然后继续尝试偷取其左右子节点的左右子节点。
   * 如果不偷取该节点，那么只能尝试偷取其左右子节点比较两种方式的结果，谁大取谁。
   *
   * 732 ms,33.33%
   * 53.7 MB,100.00%
   * 记忆优化
   *
   * @param root
   * @return
   */
  def rob(root: TreeNode): Int = {
    var memory = Map[TreeNode, Int]()

    def helper(r: TreeNode): Int = {
      if (r == null) return 0
      if (memory.contains(r)) return memory(r)
      var ccVal = r.value //孙子
      if (r.left != null) ccVal += helper(r.left.left) + helper(r.left.right)
      if (r.right != null) ccVal += helper(r.right.left) + helper(r.right.right)
      val result = math.max(helper(r.left) + helper(r.right), ccVal)
      memory = memory.+(r -> result)
      result
    }

    helper(root)
  }

  /**
   * 1372 ms,33.33%
   * 52.2 MB,100.00%
   *
   * @param root
   * @return
   */
  def rob_(root: TreeNode): Int = {
    if (root == null) return 0
    var vart = root.value
    if (root.left != null) vart += rob(root.left.left) + rob(root.left.right)
    if (root.right != null) vart += rob(root.right.left) + rob(root.right.right)
    val valt = rob(root.left) + rob(root.right)
    math.max(valt, vart)
  }

  /**
   * 后续中递归
   *
   * 688 ms,100.00%
   * 53.1 MB,100.00%
   *
   * @param root
   * @return
   */
  def rob2(root: TreeNode): Int = {
    if (root == null) return 0
    def helper(r: TreeNode): Seq[Int] = {
      if (r == null) return Seq(0, 0)
      // index 0 存储 下下层，偷root和root的下下层节点
      // index 1 存储下层（即不偷当前子树的root，而是偷root的左右子节点）
      val left = helper(r.left)
      val right = helper(r.right)
      val h = math.max(left.head + right.head + r.value, left(1) + right(1))
      Seq(left(1) + right(1), h)
    }
    helper(root)(1)
  }
}

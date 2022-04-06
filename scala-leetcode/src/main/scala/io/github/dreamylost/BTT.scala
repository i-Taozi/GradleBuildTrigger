/* Licensed under Apache-2.0 (C) All Contributors */
package io.github.dreamylost

//隐藏java集合

import java.util.{ Queue => _ }

//过期，以后使用List
import scala.collection.immutable.Queue

/**
 * 二叉树的遍历
 */
object BTT extends App {

  //前
  def qiandfs(root: TreeNode) {
    visit(root)
    qiandfs(root.left)
    qiandfs(root.right)
  }

  //中
  def zhongdfs(root: TreeNode) {
    zhongdfs(root.left)
    visit(root)
    zhongdfs(root.right)
  }

  //后
  def houdfs(root: TreeNode) {
    houdfs(root.left)
    houdfs(root.right)
    visit(root)
  }

  val visit = (root: TreeNode) => {
    println(root.value)
  }

  //前，144. Binary Tree Preorder Traversal (Medium)
  @unchecked
  def preorderTraversal(root: TreeNode): Seq[Int] = {
    var ret = Seq[Int]()
    var stack = List[TreeNode]()
    stack = root :: stack
    while (stack.nonEmpty) {
      val (node, s) = stack.head -> stack.tail
      stack = s
      if (node != null) {
        ret = ret ++ Seq(node.value)
        stack = node.right :: stack // 先右后左，保证左子树先遍历
        stack = node.left :: stack
      }
    }
    ret
  }

  /**
   * [因为是栈，先左子树出栈，后右子树出栈]
   * 前序遍历为 root -> left -> right，后序遍历为 left -> right -> root。可以修改前序遍历成为 root -> right -> left，那么这个顺序就和后序遍历正好相反。
   */
  //后，145. Binary Tree Postorder Traversal (Medium)
  @unchecked
  def postorderTraversal(root: TreeNode): Seq[Int] = {
    var ret = Seq[Int]()
    var stack = List[TreeNode]()
    stack = root :: stack
    while (stack.nonEmpty) {
      val (node, s) = stack.head -> stack.tail
      stack = s
      if (node != null) {
        ret = ret ++ Seq(node.value)
        stack = node.left :: stack // 先左后右，保证右子树先遍历
        stack = node.right :: stack
      }
    }
    ret.reverse
  }

  //层序
  def levelTraverse(root: TreeNode): Seq[Int] = {
    if (root == null) return Seq()
    var list = Seq[Int]()
    //Scala的Seq将是Java的List，Scala的List将是Java的LinkedList。
    var queue = Queue[TreeNode]() //层序遍历时保存结点的队列，可以省略new或者省略()
    queue = queue.enqueue(root)
    //初始化
    while (queue.nonEmpty) {
      val (node, q) = queue.dequeue
      queue = q
      list = list ++ Seq(node.value)
      if (node.left != null) queue = queue.enqueue(node.left)
      if (node.right != null) queue = queue.enqueue(node.right)
    }

    list
  }

  //根据中序的有序数组构造二叉树
  def buildSearchTree(values: Seq[Int], l: Int, r: Int): TreeNode = {
    if (l > r) {
      return null
    }
    if (l == r) {
      return new TreeNode(values(l))
    }
    val mid = l + (r - l) / 2
    val root = new TreeNode(values(mid))
    root.left = buildSearchTree(values, l, mid - 1)
    root.right = buildSearchTree(values, mid + 1, r)
    root
  }
}

/* Licensed under Apache-2.0 (C) All Contributors */
package io.github.dreamylost

/**
 * 303. 区域和检索 - 数组不可变
 * 给定一个整数数组  nums，求出数组从索引 i 到 j  (i ≤ j) 范围内元素的总和，包含 i,  j 两点。
 *
 * @author 梦境迷离
 * @time 2018年8月13日
 * @version v1.0
 */
object Leetcode_303 extends App {

  class NumArray(_nums: Array[Int]) {

    //牺牲空间
    for (i <- 1 until _nums.length)
      _nums(i) += _nums(i - 1);
    var nums = _nums;

    def sumRange(i: Int, j: Int): Int = {
      if (i == 0) return nums(j)
      nums(j) - nums(i - 1)
    }
  }

  val ret = new NumArray(Array(-2, 0, 3, -5, 2, -1)).sumRange(0, 2)
  print(ret)
}

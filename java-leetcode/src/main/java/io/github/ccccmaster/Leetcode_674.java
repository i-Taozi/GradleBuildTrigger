/* All Contributors (C) 2020 */
package io.github.ccccmaster;

/**
 * 最长连续递增序列 简单 练手
 *
 * @author chenyu
 * @date 2020-07-05.
 */
public class Leetcode_674 {

    public int findLengthOfLCIS(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        int maxLen = 1;
        int curLen = 1;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] > nums[i - 1]) {
                curLen++;
                maxLen = curLen > maxLen ? curLen : maxLen;
            } else {
                curLen = 1;
            }
        }
        return maxLen;
    }
}

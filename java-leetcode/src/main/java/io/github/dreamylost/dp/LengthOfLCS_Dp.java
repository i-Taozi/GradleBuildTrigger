/* All Contributors (C) 2020 */
package io.github.dreamylost.dp;

/**
 * 对于两个子序列 S1 和 S2，找出它们最长的公共子序列。
 *
 * <p>定义一个二维数组 dp 用来存储最长公共子序列的长度，其中 dp[i][j] 表示 S1 的前 i 个字符与 S2 的前 j 个字符最长公共子序列的长度。考虑 S1i 与 S2j
 * 值是否相等，分为两种情况：
 *
 * <p>当 S1i==S2j 时，那么就能在 S1 的前 i-1 个字符与 S2 的前 j-1 个字符最长公共子序列的基础上再加上 S1i 这个值，最长公共子序列长度加 1 ，
 *
 * <p>即 dp[i][j] = dp[i-1][j-1] + 1。
 *
 * <p>当 S1i != S2j 时，此时最长公共子序列为 S1的前 i-1 个字符和 S2 的前 j 个字符最长公共子序列， 与 S1 的前 i 个字符和 S2 的前 j-1
 * 个字符最长公共子序列，它们的最大者，
 *
 * <p>即dp[i][j] = max{ dp[i-1][j], dp[i][j-1] }。 综上，最长公共子序列的状态转移方程为：
 *
 * <p>对于长度为 N 的序列 S1 和 长度为 M 的序列 S2，dp[N][M] 就是序列 S1 和序列 S2 的最长公共子序列长度。
 *
 * <p>与最长递增子序列相比，最长公共子序列有以下不同点：
 *
 * <p>针对的是两个序列，求它们的最长公共子序列。 在最长递增子序列中，dp[i] 表示以 Si 为结尾的最长递增子序列长度，子序列必须包含 Si ；在最长公共子序列中，dp[i][j] 表示
 * S1 中前 i 个字符与 S2 中前 j 个字符的最长公共子序列长度，不一定包含 S1i 和 S2j 。 在求最终解时，最长公共子序列中 dp[N][M] 就是最终解，而最长递增子序列中
 * dp[N] 不是最终解，因为以 SN 为结尾的最长递增子序列不一定是整个序列最长递增子序列，需要遍历一遍 dp 数组找到最大者。
 *
 * @author 梦境迷离.
 * @time 2018年6月11日
 * @version v1.0
 */
public class LengthOfLCS_Dp {

    public static int lengthOfLCS(int[] nums1, int[] nums2) {
        int n1 = nums1.length, n2 = nums2.length;
        int[][] dp = new int[n1 + 1][n2 + 1];
        for (int i = 1; i < n1; i++) {
            for (int j = 1; j < n2; j++) {
                if (nums1[i - 1] == nums2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1; // 表示S1序列中前1个与S2中前1个有一个最大公共序列长度，值为前面+1
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]); // 求
                }
            }
        }
        return dp[n1][n2];
    }
}

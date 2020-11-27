package com.example.android.askquastionapp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private int sMoney = 20528;

    @Test
    public void addition_isCorrect() {
        for (int j = 0; j < 10; j++) {
            StringBuilder sb = new StringBuilder();
            int now = 0;
            for (int i = 0; i < 10; i++) {
                int test = test();
                now += test;
                sb.append(test > 0 ? "+" + test : test);
            }
            sMoney += now;
            System.out.println(sb + " = " + now + ", 还剩下：" + sMoney);
        }
    }

    /*zune:
    给出一个无重叠的 ，按照区间起始端点排序的区间列表。

    在列表中插入一个新的区间，你需要确保列表中的区间仍然有序且不重叠（如果有必要的话，可以合并区间）。
    输入：intervals = [[1,3],[6,9]], newInterval = [2,5]
    输出：[[1,5],[6,9]]
    输入：intervals = [[1,2],[3,5],[6,7],[8,10],[12,16]], newInterval = [4,8]
    输出：[[1,2],[3,10],[12,16]]
    解释：这是因为新的区间 [4,8] 与 [3,5],[6,7],[8,10] 重叠。

    来源：力扣（LeetCode）
    链接：https://leetcode-cn.com/problems/insert-interval
    著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
    **/
    private int test() {
        Random random = new Random();
        if (true) {
            boolean i = random.nextBoolean();
            return i ? 1000 : -1000;
        }
        int a = 100000;
        int n = 2;
        int time = 0;
        int max = a;
        int maxTime = 0;
        List<Integer> win = new ArrayList<>();
        while ((sMoney > n || a > n) && time < 1000 && n <= 32) {
            time++;
            boolean i = random.nextBoolean();
            if (i) {
                a += n;
                n = 2;
            } else {
                a -= n;
                n *= 2;
                n += 2;
            }
            win.add(a);
            if (max <= a) {
                max = a;
                maxTime = time;
            }
        }
        return a - 100000;
    }

    private String getNormalNo(String mobileNo) {
        String[] s = mobileNo.split(" ");
        if (s.length == 1) {
            return mobileNo;
        }
        return s[0] + " " + mobileNo.substring(s[0].length()).replaceAll("[^\\d]", "");
    }
}
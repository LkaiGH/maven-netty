package com.open.coinnews.utils;

/**
 * @author guominghai
 * @Description: 去重工具类
 * @Date Created in 10:17 2018/6/14
 */
public class Levenshtein {

    /**
     * 获取两字符串进行比较
     * @param str
     * @param target
     * @return
     **/
    private int compare(String str, String target) {
        //可以替换大部分空白字符， 不限于空格 ；
        str = str.replaceAll("\\s*", "");
        target = target.replaceAll("\\s*", "");
        //去除特殊符号
        str = str.replaceAll("[^0-9a-zA-Z\u4e00-\u9fa5.，,。？“”]+","");
        target = target.replaceAll("[^0-9a-zA-Z\u4e00-\u9fa5.，,。？“”]+","");
        int d[][]; // 矩阵
        int n = str.length();
        int m = target.length();
        int i; // 遍历str的
        int j; // 遍历target的
        char ch1; // str的
        char ch2; // target的
        int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];
        for (i = 0; i <= n; i++) { // 初始化第一列
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++) { // 初始化第一行
            d[0][j] = j;
        }
        for (i = 1; i <= n; i++) { // 遍历str
            ch1 = str.charAt(i - 1);//返回单个字符串
            // 去匹配target
            for (j = 1; j <= m; j++) {
                ch2 = target.charAt(j - 1);//返回单个字符串
                if (ch1 == ch2) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                // 左边+1,上边+1, 左上角+temp取最小
                d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
            }
        }
        return d[n][m];
    }

    private int min(int one, int two, int three) {
        return (one = one < two ? one : two) < three ? one : three;
    }

    /**
     * 获取两字符串的相似度
     * @param str
     * @param target
     * @return
     */
    public float getSimilarityRatio(String str, String target) {
        return 1 - (float)compare(str, target)/Math.max(str.length(), target.length());
    }

    public static void main(String[] args) {
        Levenshtein lt = new Levenshtein();
        String str = "32p32/1321/321321";
        String target = "(123123123123123)";
        float ii = lt.getSimilarityRatio(str, target);
        System.out.println("similarityRatio="+ ii);
    }
}

/**
 *
 * APDPlat - Application Product Development Platform
 * Copyright (c) 2013, 杨尚川, yang-shangchuan@qq.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.apdplat.word.analysis;

import org.apdplat.word.segmentation.Word;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文本相似度计算
 * 判定方式：余弦相似度，通过计算两个向量的夹角余弦值来评估他们的相似度
 * @author 杨尚川
 */
public class CosineTextSimilarity extends TextSimilarity {
    /**
     * 判定相似度的方式：余弦相似度
     * 余弦夹角原理：
     * 向量a=(x1,y1),向量b=(x2,y2)
     * similarity=a.b/|a|*|b|
     * a.b=x1x2+y1y2
     * |a|=根号[(x1)^2+(y1)^2],|b|=根号[(x2)^2+(y2)^2]
     * @param words1 词列表1
     * @param words2 词列表2
     * @return 相似度分值
     */
    @Override
    protected double scoreImpl(List<Word> words1, List<Word> words2) {
        //词频统计
        Map<Word, AtomicInteger> frequency1 = frequency(words1);
        Map<Word, AtomicInteger> frequency2 = frequency(words2);
        //输出词频统计信息
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("词频统计1：\n{}", formatWordsFrequency(frequency1));
            LOGGER.debug("词频统计2：\n{}", formatWordsFrequency(frequency2));
        }
        //所有的不重复词
        Set<Word> words = new HashSet<>();
        words.addAll(words1);
        words.addAll(words2);
        //向量的维度为words的大小，每一个维度的权重是词频
        //a.b
        AtomicInteger ab = new AtomicInteger();
        //|a|的平方
        AtomicInteger aa = new AtomicInteger();
        //|b|的平方
        AtomicInteger bb = new AtomicInteger();
        //计算
        words
            .stream()
            .forEach(word -> {
                AtomicInteger x1 = frequency1.get(word);
                AtomicInteger x2 = frequency2.get(word);
                if (x1 != null && x2 != null) {
                    //x1x2
                    int oneOfTheDimension = x1.get() * x2.get();
                    //+
                    ab.addAndGet(oneOfTheDimension);
                }
                if (x1 != null) {
                    //(x1)^2
                    int oneOfTheDimension = x1.get() * x1.get();
                    //+
                    aa.addAndGet(oneOfTheDimension);
                }
                if (x2 != null) {
                    //(x2)^2
                    int oneOfTheDimension = x2.get() * x2.get();
                    //+
                    bb.addAndGet(oneOfTheDimension);
                }
            });
        //|a|
        double aaa = Math.sqrt(aa.get());
        //|b|
        double bbb = Math.sqrt(bb.get());
        //使用BigDecimal保证精确计算浮点数
        //|a|*|b|
        BigDecimal aabb = BigDecimal.valueOf(aaa).multiply(BigDecimal.valueOf(bbb));
        //similarity=a.b/|a|*|b|
        double cos = ab.get()/aabb.doubleValue();
        return cos;
    }

    /**
     * 统计词频
     * @param words 词列表
     * @return 词频统计结果
     */
    private Map<Word, AtomicInteger> frequency(List<Word> words){
        Map<Word, AtomicInteger> frequency =new HashMap<>();
        words.forEach(word->{
            frequency.putIfAbsent(word, new AtomicInteger());
            frequency.get(word).incrementAndGet();
        });
        return frequency;
    }

    /**
     * 格式化词频统计信息
     * @param frequency 词频统计信息
     */
    private String formatWordsFrequency(Map<Word, AtomicInteger> frequency){
        StringBuilder str = new StringBuilder();
        if(frequency != null && !frequency.isEmpty()) {
            AtomicInteger c = new AtomicInteger();
            frequency
                    .entrySet()
                    .stream()
                    .sorted((a, b) -> b.getValue().get() - a.getValue().get())
                    .forEach(e -> str.append("\t").append(c.incrementAndGet()).append("、").append(e.getKey()).append("=").append(e.getValue()).append("\n"));
        }
        str.setLength(str.length()-1);
        return str.toString();
    }

    public static void main(String[] args) {
        String text1 = "我爱购物";
        String text2 = "我爱读书";
        String text3 = "他是黑客";
        TextSimilarity textSimilarity = new CosineTextSimilarity();
        double score1pk1 = textSimilarity.similarScore(text1, text1);
        double score1pk2 = textSimilarity.similarScore(text1, text2);
        double score1pk3 = textSimilarity.similarScore(text1, text3);
        double score2pk2 = textSimilarity.similarScore(text2, text2);
        double score2pk3 = textSimilarity.similarScore(text2, text3);
        double score3pk3 = textSimilarity.similarScore(text3, text3);
        System.out.println(text1+" 和 "+text1+" 的相似度分值："+score1pk1);
        System.out.println(text1+" 和 "+text2+" 的相似度分值："+score1pk2);
        System.out.println(text1+" 和 "+text3+" 的相似度分值："+score1pk3);
        System.out.println(text2+" 和 "+text2+" 的相似度分值："+score2pk2);
        System.out.println(text2+" 和 "+text3+" 的相似度分值："+score2pk3);
        System.out.println(text3+" 和 "+text3+" 的相似度分值："+score3pk3);
    }
}
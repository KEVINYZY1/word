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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文本相似度计算
 * 判定方式：简单共有词，通过计算两篇文档有多少个相同的词来评估他们的相似度
 * @author 杨尚川
 */
public class SimpleTextSimilarity extends TextSimilarity {
    /**
     * 判定相似度的方式：简单共有词
     * @param words1 词列表1
     * @param words2 词列表2
     * @return 相似度分值
     */
    @Override
    protected double scoreImpl(List<Word> words1, List<Word> words2) {
        //计算词列表1总的字符数
        AtomicInteger words1Length = new AtomicInteger();
        words1.parallelStream().forEach(word -> words1Length.addAndGet(word.getText().length()));
        //计算词列表2总的字符数
        AtomicInteger words2Length = new AtomicInteger();
        words2.parallelStream().forEach(word -> words2Length.addAndGet(word.getText().length()));
        //计算词列表1和词列表2共有的词的总的字符数
        //HashSet的contains性能要大于ArrayList的contains
        Set<Word> words2Set = new HashSet<>();
        words2Set.addAll(words2);
        AtomicInteger intersectionLength = new AtomicInteger();
        words1.parallelStream().forEach(word -> {
            if (words2Set.contains(word)) {
                intersectionLength.addAndGet(word.getText().length());
            }
        });
        double score = intersectionLength.get()/(double)Math.min(words1Length.get(), words2Length.get());
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("词列表1总的字符数：" + words1Length.get());
            LOGGER.debug("词列表2总的字符数：" + words2Length.get());
            LOGGER.debug("词列表1和2共有的词的总的字符数：" + intersectionLength.get());
            LOGGER.debug("相似度分值=" + intersectionLength.get() + "/(double)Math.min(" + words1Length.get() + ", " + words1Length.get() + ")=" + score);
        }
        return score;
    }

    public static void main(String[] args) {
        String text1 = "我爱购物";
        String text2 = "我爱读书";
        String text3 = "他是黑客";
        TextSimilarity textSimilarity = new SimpleTextSimilarity();
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
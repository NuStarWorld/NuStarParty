/*
 *    NuStarParty
 *    Copyright (C) 2025  NuStar
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.nustar.nustarparty.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author NuStar
 * @since 2025/8/1 00:22
 */
public class StringUtil {
    public static List<String> wrapText(String text, int maxLength) {
        String prefix = "§f";
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // 检查加上新单词是否会超过最大长度
            if (currentLine.length() + word.length() + 1 <= maxLength) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                // 如果当前行已经有内容，保存当前行并开始新行
                if (currentLine.length() > 0) {
                    lines.add(prefix + currentLine);
                } else {
                    // 如果单个词就超过长度，按最大长度截断
                    while (word.length() > maxLength) {
                        lines.add(prefix + word.substring(0, maxLength));
                        word = word.substring(maxLength);
                    }
                }
                currentLine = new StringBuilder(word);
            }
        }

        // 添加最后一行
        if (currentLine.length() > 0) {
            lines.add(prefix + currentLine);
        }

        return lines;
    }
}

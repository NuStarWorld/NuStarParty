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

package top.nustar.nustarparty.api.service;

import java.util.function.Function;
import top.nustar.nustarparty.api.placeholder.MenuPlaceholderContext;

/**
 * @author NuStar
 * @since 2025/7/10 00:44
 */
public interface MenuPlaceholderRegistry {
    /**
     * 注册一个菜单占位符，允许开发者自定义占位符的逻辑。
     *
     * <p>占位符名称会自动被 <code>{}</code> 包裹，例如传入 <code>"name"</code> 后，实际使用时应为 <code>{name}</code>。
     *
     * <p><strong>示例1：</strong> 返回玩家名称
     *
     * <pre>{@code
     * registerPlaceholder("name", ctx -> ctx.getPlayer().getName(), false);
     * }</pre>
     *
     * <p><strong>示例2：</strong> 计算并格式化玩家血量百分比
     *
     * <pre>{@code
     * registerPlaceholder("percentageHealth", ctx -> {
     *     Player onlinePlayer = ctx.getOnlinePlayer();
     *     double health = onlinePlayer.getHealth();
     *     double maxHealth = onlinePlayer.getMaxHealth();
     *     return String.format("%.2f%%", health / maxHealth * 100);
     * }, true);
     * }</pre>
     *
     * <p><strong>注意事项：</strong>
     *
     * <ul>
     *   <li>回调函数返回的对象必须能正确转换为字符串（例如，避免返回 <code>null</code> 或不可序列化的对象）。
     *   <li>占位符名称应避免使用特殊字符（如空格、<code>{}</code> 等）。
     *   <li>回调函数应尽量保持高效，避免耗时操作，以免影响菜单渲染性能。
     *   <li>函数内的玩家对象在线状态需要特别注意，即 <code>requireOnlinePlayer</code> 参数
     * </ul>
     *
     * @param placeholder 占位符名称，传入的字符串默认会被 "{}" 包裹，不需要手动添加
     * @param function 回调函数
     * @param requireOnlinePlayer 是否要求玩家在线
     */
    void registerPlaceholder(
            String placeholder, Function<MenuPlaceholderContext, Object> function, boolean requireOnlinePlayer);

    /**
     * 解析一个菜单占位符
     *
     * <p><strong>示例：</strong> 解析 {name} 占位符
     *
     * <pre>{@code
     * parse("{name}", MenuPlaceholderContext.create(player));
     * }</pre>
     *
     * @param string 占位符名称，传入的占位符需要添加 "{}"
     * @param ctx 占位符上下文，需要传入一个玩家对象
     * @return 解析后的结果
     */
    String parse(String string, MenuPlaceholderContext ctx);
}

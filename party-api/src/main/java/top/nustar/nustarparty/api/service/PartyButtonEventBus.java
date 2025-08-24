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

import top.nustar.nustarparty.api.menu.MenuExecuteResult;
import top.nustar.nustarparty.api.menu.PartyButton;
import top.nustar.nustarparty.api.menu.PartyMenuContext;

/**
 * @author NuStar
 * @since 2025/8/7 00:35
 */
public interface PartyButtonEventBus {
    /**
     * 触发一个菜单静态按钮的逻辑
     *
     * @param type 按钮类型
     * @param ctx 菜单上下文
     * @return 菜单执行结果
     */
    MenuExecuteResult post(String type, PartyMenuContext ctx);

    /**
     * 注册一个菜单静态按钮
     *
     * @param partyButton 按钮对象
     */
    PartyButtonEventBus registerPartyButton(PartyButton partyButton);
}

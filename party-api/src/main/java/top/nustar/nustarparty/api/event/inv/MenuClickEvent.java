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

package top.nustar.nustarparty.api.event.inv;

import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import top.nustar.nustargui.entity.NuStarMenuHolder;

/**
 * @author NuStar
 * @since 2025/7/15 21:27
 */
@Getter
@RequiredArgsConstructor
public class MenuClickEvent extends Event {
    @Getter
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    private final Player player;
    private final MenuType menuType;
    private final ItemStack currentItem;
    private final NuStarMenuHolder holder;
    private final ClickType clickType;

    public enum MenuType {
        PARTY_MENU("队伍菜单"),
        MY_PARTY_MENU("我的队伍"),
        JOIN_APPLICATION_MENU("加入队伍申请列表"),
        INVITE_MENU("邀请列表"),
        PLAYER_LIST_MENU("玩家列表");

        private final String menuName;

        MenuType(String menuName) {
            this.menuName = menuName;
        }

        public static Optional<MenuType> of(String menuName) {
            for (MenuType menuType : values()) {
                if (menuType.menuName.equals(menuName)) {
                    return Optional.of(menuType);
                }
            }
            return Optional.empty();
        }
    }
}

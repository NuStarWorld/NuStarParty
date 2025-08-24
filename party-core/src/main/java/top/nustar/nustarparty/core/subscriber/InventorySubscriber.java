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

package top.nustar.nustarparty.core.subscriber;

import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import team.idealstate.sugar.next.context.annotation.component.Subscriber;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import top.nustar.nustargui.entity.NuStarMenuHolder;
import top.nustar.nustarparty.api.event.inv.MenuClickEvent;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;

/**
 * @author NuStar
 * @since 2025/6/16 22:38
 */
@Subscriber
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
public class InventorySubscriber implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof NuStarMenuHolder)) return;

        NuStarMenuHolder menuHolder = (NuStarMenuHolder) event.getInventory().getHolder();
        Optional<MenuClickEvent.MenuType> menuType = MenuClickEvent.MenuType.of(menuHolder.getMenuType());
        if (!menuType.isPresent()) return;

        event.setCancelled(true);

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR) return;
        Player player = (Player) event.getWhoClicked();

        Bukkit.getPluginManager()
                .callEvent(new MenuClickEvent(player, menuType.get(), currentItem, menuHolder, event.getClick()));
    }
}

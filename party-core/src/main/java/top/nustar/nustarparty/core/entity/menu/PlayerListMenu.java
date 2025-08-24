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

package top.nustar.nustarparty.core.entity.menu;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.nustar.nustarparty.api.NuStarPartyAPI;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.core.factory.PartyMenuFactory;

/**
 * @author NuStar
 * @since 2025/8/10 15:58
 */
public class PlayerListMenu extends AbstractMenu {
    private final List<Player> nonPartyPlayers;

    public PlayerListMenu(
            String menuTitle,
            String menuName,
            Inventory templateInventory,
            Party party,
            Player holder,
            PartyMenuFactory partyMenuFactory) {
        super(menuTitle, menuName, templateInventory, party, holder, partyMenuFactory);
        nonPartyPlayers = NuStarPartyAPI.getNonPartyPlayers().stream()
                .filter(player -> !player.getUniqueId().equals(holder.getUniqueId()))
                .collect(Collectors.toList());
    }

    @Override
    public Inventory refresh(Player player) {
        inventory.clear();
        ItemStack[] contents = templateInventory.getContents();

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, nonPartyPlayers.size());
        int slotIndex = 0;

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                inventory.setItem(i, contents[i].clone());
                continue;
            }
            if (startIndex + slotIndex < endIndex) {
                OfflinePlayer listPlayer = nonPartyPlayers.get(startIndex + slotIndex);
                ItemStack playerButton = partyMenuFactory.createPlayerButton(holder, listPlayer);
                inventory.setItem(i, playerButton);
                dynamicButtonMap.put(playerButton, listPlayer.getUniqueId());
                slotIndex++;
            } else {
                inventory.setItem(i, partyMenuFactory.getPlayerListEmptyButton(holder, holder));
            }
        }
        return inventory;
    }

    @Override
    public boolean hasNextPage() {
        return currentPage * itemsPerPage < nonPartyPlayers.size();
    }
}

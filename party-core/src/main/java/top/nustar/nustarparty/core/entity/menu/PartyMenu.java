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
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.nustar.nustargui.entity.NuStarMenuHolder;
import top.nustar.nustarparty.api.NuStarPartyAPI;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.core.factory.PartyMenuFactory;

/**
 * @author NuStar
 * @since 2025/6/14 21:42
 */
@Getter
public class PartyMenu extends AbstractMenu {
    public PartyMenu(
            String menuTitle,
            String menuName,
            Inventory templateInventory,
            Party party,
            Player holder,
            PartyMenuFactory partyMenuFactory) {
        super(menuTitle, menuName, templateInventory, party, holder, partyMenuFactory);
    }

    @Override
    public boolean hasNextPage() {
        int totalParties = NuStarPartyAPI.getPartyList().size();
        return currentPage * itemsPerPage < totalParties;
    }

    @Override
    public Inventory refresh(Player player) {
        if (inventory == null) {
            inventory = Bukkit.createInventory(
                    new NuStarMenuHolder((NuStarMenuHolder) templateInventory.getHolder(), this),
                    templateInventory.getSize(),
                    title);
        }
        List<Party> allParties = NuStarPartyAPI.getPartyList();
        inventory.clear();

        // 起始下标
        int startIndex = (currentPage - 1) * itemsPerPage;
        // 结束下标
        int endIndex = Math.min(startIndex + itemsPerPage, allParties.size());
        // 当前下标
        int slotIndex = 0;

        ItemStack[] contents = templateInventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                inventory.setItem(i, contents[i].clone());
            } else {
                // 检查当前索引是否能获取到队伍对象
                if (startIndex + slotIndex < endIndex) {
                    Party party = allParties.get(startIndex + slotIndex);
                    ItemStack partyButton = partyMenuFactory.createPartyButton(party);
                    dynamicButtonMap.put(partyButton, party.getPartyUUID());
                    inventory.setItem(i, partyButton);
                    slotIndex++;
                } else {
                    inventory.setItem(i, partyMenuFactory.getPartyEmptyButton());
                }
            }
        }
        return inventory;
    }
}

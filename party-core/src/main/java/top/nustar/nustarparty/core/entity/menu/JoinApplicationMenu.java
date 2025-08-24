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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.core.factory.PartyMenuFactory;

/**
 * @author NuStar
 * @since 2025/7/16 20:45
 */
public class JoinApplicationMenu extends AbstractMenu {

    public JoinApplicationMenu(
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
        return currentPage * itemsPerPage < party.getJoinApplicationList().size();
    }

    @Override
    public Inventory refresh(Player player) {
        inventory.clear();
        ItemStack[] contents = templateInventory.getContents();

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(
                startIndex + itemsPerPage, party.getJoinApplicationList().size());
        int slotIndex = 0;

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                inventory.setItem(i, contents[i].clone());
                continue;
            }
            List<OfflinePlayer> joinApplications = party.getJoinApplicationList();
            if (startIndex + slotIndex < endIndex) {
                OfflinePlayer applicant = joinApplications.get(startIndex + slotIndex);
                ItemStack applicantButton = partyMenuFactory.createApplicantButton(holder, applicant);
                inventory.setItem(i, applicantButton);
                dynamicButtonMap.put(applicantButton, applicant.getUniqueId());
                slotIndex++;
            } else {
                inventory.setItem(i, partyMenuFactory.getApplicationEmptyButton(holder, party.getLeader()));
            }
        }
        return inventory;
    }
}

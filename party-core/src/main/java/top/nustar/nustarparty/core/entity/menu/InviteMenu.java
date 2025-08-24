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
import top.nustar.nustarparty.api.NuStarPartyAPI;
import top.nustar.nustarparty.api.entity.Invite;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.entity.component.InviteApplication;
import top.nustar.nustarparty.core.factory.PartyMenuFactory;

/**
 * @author NuStar
 * @since 2025/7/16 20:45
 */
public class InviteMenu extends AbstractMenu {

    private final Invite inviteImpl =
            NuStarPartyAPI.getPlayerInvite(holder.getUniqueId()).orElse(null);

    public InviteMenu(
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
        return currentPage * itemsPerPage < inviteImpl.getInviteApplications().size();
    }

    @Override
    public Inventory refresh(Player player) {
        inventory.clear();
        ItemStack[] contents = templateInventory.getContents();

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(
                startIndex + itemsPerPage, inviteImpl.getInviteApplications().size());
        int slotIndex = 0;

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                inventory.setItem(i, contents[i].clone());
                continue;
            }
            List<InviteApplication> inviteApplications = inviteImpl.getInviteApplications();
            if (startIndex + slotIndex < endIndex) {
                OfflinePlayer inviter =
                        inviteApplications.get(startIndex + slotIndex).getInviter();
                ItemStack inviterButton = partyMenuFactory.createInviterButton(holder, inviter);
                inventory.setItem(i, inviterButton);
                dynamicButtonMap.put(inviterButton, inviter.getUniqueId());
                slotIndex++;
            } else {
                inventory.setItem(i, partyMenuFactory.getInviteEmptyButton(holder, holder));
            }
        }
        return inventory;
    }
}

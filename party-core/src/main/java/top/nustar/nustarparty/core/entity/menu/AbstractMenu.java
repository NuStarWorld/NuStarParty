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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.nustar.nustargui.entity.AbsNuStarGui;
import top.nustar.nustargui.entity.NuStarMenuHolder;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.core.factory.PartyMenuFactory;

/**
 * @author NuStar
 * @since 2025/7/30 22:18
 */
@Getter
public abstract class AbstractMenu extends AbsNuStarGui {
    protected final Party party;
    protected final Player holder;
    protected Inventory inventory;
    protected final PartyMenuFactory partyMenuFactory;
    protected final Map<ItemStack, UUID> dynamicButtonMap = new HashMap<>();
    protected int itemsPerPage;

    public AbstractMenu(
            String menuTitle,
            String menuName,
            Inventory templateInventory,
            Party party,
            Player holder,
            PartyMenuFactory partyMenuFactory) {
        super(menuTitle, menuName, templateInventory);
        this.party = party;
        this.holder = holder;
        this.partyMenuFactory = partyMenuFactory;
        itemsPerPage = calculateAvailableSlots(templateInventory);
        inventory = Bukkit.createInventory(
                new NuStarMenuHolder((NuStarMenuHolder) templateInventory.getHolder(), this),
                templateInventory.getSize(),
                title);
    }
}

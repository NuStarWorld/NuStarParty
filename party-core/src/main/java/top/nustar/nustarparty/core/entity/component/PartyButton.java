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

package top.nustar.nustarparty.core.entity.component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.nustar.nustarparty.api.entity.Party;

/**
 * @author NuStar
 * @since 2025/6/17 21:20
 */
@Getter
public class PartyButton extends AbstractTypeButton {

    public PartyButton(ConfigurationSection section) {
        super(section);
    }

    public ItemStack getButton(Party party) {
        ItemStack item = getButton().clone();
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(meta.getDisplayName().replace("{name}", party.getPartyName()));

        List<String> partyMembersName = party.getMembers().stream()
                .map(offlinePlayer -> "§f - " + offlinePlayer.getName())
                .collect(Collectors.toList());
        partyMembersName.add(0, "§f - " + party.getLeader().getName() + " §6(Leader)");

        List<String> newLore = meta.getLore().stream()
                .flatMap(lore -> {
                    lore = lore.replace("{current}", (party.getMembers().size() + 1) + "")
                            .replace("{max}", party.getMaxSize() + "");
                    if (lore.contains("{members}")) {
                        return Stream.concat(Stream.of(lore.replace("{members}", "")), partyMembersName.stream());
                    } else {
                        return Stream.of(lore);
                    }
                })
                .collect(Collectors.toList());

        meta.setLore(newLore);
        item.setItemMeta(meta);
        return item;
    }
}

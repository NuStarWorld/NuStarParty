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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.nustar.nustarparty.api.NuStarPartyAPI;
import top.nustar.nustarparty.api.PartyMenuAPI;
import top.nustar.nustarparty.api.entity.Invite;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.placeholder.MenuPlaceholderContext;
import top.nustar.nustarparty.core.utils.StringUtil;

/**
 * @author NuStar
 * @since 2025/7/15 21:08
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
public class PlayerButton extends AbstractTypeButton {

    private final Optional<List<String>> offlineLoreOptional;
    private final Optional<List<String>> leaderApplyLoreOptional;
    private final Optional<List<String>> inviteApplyLoreOptional;

    public PlayerButton(ConfigurationSection section) {
        super(section);
        offlineLoreOptional = Optional.ofNullable(section.getStringList("offline-lore"));
        leaderApplyLoreOptional = Optional.ofNullable(section.getStringList("leader-apply-lore"));
        inviteApplyLoreOptional = Optional.ofNullable(section.getStringList("invite-apply-lore"));
    }

    public ItemStack getButton(Player holder, Player member) {
        ItemStack itemStack = getPapiItem(member);
        ItemMeta itemMeta = itemStack.getItemMeta();
        MenuPlaceholderContext ctx = MenuPlaceholderContext.create(member);
        itemMeta.setDisplayName(PartyMenuAPI.parse(itemMeta.getDisplayName(), ctx));
        List<String> lore = itemMeta.getLore();
        List<String> collect =
                lore.stream().map(line -> PartyMenuAPI.parse(line, ctx)).collect(Collectors.toList());
        applyLeaderLore(collect, holder);
        applyInviteLore(collect, holder, member);
        itemMeta.setLore(collect);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack getButton(Player holder, OfflinePlayer member) {
        ItemStack itemStack = getButton();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(PartyMenuAPI.parse(itemMeta.getDisplayName(), MenuPlaceholderContext.create(member)));
        List<String> collect = offlineLoreOptional
                .map(offlineLore -> offlineLore.stream()
                        .map(line -> PartyMenuAPI.parse(line, MenuPlaceholderContext.create(member)))
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());
        applyLeaderLore(collect, holder);
        applyInviteLore(collect, holder, member);
        itemMeta.setLore(collect);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private void applyLeaderLore(List<String> lore, Player holder) {
        Optional<Party> playerParty = NuStarPartyAPI.getPlayerParty(holder.getUniqueId());
        playerParty.ifPresent(party -> leaderApplyLoreOptional.ifPresent(leaderApplyLoreOptional -> {
            if (party.isLeader(holder.getUniqueId())) {
                lore.addAll(leaderApplyLoreOptional);
            }
        }));
    }

    private void applyInviteLore(List<String> lore, Player holder, OfflinePlayer member) {
        Optional<Invite> playerInvite = NuStarPartyAPI.getPlayerInvite(holder.getUniqueId());
        playerInvite.ifPresent(invite -> inviteApplyLoreOptional.ifPresent(inviteApplyLore -> {
            invite.getInviteApplication(member.getUniqueId()).ifPresent(inviteApplication -> {
                inviteApplyLore.stream()
                        .flatMap(line -> {
                            if (line.contains("{inviteReason}")) {
                                return Stream.concat(
                                        Stream.of(line.replace("{inviteReason}", "")),
                                        StringUtil.wrapText(inviteApplication.getInviteReason(), 12).stream());
                            } else {
                                return Stream.of(line);
                            }
                        })
                        .forEach(lore::add);
            });
        }));
    }
}

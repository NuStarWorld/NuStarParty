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

package top.nustar.nustarparty.core.placeholder;

import static top.nustar.nustarparty.core.utils.PlayerUtil.getPlayer;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import team.idealstate.minecraft.next.spigot.api.placeholder.Placeholder;
import team.idealstate.sugar.logging.Log;
import team.idealstate.sugar.next.command.CommandContext;
import team.idealstate.sugar.next.command.CommandResult;
import team.idealstate.sugar.next.command.annotation.CommandArgument;
import team.idealstate.sugar.next.command.annotation.CommandHandler;
import team.idealstate.sugar.next.context.annotation.component.Controller;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import team.idealstate.sugar.next.function.closure.Function;
import team.idealstate.sugar.validate.annotation.NotNull;
import top.nustar.nustarparty.api.PartyMenuAPI;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.placeholder.MenuPlaceholderContext;
import top.nustar.nustarparty.api.service.PartyService;

/**
 * @author NuStar
 * @since 2025/8/1 22:55
 */
@Controller(name = "NuStarParty")
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
@SuppressWarnings("unused")
public class PartyPlaceholder implements Placeholder {
    private volatile PartyService service;
    private static final String NULL = "null";

    @NotNull
    @CommandHandler(value = "myParty leaderUid", open = true)
    public CommandResult myPartyLeaderUid(@NotNull CommandContext context) {
        return executeMyParty(context, Party::getLeaderUid);
    }

    @NotNull
    @CommandHandler(value = "myParty leaderName", open = true)
    public CommandResult myPartyLeaderName(@NotNull CommandContext context) {
        return executeMyParty(context, Party::getLeaderName);
    }

    @NotNull
    @CommandHandler(value = "myParty size", open = true)
    public CommandResult myPartySize(@NotNull CommandContext context) {
        return executeMyParty(context, Party::getSize);
    }

    @NotNull
    @CommandHandler(value = "myParty emptyPositions", open = true)
    public CommandResult myPartyEmptyPositions(@NotNull CommandContext context) {
        return executeMyParty(context, Party::getEmptyPositions);
    }

    @NotNull
    @CommandHandler(value = "myParty maxSize", open = true)
    public CommandResult myPartyMaxSize(@NotNull CommandContext context) {
        return executeMyParty(context, Party::getMaxSize);
    }

    @NotNull
    @CommandHandler(value = "myParty isFull", open = true)
    public CommandResult myPartyIsFull(@NotNull CommandContext context) {
        return executeMyParty(context, Party::isFull);
    }

    @NotNull
    @CommandHandler(value = "myParty isMember {memberName}", open = true)
    public CommandResult myPartyIsMember(@NotNull CommandContext context, @CommandArgument() String memberName) {
        return executeMyParty(context, party -> party.isMember(memberName));
    }

    @NotNull
    @CommandHandler(value = "myParty isLeader {memberName}", open = true)
    public CommandResult myPartyIsLeader(@NotNull CommandContext context, @CommandArgument() String memberName) {
        return executeMyParty(context, party -> party.isLeader(memberName));
    }

    @NotNull
    @CommandHandler(value = "myParty partyName", open = true)
    public CommandResult myPartyPartyName(@NotNull CommandContext context) {
        return executeMyParty(context, Party::getPartyName);
    }

    @NotNull
    @CommandHandler(value = "myParty memberName {index}", open = true)
    public CommandResult myPartyMemberName(@NotNull CommandContext context, @CommandArgument() String index) {
        int memberIndexInt = Integer.parseInt(index);
        return executeMyParty(
                context,
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getName());
    }

    @NotNull
    @CommandHandler(value = "myParty memberUid {index}", open = true)
    public CommandResult myPartyMemberUid(@NotNull CommandContext context, @CommandArgument() String index) {
        int memberIndexInt = Integer.parseInt(index);
        return executeMyParty(
                context,
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getUniqueId().toString());
    }

    @NotNull
    @CommandHandler(value = "partyListSize", open = true)
    public CommandResult partyListSize(@NotNull CommandContext context) {
        return CommandResult.success(String.valueOf(service.getPartyList().size()));
    }

    @NotNull
    @CommandHandler(value = "partyName {index}", open = true)
    public CommandResult partyName(@NotNull CommandContext context, @CommandArgument() String index) {
        return executePartyList(Integer.parseInt(index), Party::getPartyName);
    }

    @NotNull
    @CommandHandler(value = "partyMaxSize {index}", open = true)
    public CommandResult partyMaxSize(@NotNull CommandContext context, @CommandArgument() String index) {
        return executePartyList(Integer.parseInt(index), Party::getMaxSize);
    }

    @NotNull
    @CommandHandler(value = "partySize {index}", open = true)
    public CommandResult partySize(@NotNull CommandContext context, @CommandArgument() String index) {
        return executePartyList(Integer.parseInt(index), Party::getSize);
    }

    @NotNull
    @CommandHandler(value = "partyEmptyPositions {index}", open = true)
    public CommandResult partyEmptyPositions(@NotNull CommandContext context, @CommandArgument() String index) {
        return executePartyList(Integer.parseInt(index), Party::getEmptyPositions);
    }

    @NotNull
    @CommandHandler(value = "partyIsFull {index}", open = true)
    public CommandResult partyIsFull(@NotNull CommandContext context, @CommandArgument() String index) {
        return executePartyList(Integer.parseInt(index), Party::isFull);
    }

    @NotNull
    @CommandHandler(value = "partyLeaderName {index}", open = true)
    public CommandResult partyLeaderName(@NotNull CommandContext context, @CommandArgument() String index) {
        return executePartyList(Integer.parseInt(index), Party::getLeaderName);
    }

    @NotNull
    @CommandHandler(value = "partyLeaderUid {index}", open = true)
    public CommandResult partyLeaderUid(@NotNull CommandContext context, @CommandArgument() String index) {
        return executePartyList(Integer.parseInt(index), Party::getLeaderUid);
    }

    @NotNull
    @CommandHandler(value = "partyIsMember {index} {playerName}", open = true)
    public CommandResult partyIsMember(
            @NotNull CommandContext context, @CommandArgument() String index, @CommandArgument() String playerName) {
        return executePartyList(Integer.parseInt(index), party -> party.isMember(playerName));
    }

    @NotNull
    @CommandHandler(value = "partyIsLeader {index} {playerName}", open = true)
    public CommandResult partyIsLeader(
            @NotNull CommandContext context, @CommandArgument() String index, @CommandArgument() String playerName) {
        return executePartyList(Integer.parseInt(index), party -> party.isLeader(playerName));
    }

    @NotNull
    @CommandHandler(value = "partyMemberUid {partyIndex} {memberIndex}", open = true)
    public CommandResult partyMemberUid(
            @NotNull CommandContext context,
            @CommandArgument() String partyIndex,
            @CommandArgument() String memberIndex) {
        int memberIndexInt = Integer.parseInt(memberIndex);
        return executePartyList(
                Integer.parseInt(partyIndex),
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getUniqueId().toString());
    }

    @NotNull
    @CommandHandler(value = "partyMemberName {partyIndex} {memberIndex}", open = true)
    public CommandResult partyMemberName(
            @NotNull CommandContext context,
            @CommandArgument() String partyIndex,
            @CommandArgument() String memberIndex) {
        int memberIndexInt = Integer.parseInt(memberIndex);
        return executePartyList(
                Integer.parseInt(partyIndex),
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getName());
    }

    @NotNull
    @CommandHandler(value = "parseMenuPlaceholder {playerUid} {placeholder}", open = true)
    public CommandResult parseMenuPlaceholder(
            @NotNull CommandContext context,
            @CommandArgument() String playerUid,
            @CommandArgument() String placeholder) {
        try {
            UUID playerUUID = UUID.fromString(playerUid);
            return CommandResult.success(PartyMenuAPI.parse(
                    placeholder, MenuPlaceholderContext.create(Bukkit.getOfflinePlayer(playerUUID))));
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure(NULL);
        }
    }

    @NotNull
    private CommandResult executeMyParty(@NotNull CommandContext context, Function<Party, Object> function) {
        return getPlayer(context.getSender())
                .flatMap(player -> service.getParty(player.getUniqueId()))
                .map(party -> {
                    try {
                        Object result = function.call(party);
                        return CommandResult.success(
                                result instanceof String ? (String) result : String.valueOf(result));
                    } catch (Throwable e) {
                        Log.error(e);
                        return CommandResult.failure(NULL);
                    }
                })
                .orElse(CommandResult.success(NULL));
    }

    @NotNull
    private CommandResult executePartyList(int index, Function<Party, Object> function) {
        try {
            List<Party> partyList = service.getPartyList();
            if (index < 0 || index >= partyList.size()) {
                return CommandResult.success("索引不符合总队伍数量");
            }
            Object result = function.call(partyList.get(index));
            return CommandResult.success(result instanceof String ? (String) result : String.valueOf(result));
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure(NULL);
        }
    }

    @Autowired
    public void setService(PartyService service) {
        this.service = service;
    }
}

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

package top.nustar.nustarparty.core.packet;

import java.util.*;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import team.idealstate.sugar.logging.Log;
import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import team.idealstate.sugar.next.function.closure.Function;
import top.nustar.nustarcorebridge.api.packet.PacketProcessor;
import top.nustar.nustarcorebridge.api.packet.annotations.PacketArgument;
import top.nustar.nustarcorebridge.api.packet.annotations.PacketHandler;
import top.nustar.nustarcorebridge.api.packet.annotations.PacketName;
import top.nustar.nustarcorebridge.api.packet.context.PacketContext;
import top.nustar.nustarcorebridge.api.service.PlaceholderService;
import top.nustar.nustarcorebridge.converter.UidConverter;
import top.nustar.nustarparty.api.PartyMenuAPI;
import top.nustar.nustarparty.api.entity.Invite;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.placeholder.MenuPlaceholderContext;
import top.nustar.nustarparty.api.service.PartyService;
import top.nustar.nustarparty.core.utils.StringUtil;

/**
 * @author NuStar
 * @since 2025/7/23 01:04
 */
@Component
@PacketName("NuStarPartyPlaceholder")
@DependsOn(
        classes = "top.nustar.nustarcorebridge.NuStarCoreBridge",
        properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
@SuppressWarnings("unused")
public class PartyPlaceholderPacket implements PacketProcessor {
    private volatile PartyService partyService;

    private volatile PlaceholderService placeholderService;

    public static final String RECEIVE_JOIN_APPLICATION = "NuStarPartyPlaceholder_ReceiveJoinApplication";

    @Autowired
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Autowired
    public void setPlaceholderService(PlaceholderService placeholderService) {
        this.placeholderService = placeholderService;
    }

    @PacketHandler(value = "refreshPlayerListPlaceholder", description = "刷新玩家列表变量")
    public void refreshPlayerListPlaceholder(PacketContext<Player> packetContext) {
        getPlayerListSize(packetContext);
        List<Player> collect = partyService.getNonPartyPlayers().stream()
                .filter(player ->
                        !player.getUniqueId().equals(packetContext.getPacketSender().getUid()))
                .collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {
            getPlayerListName(packetContext, String.valueOf(i));
            getPlayerListUid(packetContext, String.valueOf(i));
        }
    }

    @PacketHandler(value = "refreshJoinApplicationPlaceholder", description = "刷新加入申请变量")
    public void refreshJoinApplicationPlaceholder(PacketContext<Player> packetContext) {
        Optional<Party> partyOptional = partyService.getParty(packetContext.getPacketSender().getUid());
        partyOptional.ifPresent(party -> {
            getMyPartyJoinApplicationSize(packetContext);
            for (int i = 0; i < party.getJoinApplicationList().size(); i++) {
                getMyPartyJoinApplicantName(packetContext, String.valueOf(i));
                getMyPartyJoinApplicantUid(packetContext, String.valueOf(i));
            }
        });
    }

    @PacketHandler(value = "refreshInviteApplicationPlaceholder", description = "刷新邀请申请变量")
    public void refreshInviteApplicationPlaceholder(PacketContext<Player> packetContext) {
        getPartyInviteApplicationSize(packetContext);
        Optional<Invite> inviteOptional =
                partyService.getPlayerInvite(packetContext.getPacketSender().getUid());
        inviteOptional.ifPresent(invite -> {
            for (int i = 0; i < invite.getInviteApplications().size(); i++) {
                getPartyInviteReason(packetContext, String.valueOf(i));
                getPartyInviteApplicantName(packetContext, String.valueOf(i));
                getPartyInviteApplicantUid(packetContext, String.valueOf(i));
            }
        });
    }

    @PacketHandler(value = "refreshMyPartyPlaceholder", description = "刷新我的队伍变量")
    public void refreshMyPartyPlaceholder(PacketContext<Player> packetContext) {
        getMyPartyEmptyPositions(packetContext);
        getMyPartyIsFull(packetContext);
        getMyPartyIsLeader(packetContext);
        getMyPartyLeaderUid(packetContext);
        getMyPartyLeaderName(packetContext);
        getMyPartyMaxSize(packetContext);
        getMyPartySize(packetContext);
        getMyPartyName(packetContext);
    }

    @PacketHandler(value = "refreshMyPartyMemberPlaceholder", description = "刷新我的队伍所有成员的变量")
    public void refreshMyPartyMemberPlaceholder(PacketContext<Player> packetContext) {
        Optional<Party> partyOptional = partyService.getParty(packetContext.getPacketSender().getUid());
        partyOptional.ifPresent(party -> {
            for (int i = 0; i < party.getSize(); i++) {
                getMyPartyMemberUid(packetContext, String.valueOf(i));
                getMyPartyMemberName(packetContext, String.valueOf(i));
            }
        });
    }

    @PacketHandler(value = "refreshPartyMemberPlaceholder", description = "刷新指定索引队伍的所有成员变量")
    public void refreshPartyMemberPlaceholder(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        List<Party> partyList = partyService.getPartyList();
        if (index >= partyList.size()) return;
        int partyMemberSize = partyList.get(index).getSize();
        for (int j = 0; j < partyMemberSize; j++) {
            getPartyMemberName(packetContext, String.valueOf(index), String.valueOf(j));
            getPartyMemberUid(packetContext, String.valueOf(index), String.valueOf(j));
        }
    }

    @PacketHandler(value = "refreshAllPlaceholder", description = "刷新所有队伍变量")
    public void refreshAllPartyPlaceholder(PacketContext<Player> packetContext) {
        getPartyListSize(packetContext);
        int partyListSize = partyService.getPartyList().size();
        for (int i = 0; i < partyListSize; i++) {
            String partyIndex = String.valueOf(i);
            getPartyName(packetContext, partyIndex);
            getPartyMaxSize(packetContext, partyIndex);
            getPartySize(packetContext, partyIndex);
            getPartyEmptyPositions(packetContext, partyIndex);
            getPartyUid(packetContext, partyIndex);
            getPartyLeaderName(packetContext, partyIndex);
            getPartyLeaderUid(packetContext, partyIndex);
            getPartyIsFull(packetContext, partyIndex);
        }
    }

    private void sendPlaceholderForPartyList(
            PacketContext<Player> packetContext, int index, String placeholder, Function<Party, String> function) {
        try {
            List<Party> partyList = partyService.getPartyList();
            String value = index < 0 || index >= partyList.size() ? "索引不符合总队伍数量" : function.call(partyList.get(index));
            placeholderService.sendPlaceholder(packetContext.getPacketSender().getSender(), placeholder, value);
        } catch (Throwable e) {
            Log.error(e);
            placeholderService.sendPlaceholder(packetContext.getPacketSender().getSender(), placeholder, "获取失败");
        }
    }

    private void sendPlaceholderForMyParty(
            PacketContext<Player> packetContext, String placeholder, Function<Party, String> function) {
        Optional<Party> partyOptional = partyService.getParty(packetContext.getPacketSender().getUid());
        if (!partyOptional.isPresent()) {
            placeholderService.sendPlaceholder(packetContext.getPacketSender().getSender(), placeholder, "无队伍");
            return;
        }
        partyOptional.ifPresent(party -> {
            try {
                placeholderService.sendPlaceholder(packetContext.getPacketSender().getSender(), placeholder, function.call(party));
            } catch (Throwable e) {
                Log.error(e);
                placeholderService.sendPlaceholder(packetContext.getPacketSender().getSender(), placeholder, "获取失败");
            }
        });
    }

    public void sendPlaceholderForInvite(
            PacketContext<Player> packetContext, String placeholder, Function<Invite, String> function) {
        Optional<Invite> inviteOptional =
                partyService.getPlayerInvite(packetContext.getPacketSender().getUid());
        if (!inviteOptional.isPresent()) {
            placeholderService.sendPlaceholder(packetContext.getPacketSender().getSender(), placeholder, "无邀请");
            return;
        }
        inviteOptional.ifPresent(invite -> {
            try {
                placeholderService.sendPlaceholder(packetContext.getPacketSender().getSender(), placeholder, function.call(invite));
            } catch (Throwable e) {
                Log.error(e);
                placeholderService.sendPlaceholder(packetContext.getPacketSender().getSender(), placeholder, "获取失败");
            }
        });
    }

    @PacketHandler(value = "playerFuzzySearch", description = "模糊搜索玩家")
    public void playerFuzzySearch(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "playerName", description = "玩家名称") String playerName) {
        List<Player> collect = partyService.getNonPartyPlayers().stream()
                .filter(player ->
                        !player.getUniqueId().equals(packetContext.getPacketSender().getUid()))
                .filter(player -> player.getName().toLowerCase().contains(playerName.toLowerCase()))
                .collect(Collectors.toList());
        Map<String, String> map = new HashMap<>();
        map.put("NuStarParty_FuzzySearchSize", String.valueOf(collect.size()));
        for (int i = 0; i < collect.size(); i++) {
            map.put("NuStarParty_FuzzySearchName_" + i, collect.get(i).getName());
            map.put(
                    "NuStarParty_FuzzySearchUid_" + i,
                    collect.get(i).getUniqueId().toString());
        }
        placeholderService.sendPlaceholderMap(packetContext.getPacketSender().getSender(), map);
    }

    @PacketHandler(value = "removeAllPlaceholder", description = "移除所有队伍变量")
    public void removeAllPlaceholder(PacketContext<Player> packetContext) {
        placeholderService.removePlaceholder(packetContext.getPacketSender().getSender(), "NuStarParty", true);
    }

    @PacketHandler(value = "getPlayerListSize", description = "获取未加入队伍的玩家数量")
    public void getPlayerListSize(PacketContext<Player> packetContext) {
        List<Player> collect = partyService.getNonPartyPlayers().stream()
                .filter(player ->
                        !player.getUniqueId().equals(packetContext.getPacketSender().getUid()))
                .collect(Collectors.toList());
        placeholderService.sendPlaceholder(
                packetContext.getPacketSender().getSender(), "NuStarParty_PlayerListSize", String.valueOf(collect.size()));
    }

    @PacketHandler(value = "getPlayerListUid", description = "获取未加入队伍的某个索引的玩家 UUID")
    public void getPlayerListUid(
            PacketContext<Player> packetContext, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        List<Player> collect = partyService.getNonPartyPlayers().stream()
                .filter(player ->
                        !player.getUniqueId().equals(packetContext.getPacketSender().getUid()))
                .collect(Collectors.toList());
        placeholderService.sendPlaceholder(
                packetContext.getPacketSender().getSender(),
                String.format("NuStarParty_PlayerListUid_%s", index),
                indexInt < 0 || indexInt >= collect.size()
                        ? "索引不符合未加入队伍玩家数量"
                        : collect.get(indexInt).getUniqueId().toString());
    }

    @PacketHandler(value = "getPlayerListName", description = "获取未加入队伍的某个索引的玩家名称")
    public void getPlayerListName(
            PacketContext<Player> packetContext, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        List<Player> collect = partyService.getNonPartyPlayers().stream()
                .filter(player ->
                        !player.getUniqueId().equals(packetContext.getPacketSender().getUid()))
                .collect(Collectors.toList());
        placeholderService.sendPlaceholder(
                packetContext.getPacketSender().getSender(),
                String.format("NuStarParty_PlayerListName_%s", index),
                indexInt < 0 || indexInt >= collect.size()
                        ? "索引不符合未加入队伍玩家数量"
                        : collect.get(indexInt).getName());
    }

    @PacketHandler(value = "getMyPartyJoinApplicationSize", description = "获取我的队伍的申请请求数量")
    public void getMyPartyJoinApplicationSize(PacketContext<Player> packetContext) {
        sendPlaceholderForMyParty(
                packetContext,
                "NuStarParty_MyPartyJoinApplicationSize",
                party -> String.valueOf(party.getJoinApplicationList().size()));
    }

    @PacketHandler(value = "getPartyInviteApplicationSize", description = "获取队伍的邀请请求数量")
    public void getPartyInviteApplicationSize(PacketContext<Player> packetContext) {
        sendPlaceholderForInvite(
                packetContext,
                "NuStarParty_PartyInviteApplicationSize",
                invite -> String.valueOf(invite.getInviteApplications().size()));
    }

    @PacketHandler(value = "getPartyInviteApplicantUid", description = "获取某个索引的队伍邀请请求者的 UUID")
    public void getPartyInviteApplicantUid(
            PacketContext<Player> packetContext, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        sendPlaceholderForInvite(
                packetContext,
                String.format("NuStarParty_PartyInviteApplicantUid_%s", index),
                invite -> indexInt < 0
                                || indexInt >= invite.getInviteApplications().size()
                        ? "索引大于申请数量"
                        : invite.getInviteApplications()
                                .get(indexInt)
                                .getInviter()
                                .getUniqueId()
                                .toString());
    }

    @PacketHandler(value = "getPartyInviteApplicantName", description = "获取某个索引的队伍邀请请求者的名称")
    public void getPartyInviteApplicantName(
            PacketContext<Player> packetContext, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        sendPlaceholderForInvite(
                packetContext,
                String.format("NuStarParty_PartyInviteApplicantName_%s", index),
                invite -> indexInt < 0
                                || indexInt >= invite.getInviteApplications().size()
                        ? "索引大于申请数量"
                        : invite.getInviteApplications()
                                .get(indexInt)
                                .getInviter()
                                .getName());
    }

    @PacketHandler(value = "getPartyInviteReason", description = "获取某个索引的队伍邀请请求者的邀请理由")
    public void getPartyInviteReason(
            PacketContext<Player> packetContext, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        sendPlaceholderForInvite(
                packetContext,
                String.format("NuStarParty_PartyInviteReason_%s", index),
                invite -> indexInt < 0
                                || indexInt >= invite.getInviteApplications().size()
                        ? "索引大于申请数量"
                        : String.join(
                                "\n",
                                StringUtil.wrapText(
                                        invite.getInviteApplications()
                                                .get(indexInt)
                                                .getInviteReason(),
                                        12)));
    }

    @PacketHandler(value = "getMyPartyJoinApplicantUid", description = "获取我的队伍某个索引的加入请求者的 UUID")
    public void getMyPartyJoinApplicantUid(
            PacketContext<Player> packetContext, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        sendPlaceholderForMyParty(
                packetContext,
                String.format("NuStarParty_MyPartyJoinApplicantUid_%s", index),
                party -> indexInt < 0
                                || indexInt >= party.getJoinApplicationList().size()
                        ? "索引大于申请数量"
                        : party.getJoinApplicationList()
                                .get(indexInt)
                                .getUniqueId()
                                .toString());
    }

    @PacketHandler(value = "getMyPartyJoinApplicantName", description = "获取我的队伍某个索引的加入请求者的名称")
    public void getMyPartyJoinApplicantName(
            PacketContext<Player> packetContext, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        sendPlaceholderForMyParty(
                packetContext,
                String.format("NuStarParty_MyPartyJoinApplicantName_%s", index),
                party -> indexInt < 0
                                || indexInt >= party.getJoinApplicationList().size()
                        ? "索引大于申请数量"
                        : party.getJoinApplicationList().get(indexInt).getName());
    }

    @PacketHandler(value = "getMyPartyLeaderUid", description = "获取我的队伍的队长 UUID")
    public void getMyPartyLeaderUid(PacketContext<Player> packetContext) {
        sendPlaceholderForMyParty(packetContext, "NuStarParty_MyPartyLeaderUid", party -> party.getLeaderUid()
                .toString());
    }

    @PacketHandler(value = "getMyPartyLeaderName", description = "获取我的队伍的队长名称")
    public void getMyPartyLeaderName(PacketContext<Player> packetContext) {
        sendPlaceholderForMyParty(packetContext, "NuStarParty_MyPartyLeaderName", Party::getLeaderName);
    }

    @PacketHandler(value = "getMyPartySize", description = "获得我的队伍的队伍当前人数")
    public void getMyPartySize(PacketContext<Player> packetContext) {
        sendPlaceholderForMyParty(packetContext, "NuStarParty_MyPartySize", party -> String.valueOf(party.getSize()));
    }

    @PacketHandler(value = "getMyPartyEmptyPositions", description = "获得我的队伍的队伍剩余可加入人数")
    public void getMyPartyEmptyPositions(PacketContext<Player> packetContext) {
        sendPlaceholderForMyParty(
                packetContext, "NuStarParty_MyPartyEmptyPositions", party -> String.valueOf(party.getEmptyPositions()));
    }

    @PacketHandler(value = "getMyPartyMaxSize", description = "获得我的队伍的队伍最大人数")
    public void getMyPartyMaxSize(PacketContext<Player> packetContext) {
        sendPlaceholderForMyParty(packetContext, "NuStarParty_MyPartyMaxSize", party -> String.valueOf(party.getMaxSize()));
    }

    @PacketHandler(value = "getMyPartyIsFull", description = "获得我的队伍是否已满")
    public void getMyPartyIsFull(PacketContext<Player> packetContext) {
        sendPlaceholderForMyParty(packetContext, "NuStarParty_MyPartyIsFull", party -> String.valueOf(party.isFull()));
    }

    @PacketHandler(value = "getMyPartyIsMember", description = "检查我的队伍是否包含这个名字的玩家")
    public void getMyPartyIsMember(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "memberName", description = "要检查的玩家名称") String memberName) {
        sendPlaceholderForMyParty(
                packetContext, "NuStarParty_MyPartyIsMember", party -> String.valueOf(party.isMember(memberName)));
    }

    @PacketHandler(value = "getMyPartyIsLeader", description = "获取当前玩家是否是队伍的队长")
    public void getMyPartyIsLeader(PacketContext<Player> packetContext) {
        sendPlaceholderForMyParty(
                packetContext,
                "NuStarParty_MyPartyIsLeader",
                party -> String.valueOf(
                        party.getLeaderUid().equals(packetContext.getPacketSender().getUid())));
    }

    @PacketHandler(value = "getMyPartyMemberUid", description = "获取我的队伍某个索引的成员 UUID")
    public void getMyPartyMemberUid(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "memberIndex", description = "成员索引") String memberIndex) {
        int memberIndexInt = Integer.parseInt(memberIndex);
        sendPlaceholderForMyParty(
                packetContext,
                String.format("NuStarParty_MyPartyMemberUid_%s", memberIndex),
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getUniqueId().toString());
    }

    @PacketHandler(value = "getMyPartyMemberName", description = "获取我的队伍某个索引的成员 UUID")
    public void getMyPartyMemberName(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "memberIndex", description = "成员索引") String memberIndex) {
        int memberIndexInt = Integer.parseInt(memberIndex);
        sendPlaceholderForMyParty(
                packetContext,
                String.format("NuStarParty_MyPartyMemberName_%s", memberIndex),
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getName());
    }

    @PacketHandler(value = "getMyPartyName", description = "获取我的队伍名称")
    public void getMyPartyName(PacketContext<Player> packetContext) {
        sendPlaceholderForMyParty(packetContext, "NuStarParty_MyPartyName", Party::getPartyName);
    }

    @PacketHandler(value = "getPartyListSize", description = "获取队伍总数量")
    public void getPartyListSize(PacketContext<Player> packetContext) {
        placeholderService.sendPlaceholder(
                packetContext.getPacketSender().getSender(),
                "NuStarParty_PartyListSize",
                String.valueOf(partyService.getPartyList().size()));
    }

    @PacketHandler(value = "getPartyName", description = "获取某个索引的队伍名称")
    public void getPartyName(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                packetContext, index, String.format("NuStarParty_PartyName_%s", partyIndex), Party::getPartyName);
    }

    @PacketHandler(value = "getPartyMaxSize", description = "获取某个索引的最大队伍人数")
    public void getPartyMaxSize(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        sendPlaceholderForPartyList(
                packetContext,
                Integer.parseInt(partyIndex),
                String.format("NuStarParty_PartyMaxSize_%s", partyIndex),
                party -> String.valueOf(party.getMaxSize()));
    }

    @PacketHandler(value = "getPartySize", description = "获取某个索引的队伍人数(不包含队长)")
    public void getPartySize(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                packetContext,
                index,
                String.format("NuStarParty_PartySize_%s", partyIndex),
                party -> String.valueOf(party.getSize()));
    }

    @PacketHandler(value = "getPartyEmptyPositions", description = "获取某个索引的队伍剩余可加入人数")
    public void getPartyEmptyPositions(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                packetContext,
                index,
                String.format("NuStarParty_PartyEmptyPositions_%s", partyIndex),
                party -> String.valueOf(party.getEmptyPositions()));
    }

    @PacketHandler(value = "getPartyUid", description = "获取某个索引的队伍 UUID")
    public void getPartyUid(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                packetContext, index, String.format("NuStarParty_PartyUid_%s", partyIndex), party -> party.getPartyUUID()
                        .toString());
    }

    @PacketHandler(value = "getPartyLeaderName", description = "获取某个索引的队伍的队长名称")
    public void getPartyLeaderName(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                packetContext, index, String.format("NuStarParty_PartyLeaderName_%s", partyIndex), party -> party.getLeader()
                        .getName());
    }

    @PacketHandler(value = "getPartyIsFull", description = "获取某个索引的队伍是否已满")
    public void getPartyIsFull(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                packetContext,
                index,
                String.format("NuStarParty_PartyIsFull_%s", partyIndex),
                party -> String.valueOf(party.isFull()));
    }

    @PacketHandler(value = "getPartyLeaderUid", description = "获取某个索引的队伍的队长 UUID")
    public void getPartyLeaderUid(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                packetContext, index, String.format("NuStarParty_PartyLeaderUid_%s", partyIndex), party -> party.getLeader()
                        .getUniqueId()
                        .toString());
    }

    @PacketHandler(value = "getPartyMemberName", description = "获取某个索引的队伍的某个索引的成员名称")
    public void getPartyMemberName(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex,
            @PacketArgument(value = "memberIndex", description = "成员索引") String memberIndex) {
        int index = Integer.parseInt(partyIndex);
        int memberIndexInt = Integer.parseInt(memberIndex);
        sendPlaceholderForPartyList(
                packetContext,
                index,
                String.format("NuStarParty_PartyMemberName_%s_%s", partyIndex, memberIndex),
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getName());
    }

    @PacketHandler(value = "getPartyMemberUid", description = "获取某个索引的队伍的某个索引的成员 UUID")
    public void getPartyMemberUid(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex,
            @PacketArgument(value = "memberIndex", description = "成员索引") String memberIndex) {
        int index = Integer.parseInt(partyIndex);
        int memberIndexInt = Integer.parseInt(memberIndex);
        sendPlaceholderForPartyList(
                packetContext,
                index,
                String.format("NuStarParty_PartyMemberUid_%s_%s", partyIndex, memberIndex),
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getUniqueId().toString());
    }

    @PacketHandler(value = "parseMenuPlaceholder", description = "使目标玩家解析一个菜单占位符")
    public void parsePlaceholder(
            PacketContext<Player> packetContext,
            @PacketArgument(value = "playerUid", description = "目标玩家 UUID", converter = UidConverter.class)
                    UUID playerUid,
            @PacketArgument(value = "menuPlaceholder", description = "占位符") String menuPlaceholder) {
        String parse =
                PartyMenuAPI.parse(menuPlaceholder, MenuPlaceholderContext.create(Bukkit.getOfflinePlayer(playerUid)));
        String placeholder =
                String.format("NuStarParty_ParseMenuPlaceholder_%s_%s", playerUid.toString(), menuPlaceholder);
        placeholderService.sendPlaceholder(packetContext.getPacketSender().getSender(), menuPlaceholder, parse);
    }
}

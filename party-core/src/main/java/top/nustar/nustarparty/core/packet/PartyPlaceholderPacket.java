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
import top.nustar.nustarcorebridge.api.packet.sender.PacketSender;
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
    public void refreshPlayerListPlaceholder(PacketSender<Player> sender) {
        getPlayerListSize(sender);
        List<Player> collect = partyService.getNonPartyPlayers().stream()
                .filter(player ->
                        !player.getUniqueId().equals(sender.getSender().getUniqueId()))
                .collect(Collectors.toList());
        for (int i = 0; i < collect.size(); i++) {
            getPlayerListName(sender, String.valueOf(i));
            getPlayerListUid(sender, String.valueOf(i));
        }
    }

    @PacketHandler(value = "refreshJoinApplicationPlaceholder", description = "刷新加入申请变量")
    public void refreshJoinApplicationPlaceholder(PacketSender<Player> sender) {
        Optional<Party> partyOptional = partyService.getParty(sender.getSender().getUniqueId());
        partyOptional.ifPresent(party -> {
            getMyPartyJoinApplicationSize(sender);
            for (int i = 0; i < party.getJoinApplicationList().size(); i++) {
                getMyPartyJoinApplicantName(sender, String.valueOf(i));
                getMyPartyJoinApplicantUid(sender, String.valueOf(i));
            }
        });
    }

    @PacketHandler(value = "refreshInviteApplicationPlaceholder", description = "刷新邀请申请变量")
    public void refreshInviteApplicationPlaceholder(PacketSender<Player> sender) {
        getPartyInviteApplicationSize(sender);
        Optional<Invite> inviteOptional =
                partyService.getPlayerInvite(sender.getSender().getUniqueId());
        inviteOptional.ifPresent(invite -> {
            for (int i = 0; i < invite.getInviteApplications().size(); i++) {
                getPartyInviteReason(sender, String.valueOf(i));
                getPartyInviteApplicantName(sender, String.valueOf(i));
                getPartyInviteApplicantUid(sender, String.valueOf(i));
            }
        });
    }

    @PacketHandler(value = "refreshMyPartyPlaceholder", description = "刷新我的队伍变量")
    public void refreshMyPartyPlaceholder(PacketSender<Player> sender) {
        getMyPartyEmptyPositions(sender);
        getMyPartyIsFull(sender);
        getMyPartyIsLeader(sender);
        getMyPartyLeaderUid(sender);
        getMyPartyLeaderName(sender);
        getMyPartyMaxSize(sender);
        getMyPartySize(sender);
        getMyPartyName(sender);
    }

    @PacketHandler(value = "refreshMyPartyMemberPlaceholder", description = "刷新我的队伍所有成员的变量")
    public void refreshMyPartyMemberPlaceholder(PacketSender<Player> sender) {
        Optional<Party> partyOptional = partyService.getParty(sender.getSender().getUniqueId());
        partyOptional.ifPresent(party -> {
            for (int i = 0; i < party.getSize(); i++) {
                getMyPartyMemberUid(sender, String.valueOf(i));
                getMyPartyMemberName(sender, String.valueOf(i));
            }
        });
    }

    @PacketHandler(value = "refreshPartyMemberPlaceholder", description = "刷新指定索引队伍的所有成员变量")
    public void refreshPartyMemberPlaceholder(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        List<Party> partyList = partyService.getPartyList();
        if (index >= partyList.size()) return;
        int partyMemberSize = partyList.get(index).getSize();
        for (int j = 0; j < partyMemberSize; j++) {
            getPartyMemberName(sender, String.valueOf(index), String.valueOf(j));
            getPartyMemberUid(sender, String.valueOf(index), String.valueOf(j));
        }
    }

    @PacketHandler(value = "refreshAllPlaceholder", description = "刷新所有队伍变量")
    public void refreshAllPartyPlaceholder(PacketSender<Player> sender) {
        getPartyListSize(sender);
        int partyListSize = partyService.getPartyList().size();
        for (int i = 0; i < partyListSize; i++) {
            String partyIndex = String.valueOf(i);
            getPartyName(sender, partyIndex);
            getPartyMaxSize(sender, partyIndex);
            getPartySize(sender, partyIndex);
            getPartyEmptyPositions(sender, partyIndex);
            getPartyUid(sender, partyIndex);
            getPartyLeaderName(sender, partyIndex);
            getPartyLeaderUid(sender, partyIndex);
            getPartyIsFull(sender, partyIndex);
        }
    }

    private void sendPlaceholderForPartyList(
            PacketSender<Player> sender, int index, String placeholder, Function<Party, String> function) {
        try {
            List<Party> partyList = partyService.getPartyList();
            String value = index < 0 || index >= partyList.size() ? "索引不符合总队伍数量" : function.call(partyList.get(index));
            placeholderService.sendPlaceholder(sender.getSender(), placeholder, value);
        } catch (Throwable e) {
            Log.error(e);
            placeholderService.sendPlaceholder(sender.getSender(), placeholder, "获取失败");
        }
    }

    private void sendPlaceholderForMyParty(
            PacketSender<Player> sender, String placeholder, Function<Party, String> function) {
        Optional<Party> partyOptional = partyService.getParty(sender.getSender().getUniqueId());
        if (!partyOptional.isPresent()) {
            placeholderService.sendPlaceholder(sender.getSender(), placeholder, "无队伍");
            return;
        }
        partyOptional.ifPresent(party -> {
            try {
                placeholderService.sendPlaceholder(sender.getSender(), placeholder, function.call(party));
            } catch (Throwable e) {
                Log.error(e);
                placeholderService.sendPlaceholder(sender.getSender(), placeholder, "获取失败");
            }
        });
    }

    public void sendPlaceholderForInvite(
            PacketSender<Player> sender, String placeholder, Function<Invite, String> function) {
        Optional<Invite> inviteOptional =
                partyService.getPlayerInvite(sender.getSender().getUniqueId());
        if (!inviteOptional.isPresent()) {
            placeholderService.sendPlaceholder(sender.getSender(), placeholder, "无邀请");
            return;
        }
        inviteOptional.ifPresent(invite -> {
            try {
                placeholderService.sendPlaceholder(sender.getSender(), placeholder, function.call(invite));
            } catch (Throwable e) {
                Log.error(e);
                placeholderService.sendPlaceholder(sender.getSender(), placeholder, "获取失败");
            }
        });
    }

    @PacketHandler(value = "playerFuzzySearch", description = "模糊搜索玩家")
    public void playerFuzzySearch(
            PacketSender<Player> sender,
            @PacketArgument(value = "playerName", description = "玩家名称") String playerName) {
        List<Player> collect = partyService.getNonPartyPlayers().stream()
                .filter(player ->
                        !player.getUniqueId().equals(sender.getSender().getUniqueId()))
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
        placeholderService.sendPlaceholderMap(sender.getSender(), map);
    }

    @PacketHandler(value = "removeAllPlaceholder", description = "移除所有队伍变量")
    public void removeAllPlaceholder(PacketSender<Player> sender) {
        placeholderService.removePlaceholder(sender.getSender(), "NuStarParty", true);
    }

    @PacketHandler(value = "getPlayerListSize", description = "获取未加入队伍的玩家数量")
    public void getPlayerListSize(PacketSender<Player> sender) {
        List<Player> collect = partyService.getNonPartyPlayers().stream()
                .filter(player ->
                        !player.getUniqueId().equals(sender.getSender().getUniqueId()))
                .collect(Collectors.toList());
        placeholderService.sendPlaceholder(
                sender.getSender(), "NuStarParty_PlayerListSize", String.valueOf(collect.size()));
    }

    @PacketHandler(value = "getPlayerListUid", description = "获取未加入队伍的某个索引的玩家 UUID")
    public void getPlayerListUid(
            PacketSender<Player> sender, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        List<Player> collect = partyService.getNonPartyPlayers().stream()
                .filter(player ->
                        !player.getUniqueId().equals(sender.getSender().getUniqueId()))
                .collect(Collectors.toList());
        placeholderService.sendPlaceholder(
                sender.getSender(),
                String.format("NuStarParty_PlayerListUid_%s", index),
                indexInt < 0 || indexInt >= collect.size()
                        ? "索引不符合未加入队伍玩家数量"
                        : collect.get(indexInt).getUniqueId().toString());
    }

    @PacketHandler(value = "getPlayerListName", description = "获取未加入队伍的某个索引的玩家名称")
    public void getPlayerListName(
            PacketSender<Player> sender, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        List<Player> collect = partyService.getNonPartyPlayers().stream()
                .filter(player ->
                        !player.getUniqueId().equals(sender.getSender().getUniqueId()))
                .collect(Collectors.toList());
        placeholderService.sendPlaceholder(
                sender.getSender(),
                String.format("NuStarParty_PlayerListName_%s", index),
                indexInt < 0 || indexInt >= collect.size()
                        ? "索引不符合未加入队伍玩家数量"
                        : collect.get(indexInt).getName());
    }

    @PacketHandler(value = "getMyPartyJoinApplicationSize", description = "获取我的队伍的申请请求数量")
    public void getMyPartyJoinApplicationSize(PacketSender<Player> sender) {
        sendPlaceholderForMyParty(
                sender,
                "NuStarParty_MyPartyJoinApplicationSize",
                party -> String.valueOf(party.getJoinApplicationList().size()));
    }

    @PacketHandler(value = "getPartyInviteApplicationSize", description = "获取队伍的邀请请求数量")
    public void getPartyInviteApplicationSize(PacketSender<Player> sender) {
        sendPlaceholderForInvite(
                sender,
                "NuStarParty_PartyInviteApplicationSize",
                invite -> String.valueOf(invite.getInviteApplications().size()));
    }

    @PacketHandler(value = "getPartyInviteApplicantUid", description = "获取某个索引的队伍邀请请求者的 UUID")
    public void getPartyInviteApplicantUid(
            PacketSender<Player> sender, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        sendPlaceholderForInvite(
                sender,
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
            PacketSender<Player> sender, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        sendPlaceholderForInvite(
                sender,
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
            PacketSender<Player> sender, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        sendPlaceholderForInvite(
                sender,
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
            PacketSender<Player> sender, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        sendPlaceholderForMyParty(
                sender,
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
            PacketSender<Player> sender, @PacketArgument(value = "index", description = "索引") String index) {
        int indexInt = Integer.parseInt(index);
        sendPlaceholderForMyParty(
                sender,
                String.format("NuStarParty_MyPartyJoinApplicantName_%s", index),
                party -> indexInt < 0
                                || indexInt >= party.getJoinApplicationList().size()
                        ? "索引大于申请数量"
                        : party.getJoinApplicationList().get(indexInt).getName());
    }

    @PacketHandler(value = "getMyPartyLeaderUid", description = "获取我的队伍的队长 UUID")
    public void getMyPartyLeaderUid(PacketSender<Player> sender) {
        sendPlaceholderForMyParty(sender, "NuStarParty_MyPartyLeaderUid", party -> party.getLeaderUid()
                .toString());
    }

    @PacketHandler(value = "getMyPartyLeaderName", description = "获取我的队伍的队长名称")
    public void getMyPartyLeaderName(PacketSender<Player> sender) {
        sendPlaceholderForMyParty(sender, "NuStarParty_MyPartyLeaderName", Party::getLeaderName);
    }

    @PacketHandler(value = "getMyPartySize", description = "获得我的队伍的队伍当前人数")
    public void getMyPartySize(PacketSender<Player> sender) {
        sendPlaceholderForMyParty(sender, "NuStarParty_MyPartySize", party -> String.valueOf(party.getSize()));
    }

    @PacketHandler(value = "getMyPartyEmptyPositions", description = "获得我的队伍的队伍剩余可加入人数")
    public void getMyPartyEmptyPositions(PacketSender<Player> sender) {
        sendPlaceholderForMyParty(
                sender, "NuStarParty_MyPartyEmptyPositions", party -> String.valueOf(party.getEmptyPositions()));
    }

    @PacketHandler(value = "getMyPartyMaxSize", description = "获得我的队伍的队伍最大人数")
    public void getMyPartyMaxSize(PacketSender<Player> sender) {
        sendPlaceholderForMyParty(sender, "NuStarParty_MyPartyMaxSize", party -> String.valueOf(party.getMaxSize()));
    }

    @PacketHandler(value = "getMyPartyIsFull", description = "获得我的队伍是否已满")
    public void getMyPartyIsFull(PacketSender<Player> sender) {
        sendPlaceholderForMyParty(sender, "NuStarParty_MyPartyIsFull", party -> String.valueOf(party.isFull()));
    }

    @PacketHandler(value = "getMyPartyIsMember", description = "检查我的队伍是否包含这个名字的玩家")
    public void getMyPartyIsMember(
            PacketSender<Player> sender,
            @PacketArgument(value = "memberName", description = "要检查的玩家名称") String memberName) {
        sendPlaceholderForMyParty(
                sender, "NuStarParty_MyPartyIsMember", party -> String.valueOf(party.isMember(memberName)));
    }

    @PacketHandler(value = "getMyPartyIsLeader", description = "获取当前玩家是否是队伍的队长")
    public void getMyPartyIsLeader(PacketSender<Player> sender) {
        sendPlaceholderForMyParty(
                sender,
                "NuStarParty_MyPartyIsLeader",
                party -> String.valueOf(
                        party.getLeaderUid().equals(sender.getSender().getUniqueId())));
    }

    @PacketHandler(value = "getMyPartyMemberUid", description = "获取我的队伍某个索引的成员 UUID")
    public void getMyPartyMemberUid(
            PacketSender<Player> sender,
            @PacketArgument(value = "memberIndex", description = "成员索引") String memberIndex) {
        int memberIndexInt = Integer.parseInt(memberIndex);
        sendPlaceholderForMyParty(
                sender,
                String.format("NuStarParty_MyPartyMemberUid_%s", memberIndex),
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getUniqueId().toString());
    }

    @PacketHandler(value = "getMyPartyMemberName", description = "获取我的队伍某个索引的成员 UUID")
    public void getMyPartyMemberName(
            PacketSender<Player> sender,
            @PacketArgument(value = "memberIndex", description = "成员索引") String memberIndex) {
        int memberIndexInt = Integer.parseInt(memberIndex);
        sendPlaceholderForMyParty(
                sender,
                String.format("NuStarParty_MyPartyMemberName_%s", memberIndex),
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getName());
    }

    @PacketHandler(value = "getMyPartyName", description = "获取我的队伍名称")
    public void getMyPartyName(PacketSender<Player> sender) {
        sendPlaceholderForMyParty(sender, "NuStarParty_MyPartyName", Party::getPartyName);
    }

    @PacketHandler(value = "getPartyListSize", description = "获取队伍总数量")
    public void getPartyListSize(PacketSender<Player> sender) {
        placeholderService.sendPlaceholder(
                sender.getSender(),
                "NuStarParty_PartyListSize",
                String.valueOf(partyService.getPartyList().size()));
    }

    @PacketHandler(value = "getPartyName", description = "获取某个索引的队伍名称")
    public void getPartyName(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                sender, index, String.format("NuStarParty_PartyName_%s", partyIndex), Party::getPartyName);
    }

    @PacketHandler(value = "getPartyMaxSize", description = "获取某个索引的最大队伍人数")
    public void getPartyMaxSize(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        sendPlaceholderForPartyList(
                sender,
                Integer.parseInt(partyIndex),
                String.format("NuStarParty_PartyMaxSize_%s", partyIndex),
                party -> String.valueOf(party.getMaxSize()));
    }

    @PacketHandler(value = "getPartySize", description = "获取某个索引的队伍人数(不包含队长)")
    public void getPartySize(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                sender,
                index,
                String.format("NuStarParty_PartySize_%s", partyIndex),
                party -> String.valueOf(party.getSize()));
    }

    @PacketHandler(value = "getPartyEmptyPositions", description = "获取某个索引的队伍剩余可加入人数")
    public void getPartyEmptyPositions(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                sender,
                index,
                String.format("NuStarParty_PartyEmptyPositions_%s", partyIndex),
                party -> String.valueOf(party.getEmptyPositions()));
    }

    @PacketHandler(value = "getPartyUid", description = "获取某个索引的队伍 UUID")
    public void getPartyUid(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                sender, index, String.format("NuStarParty_PartyUid_%s", partyIndex), party -> party.getPartyUUID()
                        .toString());
    }

    @PacketHandler(value = "getPartyLeaderName", description = "获取某个索引的队伍的队长名称")
    public void getPartyLeaderName(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                sender, index, String.format("NuStarParty_PartyLeaderName_%s", partyIndex), party -> party.getLeader()
                        .getName());
    }

    @PacketHandler(value = "getPartyIsFull", description = "获取某个索引的队伍是否已满")
    public void getPartyIsFull(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                sender,
                index,
                String.format("NuStarParty_PartyIsFull_%s", partyIndex),
                party -> String.valueOf(party.isFull()));
    }

    @PacketHandler(value = "getPartyLeaderUid", description = "获取某个索引的队伍的队长 UUID")
    public void getPartyLeaderUid(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex) {
        int index = Integer.parseInt(partyIndex);
        sendPlaceholderForPartyList(
                sender, index, String.format("NuStarParty_PartyLeaderUid_%s", partyIndex), party -> party.getLeader()
                        .getUniqueId()
                        .toString());
    }

    @PacketHandler(value = "getPartyMemberName", description = "获取某个索引的队伍的某个索引的成员名称")
    public void getPartyMemberName(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex,
            @PacketArgument(value = "memberIndex", description = "成员索引") String memberIndex) {
        int index = Integer.parseInt(partyIndex);
        int memberIndexInt = Integer.parseInt(memberIndex);
        sendPlaceholderForPartyList(
                sender,
                index,
                String.format("NuStarParty_PartyMemberName_%s_%s", partyIndex, memberIndex),
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getName());
    }

    @PacketHandler(value = "getPartyMemberUid", description = "获取某个索引的队伍的某个索引的成员 UUID")
    public void getPartyMemberUid(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyIndex", description = "队伍索引") String partyIndex,
            @PacketArgument(value = "memberIndex", description = "成员索引") String memberIndex) {
        int index = Integer.parseInt(partyIndex);
        int memberIndexInt = Integer.parseInt(memberIndex);
        sendPlaceholderForPartyList(
                sender,
                index,
                String.format("NuStarParty_PartyMemberUid_%s_%s", partyIndex, memberIndex),
                party -> memberIndexInt < 0
                                || memberIndexInt >= party.getMembers().size()
                        ? "索引不符合成员数量"
                        : party.getMembers().get(memberIndexInt).getUniqueId().toString());
    }

    @PacketHandler(value = "parseMenuPlaceholder", description = "使目标玩家解析一个菜单占位符")
    public void parsePlaceholder(
            PacketSender<Player> sender,
            @PacketArgument(value = "playerUid", description = "目标玩家 UUID", converter = UidConverter.class)
                    UUID playerUid,
            @PacketArgument(value = "menuPlaceholder", description = "占位符") String menuPlaceholder) {
        String parse =
                PartyMenuAPI.parse(menuPlaceholder, MenuPlaceholderContext.create(Bukkit.getOfflinePlayer(playerUid)));
        String placeholder =
                String.format("NuStarParty_ParseMenuPlaceholder_%s_%s", playerUid.toString(), menuPlaceholder);
        placeholderService.sendPlaceholder(sender.getSender(), menuPlaceholder, parse);
    }
}

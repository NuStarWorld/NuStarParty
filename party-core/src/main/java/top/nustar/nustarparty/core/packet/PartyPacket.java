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

import java.util.UUID;
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
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.service.PartyService;

/**
 * @author NuStar
 * @since 2025/6/12 21:07
 */
@Component
@PacketName("NuStarParty")
@DependsOn(
        classes = "top.nustar.nustarcorebridge.NuStarCoreBridge",
        properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
@SuppressWarnings("unused")
public class PartyPacket implements PacketProcessor {

    private volatile PartyService partyService;

    private volatile PlaceholderService placeholderService;

    @Autowired
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Autowired
    public void setPlaceholderService(PlaceholderService placeholderService) {
        this.placeholderService = placeholderService;
    }

    private void executeParty(PacketSender<Player> sender, String placeholder, Function<Player, Boolean> function) {
        try {
            if (function.call(sender.getSender())) {
                placeholderService.sendPlaceholder(sender.getSender(), placeholder, "1");
            } else {
                placeholderService.sendPlaceholder(sender.getSender(), placeholder, "0");
            }
        } catch (Throwable e) {
            Log.error(e);
            placeholderService.sendPlaceholder(sender.getSender(), placeholder, "执行失败");
        }
    }

    @PacketHandler(value = "createParty", description = "创建队伍")
    public void createParty(PacketSender<Player> sender) {
        executeParty(sender, "NuStarParty_CreateParty", partyService::createParty);
    }

    @PacketHandler(value = "quitParty", description = "退出队伍")
    public void quitParty(PacketSender<Player> sender) {
        executeParty(sender, "NuStarParty_QuitParty", partyService::quitParty);
    }

    @PacketHandler(value = "disbandParty", description = "解散队伍")
    public void disbandParty(PacketSender<Player> sender) {
        executeParty(sender, "NuStarParty_DisbandParty", partyService::disbandParty);
    }

    @PacketHandler(value = "addJoinPartyRequest", description = "申请加入队伍")
    public void addJoinPartyRequest(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyUid", description = "队伍UUID", converter = UidConverter.class) UUID partyUid) {
        executeParty(
                sender,
                "NuStarParty_AddJoinPartyRequest",
                player -> partyService.addJoinPartyRequest(player, partyUid));
    }

    @PacketHandler(value = "acceptJoinRequest", description = "接受加入队伍请求")
    public void acceptJoinRequest(
            PacketSender<Player> sender,
            @PacketArgument(value = "requesterName", description = "通过申请者的加入请求(名称)") String requesterName) {
        executeParty(
                sender,
                "NuStarParty_AcceptJoinRequest",
                player -> partyService.acceptJoinApplication(player, requesterName));
    }

    @PacketHandler(value = "kickMember", description = "踢出队伍成员")
    public void kickMember(
            PacketSender<Player> sender,
            @PacketArgument(value = "memberName", description = "被踢的玩家名称") String memberName,
            @PacketArgument(value = "kickReason", description = "踢出原因") String kickReason) {
        executeParty(
                sender, "NuStarParty_KickMember", player -> partyService.kickMember(player, memberName, kickReason));
    }

    @PacketHandler(value = "isInSameParty", description = "检查两个玩家是否在同一个队伍中")
    public void isInSameParty(
            PacketSender<Player> sender,
            @PacketArgument(value = "playerUid1", description = "被检查的玩家1的 UUID", converter = UidConverter.class)
                    UUID playerUid1,
            @PacketArgument(value = "playerUid2", description = "被检查的玩家2的 UUID", converter = UidConverter.class)
                    UUID playerUid2) {
        executeParty(sender, "NuStarParty_IsInSameParty", player -> partyService.isInSameParty(playerUid1, playerUid2));
    }

    // TODO 待实现
    @PacketHandler(value = "invitePlayer", description = "邀请玩家加入队伍")
    public void invitePlayer(
            PacketSender<Player> sender,
            @PacketArgument(value = "playerName", description = "被邀请的玩家名称") String playerName) {
        sender.sendMessage("§a§l[!] §6NuStarParty §f- 玩家已邀请");
    }

    @PacketHandler(value = "transferLeader", description = "转让队长")
    public void transferLeader(
            PacketSender<Player> sender,
            @PacketArgument(value = "playerName", description = "被转让的玩家名称") String playerName) {
        sender.sendMessage("§a§l[!] §6NuStarParty §f- 玩家已转让");
    }

    @PacketHandler(value = "mergeParty", description = "合并队伍")
    public void mergeParty(
            PacketSender<Player> sender,
            @PacketArgument(value = "partyName", description = "要合并的队伍名称") String partyName) {
        sender.sendMessage("§a§l[!] §6NuStarParty §f- 队伍已合并");
    }

    @PacketHandler(value = "changePickupMode", description = "改变掉落物拾取模式")
    public void changePickupMode(
            PacketSender<Player> sender, @PacketArgument(value = "mode", description = "掉落物拾取模式") String mode) {
        sender.sendMessage("§a§l[!] §6NuStarParty §f- 掉落物拾取模式已改变");
    }

    @PacketHandler(value = "setPartyDestination", description = "设置队伍目的地")
    public void setPartyDestination(
            PacketSender<Player> sender,
            @PacketArgument(value = "destination", description = "目标") String destination,
            @PacketArgument(value = "notify", description = "是否通知组员") String type) {
        sender.sendMessage("§a§l[!] §6NuStarParty §f- 队伍目的地已设置");
    }

    @PacketHandler(value = "sendGatherRequest", description = "发送队伍集合请求")
    public void sendGatherRequest(PacketSender<Player> sender) {
        sender.sendMessage("§a§l[!] §6NuStarParty §f- 队伍集合请求已发送");
    }
}

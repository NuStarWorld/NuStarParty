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

package top.nustar.nustarparty.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import top.nustar.nustarparty.api.entity.Invite;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.language.PartyLanguage;

/**
 * @author NuStar
 * @since 2025/6/16 22:59
 */
public interface PartyService {

    PartyLanguage getPartyLanguage();

    /**
     * 获取玩家所在的队伍
     *
     * @param playerUUID 玩家UUID
     * @return 克隆的 {@link Party} 副本 仅做获取信息使用
     */
    Optional<Party> getParty(UUID playerUUID);

    /**
     * 获取所有队伍的克隆副本
     *
     * @return 队伍列表
     */
    List<Party> getPartyList();

    /**
     * 为玩家打开队伍菜单
     *
     * @param player 玩家
     */
    void openPartyMenu(Player player);

    /**
     * 为玩家打开自己的队伍菜单
     *
     * @param player 玩家
     */
    void openMyPartyMenu(Player player);

    /**
     * 为玩家打开入队申请列表菜单
     *
     * @param player 玩家
     */
    void openJoinApplicationMenu(Player player);

    /**
     * 为玩家打开邀请列表菜单
     *
     * @param player 玩家
     */
    void openInviteApplicationMenu(Player player);

    /**
     * 为玩家打开玩家列表菜单
     *
     * @param player 玩家
     */
    void openPlayerListMenu(Player player);

    /**
     * 创建队伍
     *
     * @param leader 队长
     */
    boolean createParty(Player leader);

    /**
     * 将一名玩家添加到队伍内
     *
     * @param leader 队长
     * @param memberUUID 成员UUID
     * @return 是否成功
     */
    boolean addPartyMember(OfflinePlayer leader, UUID memberUUID);

    /**
     * 创建加入队伍请求
     *
     * @param player 玩家
     * @param partyUUID 队伍UUID
     */
    boolean addJoinPartyRequest(Player player, UUID partyUUID);

    /**
     * 接受加入请求
     *
     * @param leader 队长
     * @param requesterUUID 请求者UUID
     */
    boolean acceptJoinApplication(Player leader, UUID requesterUUID);

    /**
     * 接受加入请求
     *
     * @param leader 队长
     * @param requesterName 请求者名称
     */
    boolean acceptJoinApplication(Player leader, String requesterName);

    /**
     * 拒绝加入请求
     *
     * @param leader 队长
     * @param requesterUUID 请求者UUID
     */
    boolean refuseJoinApplication(Player leader, UUID requesterUUID);

    /**
     * 拒绝加入请求
     *
     * @param leader 队长
     * @param requesterName 请求者名称
     */
    boolean refuseJoinApplication(Player leader, String requesterName);

    /**
     * 退出队伍
     *
     * @param member 成员
     */
    boolean quitParty(Player member);

    /**
     * 解散队伍
     *
     * @param leader 队长
     * @return 是否解散成功
     */
    boolean disbandParty(Player leader);

    /**
     * 踢出一名玩家
     *
     * @param leader 队长
     * @param memberUUID 成员UUID
     * @param reason 踢出原因
     */
    boolean kickMember(Player leader, UUID memberUUID, String reason);

    /**
     * 踢出一名玩家
     *
     * @param leader 队长
     * @param memberName 成员名称
     * @param reason 踢出原因
     */
    boolean kickMember(Player leader, String memberName, String reason);

    /**
     * 邀请一名玩家加入队伍
     *
     * @param member 成员
     * @param invitedPlayer 邀请的玩家
     * @param inviteReason 邀请理由
     */
    boolean invitePlayer(Player member, Player invitedPlayer, String inviteReason);

    /**
     * 接受一个玩家的邀请请求
     *
     * @param player 玩家
     * @param inviterName 邀请你的玩家名称
     * @return 是否成功
     */
    boolean acceptInviteApplication(Player player, String inviterName);

    /**
     * 接受一个玩家的邀请请求
     *
     * @param player 玩家
     * @param inviterUid 邀请你的玩家 UUID
     * @return 是否成功
     */
    boolean acceptInviteApplication(Player player, UUID inviterUid);

    /**
     * 拒绝一个玩家的邀请请求
     *
     * @param player 玩家
     * @param inviterName 邀请你的玩家名称
     * @return 是否成功
     */
    boolean refuseInviteApplication(Player player, String inviterName);

    /**
     * 拒绝一个玩家的邀请请求
     *
     * @param player 玩家
     * @param inviterUid 邀请你的玩家 UUID
     * @return 是否成功
     */
    boolean refuseInviteApplication(Player player, UUID inviterUid);
    /**
     * 获取一个玩家的 Invite 对象(副本)
     *
     * @param playerUUID 玩家 UUID
     * @return Invite 对象
     */
    Optional<Invite> getPlayerInvite(UUID playerUUID);

    /**
     * 判断两个玩家是否在同一个队伍内
     *
     * @param playerUUID1 玩家1
     * @param playerUUID2 玩家2
     * @return 是否在相同队伍内
     */
    boolean isInSameParty(UUID playerUUID1, UUID playerUUID2);

    /**
     * 获取所有没有队伍的玩家
     *
     * @return 玩家列表
     */
    List<Player> getNonPartyPlayers();

    /**
     * 是否开启地牢中途加入功能
     * @return 是否开启
     */
    boolean isEnableJoinDungeonMidway();
}

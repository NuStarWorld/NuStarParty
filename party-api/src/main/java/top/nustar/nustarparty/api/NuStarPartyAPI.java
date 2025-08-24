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

package top.nustar.nustarparty.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.Scope;
import top.nustar.nustarparty.api.entity.Invite;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.service.PartyService;

/**
 * @author NuStar
 * @since 2025/6/15 16:19
 */
@Component
@Scope(Scope.SINGLETON)
@SuppressWarnings("unused")
public class NuStarPartyAPI {
    private static volatile PartyService partyService;

    public static boolean createParty(Player leader) {
        return partyService.createParty(leader);
    }

    public static boolean quitParty(Player player) {
        return partyService.quitParty(player);
    }

    public static boolean disbandParty(Player leader) {
        return partyService.disbandParty(leader);
    }

    public static boolean kickMember(Player leader, UUID memberUUID, String reason) {
        return partyService.kickMember(leader, memberUUID, reason);
    }

    public static boolean kickMember(Player leader, String memberName, String reason) {
        return partyService.kickMember(leader, memberName, reason);
    }

    public static boolean invitePlayer(Player member, Player invitedPlayer, String inviteReason) {
        return partyService.invitePlayer(member, invitedPlayer, inviteReason);
    }

    public static List<Party> getPartyList() {
        return partyService.getPartyList();
    }

    public static Optional<Party> getPlayerParty(UUID playerUid) {
        return partyService.getParty(playerUid);
    }

    public static Optional<Invite> getPlayerInvite(UUID playerUid) {
        return partyService.getPlayerInvite(playerUid);
    }

    public static List<Player> getNonPartyPlayers() {
        return partyService.getNonPartyPlayers();
    }

    /** @see PartyService#addPartyMember(OfflinePlayer, UUID) */
    public static boolean addPartyMember(OfflinePlayer leader, UUID memberUUID) {
        return partyService.addPartyMember(leader, memberUUID);
    }

    /** @see PartyService#addJoinPartyRequest(Player, UUID) */
    public static boolean addJoinPartyRequest(Player player, UUID partyUUID) {
        return partyService.addJoinPartyRequest(player, partyUUID);
    }

    /** @see PartyService#acceptJoinApplication(Player, UUID) */
    public static boolean acceptJoinApplication(Player leader, UUID requesterUUID) {
        return partyService.acceptJoinApplication(leader, requesterUUID);
    }

    /** @see PartyService#acceptJoinApplication(Player, String) */
    public static boolean acceptJoinApplication(Player leader, String requesterName) {
        return partyService.acceptJoinApplication(leader, requesterName);
    }

    /** @see PartyService#refuseJoinApplication(Player, UUID) */
    public static boolean refuseJoinApplication(Player leader, UUID requesterUUID) {
        return partyService.refuseJoinApplication(leader, requesterUUID);
    }

    /** @see PartyService#refuseJoinApplication(Player, String) */
    public static boolean refuseJoinApplication(Player leader, String requesterName) {
        return partyService.refuseJoinApplication(leader, requesterName);
    }

    /** @see PartyService#acceptInviteApplication(Player, String) */
    public static boolean acceptInviteRequest(Player player, String inviterName) {
        return partyService.acceptInviteApplication(player, inviterName);
    }

    /** @see PartyService#acceptInviteApplication(Player, UUID) */
    public static boolean acceptInviteRequest(Player player, UUID inviterUid) {
        return partyService.acceptInviteApplication(player, inviterUid);
    }

    /** @see PartyService#refuseInviteApplication(Player, String) */
    public static boolean refuseInviteRequest(Player player, String inviterName) {
        return partyService.refuseInviteApplication(player, inviterName);
    }

    /** @see PartyService#refuseInviteApplication(Player, UUID) */
    public static boolean refuseInviteRequest(Player player, UUID inviterUid) {
        return partyService.refuseInviteApplication(player, inviterUid);
    }

    /** @see PartyService#isInSameParty(UUID, UUID) */
    public static boolean isInSameParty(UUID playerUUID1, UUID playerUUID2) {
        return partyService.isInSameParty(playerUUID1, playerUUID2);
    }

    @Autowired
    public void setPartyService(PartyService partyService) {
        NuStarPartyAPI.partyService = partyService;
    }
}

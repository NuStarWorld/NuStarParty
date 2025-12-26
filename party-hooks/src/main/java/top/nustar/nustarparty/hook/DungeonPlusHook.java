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

package top.nustar.nustarparty.hook;

import static team.idealstate.sugar.next.function.Functional.functional;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.serverct.ersha.dungeon.DungeonPlus;
import org.serverct.ersha.dungeon.common.api.component.script.type.ScriptType;
import org.serverct.ersha.dungeon.common.api.event.DungeonEvent;
import org.serverct.ersha.dungeon.common.api.event.dungeon.DungeonStartEvent;
import org.serverct.ersha.dungeon.common.team.Team;
import org.serverct.ersha.dungeon.common.team.type.TeamOperationType;
import org.serverct.ersha.dungeon.internal.dungeon.Dungeon;
import team.idealstate.sugar.next.context.annotation.component.Subscriber;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.event.*;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.service.PartyService;

/**
 * @author NuStar
 * @since 2025/7/20 21:19
 */
@Subscriber
@DependsOn(
        classes = "org.serverct.ersha.dungeon.DungeonPlus",
        properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
@SuppressWarnings("unused")
public class DungeonPlusHook implements Listener {
    private volatile PartyService service;

    @EventHandler
    public void on(DungeonEvent event) {
        if (!(event.getEvent() instanceof DungeonStartEvent.Before)) return;
        Team team = event.getDungeon().getTeam();
        UUID leaderUid = team.leader;
        Player leaderPlayer = team.getLeaderPlayer();
        service.getParty(leaderUid).ifPresent(party -> {
            if (!party.getLeader().getUniqueId().equals(leaderUid)) {
                functional(leaderPlayer).run(it -> service.getPartyLanguage()
                        .getOnNonLeaderStartDungeon()
                        .use(it::sendMessage));
                event.setCancelled(true);
                return;
            }
            AtomicBoolean memberIsInDungeon = new AtomicBoolean(false);
            for (OfflinePlayer member : party.getMembers()) {
                functional(member)
                        .convert(OfflinePlayer::getPlayer)
                        .when(Objects::nonNull)
                        .run(it -> {
                            if (it.getWorld().getName().contains("dungeon")) {
                                memberIsInDungeon.set(true);
                            }
                        });
                if (memberIsInDungeon.get()) {
                    functional(leaderPlayer).run(it -> service.getPartyLanguage()
                            .getOnOtherMemberInDungeon()
                            .use(it::sendMessage));
                    event.setCancelled(true);
                    return;
                }
            }
            for (OfflinePlayer member : party.getMembers()) {
                team.playerOperation(member.getUniqueId(), TeamOperationType.JOIN, false, false);
            }
        });
    }

    @EventHandler
    public void on(QuitPartyEvent.After event) {
        Dungeon leaderDungeon =
                DungeonPlus.dungeonManager.getDungeon(event.getQuitParty().getLeaderUid());
        if (leaderDungeon != null) {
            if (leaderDungeon.getTeam().getPlayers().contains(event.getPlayer().getUniqueId())) {
                functional(event.getPlayer()).run(leaderDungeon::leave);
            }
        } else {
            Team team = DungeonPlus.teamManager.getTeam(event.getPlayer());
            if (team != null) {
                team.playerOperation(event.getPlayer().getUniqueId(), TeamOperationType.QUIT, true, false);
            }
        }
    }

    @EventHandler
    public void on(KickPartyEvent event) {
        UUID memberUid = event.getMember().getUniqueId();
        Dungeon leaderDungeon =
                DungeonPlus.dungeonManager.getDungeon(event.getParty().getLeaderUid());
        if (leaderDungeon != null) {
            if (leaderDungeon.getTeam().getPlayers().contains(memberUid)) {
                functional(event.getMember())
                        .convert(OfflinePlayer::getPlayer)
                        .when(Objects::nonNull)
                        .run(leaderDungeon::leave);
            }
        } else {
            Team team = DungeonPlus.teamManager.getTeam(memberUid);
            if (team != null) {
                team.playerOperation(memberUid, TeamOperationType.KICK, true, false);
            }
        }
    }

    @EventHandler
    public void on(AcceptJoinApplicationEvent.After event) {
        if (!service.isEnableJoinDungeonMidway()) return;
        Party joinParty = event.getJoinParty();
        Dungeon leaderDungeon = DungeonPlus.dungeonManager.getDungeon(joinParty.getLeaderUid());
        if (leaderDungeon == null || leaderDungeon.getDungeonEditMode()) return;
        OfflinePlayer applicant = event.getApplicant();
        leaderDungeon.getTeam().playerOperation(applicant.getUniqueId(), TeamOperationType.JOIN, true, false);
        functional(applicant)
                .convert(OfflinePlayer::getPlayer)
                .when(Objects::nonNull)
                .run(it -> {
                    JoinPartyDungeonEvent.Pre pre = new JoinPartyDungeonEvent.Pre(it, joinParty);
                    Bukkit.getPluginManager().callEvent(pre);
                    if (pre.isCancelled()) return;
                    boolean start = true;
                    if (!leaderDungeon
                            .getDungeonContent()
                            .getStartConditionScript()
                            .isEmpty()) {
                        start = leaderDungeon.conditionScriptHandle(
                                "start-condition",
                                leaderDungeon.getDungeonContent().getStartConditionScript(),
                                leaderDungeon,
                                Collections.singletonList(ScriptType.SYSTEM));
                    }
                    if (!start) {
                        service.getPartyLanguage()
                                .getOnTeleportToDungeonConditionsNotMet()
                                .use(it::sendMessage);
                        return;
                    }
                    Location location = joinParty.getLeader().getPlayer().getLocation();
                    it.setFlying(false);
                    it.setGameMode(leaderDungeon.getGameMode());
                    it.teleport(location, PlayerTeleportEvent.TeleportCause.UNKNOWN);
                    DungeonPlus.dungeonManager.setPlayerDungeon(it.getUniqueId(), leaderDungeon);
                    service.getPartyLanguage().getOnTeleportToDungeon().use(it::sendMessage);
                    Bukkit.getPluginManager().callEvent(new JoinPartyDungeonEvent.After(it, joinParty));
                });
    }

    @EventHandler
    public void on(DisbandPartyEvent.Pre event) {
        Dungeon leaderDungeon =
                DungeonPlus.dungeonManager.getDungeon(event.getPlayer().getUniqueId());
        if (leaderDungeon != null) {
            functional(event.getPlayer()).run(it -> service.getPartyLanguage()
                    .getOnCannotDisbandPartyWhileInDungeon()
                    .use(it::sendMessage));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void on(DisbandPartyEvent.After event) {
        Team team = DungeonPlus.teamManager.getTeam(event.getPlayer());
        if (team == null) return;
        team.playerOperation(team.leader, TeamOperationType.DISBAND, false, false);
    }

    @Autowired
    public void setService(PartyService service) {
        this.service = service;
    }
}

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

import java.util.UUID;
import net.playavalon.mythicdungeons.MythicDungeons;
import net.playavalon.mythicdungeons.api.events.dungeon.DungeonStartEvent;
import net.playavalon.mythicdungeons.api.parents.dungeons.AbstractDungeon;
import net.playavalon.mythicdungeons.player.MythicPlayer;
import net.playavalon.mythicdungeons.utility.helpers.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import team.idealstate.sugar.next.context.annotation.component.Subscriber;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.service.PartyService;

/**
 * @author NuStar
 * @since 2025/7/24 23:25
 */
@Subscriber
@DependsOn(
        classes = "net.playavalon.mythicdungeons.MythicDungeons",
        properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
public class MythicDungeonHook implements Listener {

    private volatile PartyService service;

    @EventHandler
    public void on(DungeonStartEvent event) {
        MythicPlayer mythicLeader = event.getMythicPlayers().get(0);
        Player leader = mythicLeader.getPlayer();
        UUID leaderUid = leader.getUniqueId();
        service.getParty(leaderUid).ifPresent(party -> {
            if (!party.getLeader().getUniqueId().equals(leaderUid)) {
                functional(leader).run(it -> service.getPartyLanguage()
                        .getOnNonLeaderStartDungeon()
                        .use(it::sendMessage));
                return;
            }
            for (OfflinePlayer member : party.getMembers()) {
                event.getMythicPlayers().add(MythicDungeons.inst().getMythicPlayer(member.getUniqueId()));
            }
            AbstractDungeon dungeon = event.getDungeon();
            for (MythicPlayer mythicPlayer : event.getMythicPlayers()) {
                Player player = mythicPlayer.getPlayer();
                if (dungeon.getConfig().getBoolean("General.ShowTitleOnStart", false)) {
                    player.sendTitle(
                            Util.fullColor(dungeon.getConfig().getString("General.DisplayName", "&cA Dungeon")),
                            "",
                            10,
                            70,
                            10);
                }
                Util.forceTeleport(player, dungeon.getStartSpawn());
                mythicPlayer.setDungeonRespawn(dungeon.getStartSpawn());
                if (!dungeon.isCooldownOnStart()) continue;
                dungeon.addAccessCooldown(player);
            }
        });
    }

    @Autowired
    public void setService(PartyService service) {
        this.service = service;
    }
}

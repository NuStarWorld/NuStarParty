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

package top.nustar.nustarparty.core.subscriber;

import static team.idealstate.sugar.next.function.Functional.functional;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import team.idealstate.sugar.next.context.Context;
import team.idealstate.sugar.next.context.annotation.component.Subscriber;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import team.idealstate.sugar.next.context.aware.ContextAware;
import team.idealstate.sugar.validate.Validation;
import top.nustar.nustarcorebridge.api.service.PlaceholderService;
import top.nustar.nustarparty.NuStarParty;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.event.AcceptJoinApplicationEvent;
import top.nustar.nustarparty.api.event.AddJoinApplicationEvent;
import top.nustar.nustarparty.api.event.RefuseJoinApplicationEvent;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.service.PartyService;
import top.nustar.nustarparty.core.packet.PartyPlaceholderPacket;

/**
 * @author NuStar
 * @since 2025/7/6 20:34
 */
@Subscriber
@DependsOn(
        classes = "top.nustar.nustarcorebridge.NuStarCoreBridge",
        properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
@SuppressWarnings("unused")
public class PartySubscriber implements Listener, ContextAware {

    private volatile PartyService partyService;
    private volatile PlaceholderService placeholderService;
    private volatile Context context;

    @EventHandler
    public void on(AddJoinApplicationEvent.After event) {
        Validation.notNull(placeholderService, "placeholderService cannot be null");
        Party joinParty = event.getJoinParty();
        OfflinePlayer leader = joinParty.getLeader();
        functional(leader)
                .convert(OfflinePlayer::getPlayer)
                .when(Objects::nonNull)
                .run(it -> {
                    placeholderService.sendPlaceholder(it, PartyPlaceholderPacket.RECEIVE_JOIN_APPLICATION, "1");

                    if (!joinParty.isSendAddJoinApplicationPlaceholder()) {
                        Bukkit.getScheduler()
                                .runTaskLaterAsynchronously(
                                        (NuStarParty) context.getHolder(),
                                        () -> {
                                            placeholderService.removePlaceholder(
                                                    it, PartyPlaceholderPacket.RECEIVE_JOIN_APPLICATION, false);
                                            joinParty.setSendAddJoinApplicationPlaceholder(false);
                                        },
                                        600L);
                    }
                    joinParty.setSendAddJoinApplicationPlaceholder(true);
                });
    }

    @EventHandler
    public void on(AcceptJoinApplicationEvent.After event) {
        Validation.notNull(placeholderService, "placeholderService cannot be null");
        if (event.getJoinParty().getJoinApplicationList().isEmpty()) {
            OfflinePlayer leader = event.getJoinParty().getLeader();
            functional(leader)
                    .convert(OfflinePlayer::getPlayer)
                    .when(Objects::nonNull)
                    .run(it -> placeholderService.removePlaceholder(
                            it, PartyPlaceholderPacket.RECEIVE_JOIN_APPLICATION, false));
            event.getJoinParty().setSendAddJoinApplicationPlaceholder(false);
        }
    }

    @EventHandler
    public void on(RefuseJoinApplicationEvent.After event) {
        Validation.notNull(placeholderService, "placeholderService cannot be null");
        if (event.getRefuseParty().getJoinApplicationList().isEmpty()) {
            OfflinePlayer leader = event.getRefuseParty().getLeader();
            functional(leader)
                    .convert(OfflinePlayer::getPlayer)
                    .when(Objects::nonNull)
                    .run(it -> placeholderService.removePlaceholder(
                            it, PartyPlaceholderPacket.RECEIVE_JOIN_APPLICATION, false));
            event.getRefuseParty().setSendAddJoinApplicationPlaceholder(false);
        }
    }

    @Autowired
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Autowired
    public void setPlaceholderService(PlaceholderService placeholderService) {
        this.placeholderService = placeholderService;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}

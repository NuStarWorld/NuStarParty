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

package top.nustar.nustarparty.core.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import top.nustar.nustarparty.api.entity.Invite;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.core.entity.InviteImpl;

/**
 * @author NuStar
 * @since 2025/6/15 16:21
 */
@Component
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
public class InviteManager {
    private final Map<UUID, Invite> inviteMap = new HashMap<>();

    public Optional<Invite> getInvite(UUID playerUid) {
        return Optional.of(inviteMap.computeIfAbsent(playerUid, InviteImpl::new));
    }

    public void deleteInvite(UUID player) {
        inviteMap.computeIfPresent(player, (uuid, invite) -> {
            invite.getInviteApplications().clear();
            return null;
        });
    }

    public void updateInvite(UUID playerUid, Consumer<Invite> consumer) {
        Optional<Invite> inviteOptional = getInvite(playerUid);
        inviteOptional.ifPresent(consumer);
    }
}

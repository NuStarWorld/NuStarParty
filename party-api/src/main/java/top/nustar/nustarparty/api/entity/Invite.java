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

package top.nustar.nustarparty.api.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.OfflinePlayer;
import top.nustar.nustarparty.api.entity.component.InviteApplication;

/**
 * @author NuStar
 * @since 2025/8/7 00:43
 */
public interface Invite {
    void addInviteRequest(Party party, OfflinePlayer inviter, String inviteReason);

    void removeInviteRequest(UUID inviterUid);

    Optional<InviteApplication> getInviteApplication(UUID inviterUid);

    Optional<InviteApplication> popInviteRequest(Predicate<InviteApplication> matcher, boolean isAccept);

    Invite copy();

    List<InviteApplication> getInviteApplications();

    UUID getHolder();
}

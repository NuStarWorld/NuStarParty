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

package top.nustar.nustarparty.core.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import top.nustar.nustarparty.api.entity.Invite;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.entity.component.InviteApplication;
import top.nustar.nustarparty.api.event.AcceptInviteApplicationEvent;
import top.nustar.nustarparty.api.event.RefuseInviteApplicationEvent;

/**
 * @author NuStar
 * @since 2025/6/15 13:58
 */
@Data
public class InviteImpl implements Invite {
    private final List<InviteApplication> inviteApplications = new ArrayList<>();
    private final UUID holder;

    public InviteImpl(UUID holder) {
        this.holder = holder;
    }

    @Override
    public void addInviteRequest(Party party, OfflinePlayer inviter, String inviteReason) {
        inviteApplications.add(new InviteApplication(inviter, party, inviteReason));
    }

    @Override
    public void removeInviteRequest(UUID inviterUid) {
        inviteApplications.removeIf(inviteApplication ->
                inviteApplication.getInviter().getUniqueId().equals(inviterUid));
    }

    @Override
    public Optional<InviteApplication> getInviteApplication(UUID inviterUid) {
        return inviteApplications.stream()
                .filter(inviteApplication ->
                        inviteApplication.getInviter().getUniqueId().equals(inviterUid))
                .findFirst();
    }

    @Override
    public Optional<InviteApplication> popInviteRequest(Predicate<InviteApplication> matcher, boolean isAccept) {
        Optional<InviteApplication> popApplicationOptional =
                inviteApplications.stream().filter(matcher).findFirst();
        if (popApplicationOptional.isPresent()) {
            InviteApplication application = popApplicationOptional.get();
            if (isAccept) {
                AcceptInviteApplicationEvent.Pre acceptInviteApplicationPreEvent =
                        new AcceptInviteApplicationEvent.Pre(application, holder);
                Bukkit.getPluginManager().callEvent(acceptInviteApplicationPreEvent);
                if (acceptInviteApplicationPreEvent.isCancelled()) return Optional.empty();
            } else {
                RefuseInviteApplicationEvent.Pre refuseInviteApplicationPreEvent =
                        new RefuseInviteApplicationEvent.Pre(application, holder);
                Bukkit.getPluginManager().callEvent(refuseInviteApplicationPreEvent);
                if (refuseInviteApplicationPreEvent.isCancelled()) return Optional.empty();
            }
            inviteApplications.remove(application);
            if (isAccept) {
                Bukkit.getPluginManager().callEvent(new AcceptInviteApplicationEvent.After(application, holder));
            } else {
                Bukkit.getPluginManager().callEvent(new RefuseInviteApplicationEvent.After(application, holder));
            }
            return popApplicationOptional;
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Invite copy() {
        InviteImpl invite = new InviteImpl(holder);
        invite.inviteApplications.addAll(inviteApplications);
        return invite;
    }
}

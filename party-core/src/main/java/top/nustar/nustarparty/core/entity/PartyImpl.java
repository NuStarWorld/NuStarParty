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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.event.AcceptJoinApplicationEvent;
import top.nustar.nustarparty.api.event.RefuseJoinApplicationEvent;

/**
 * @author NuStar
 * @since 2025/6/14 21:48
 */
@Data
public class PartyImpl implements Party {
    private final UUID partyUUID;
    private String partyName;
    private OfflinePlayer leader;
    private final List<OfflinePlayer> members = new ArrayList<>();
    private final List<OfflinePlayer> joinApplicationList = new ArrayList<>();
    private final int maxSize;
    private AtomicBoolean sendAddJoinApplicationPlaceholder = new AtomicBoolean(false);

    public PartyImpl(UUID partyUUID, OfflinePlayer leader, int maxSize) {
        this.partyUUID = partyUUID;
        this.leader = leader;
        this.partyName = leader.getName() + "的队伍";
        this.maxSize = maxSize;
    }

    @Override
    public void addMember(OfflinePlayer member) {
        members.add(member);
    }

    @Override
    public void removeMember(OfflinePlayer member) {
        members.removeIf(offlinePlayer -> offlinePlayer.getUniqueId().equals(member.getUniqueId()));
    }

    @Override
    public OfflinePlayer removeMember(Predicate<OfflinePlayer> matcher) {
        return members.stream()
                .filter(matcher)
                .findFirst()
                .map(offlinePlayer -> {
                    removeMember(offlinePlayer);
                    return offlinePlayer;
                })
                .orElse(null);
    }

    @Override
    public boolean joinApplicationExist(Player player) {
        return joinApplicationList.contains(player);
    }

    @Override
    public Optional<OfflinePlayer> popJoinApplicant(Predicate<OfflinePlayer> matcher, boolean isAccept) {
        Optional<OfflinePlayer> popJoinApplicant =
                joinApplicationList.stream().filter(matcher).findFirst();
        if (popJoinApplicant.isPresent()) {
            OfflinePlayer applicant = popJoinApplicant.get();
            if (isAccept) {
                AcceptJoinApplicationEvent.Pre acceptJoinApplicationPreEvent =
                        new AcceptJoinApplicationEvent.Pre(this, applicant);
                Bukkit.getPluginManager().callEvent(acceptJoinApplicationPreEvent);
                if (acceptJoinApplicationPreEvent.isCancelled()) return Optional.empty();
            } else {
                RefuseJoinApplicationEvent.Pre refuseJoinApplicationPreEvent =
                        new RefuseJoinApplicationEvent.Pre(this, applicant);
                Bukkit.getPluginManager().callEvent(refuseJoinApplicationPreEvent);
                if (refuseJoinApplicationPreEvent.isCancelled()) return Optional.empty();
            }
            joinApplicationList.remove(applicant);
            if (isAccept) {
                addMember(applicant);
                Bukkit.getPluginManager().callEvent(new AcceptJoinApplicationEvent.After(this, applicant));
            } else {
                Bukkit.getPluginManager().callEvent(new RefuseJoinApplicationEvent.After(this, applicant));
            }
            return popJoinApplicant;
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void addJoinApplication(Player player) {
        joinApplicationList.add(player);
    }

    @Override
    public boolean isLeader(UUID playerUUID) {
        return leader.getUniqueId().equals(playerUUID);
    }

    @Override
    public boolean isLeader(String playerName) {
        return leader.getName().equals(playerName);
    }

    @Override
    public boolean isFull() {
        return members.size() >= maxSize;
    }

    @Override
    public int getEmptyPositions() {
        return maxSize - members.size();
    }

    @Override
    public int getSize() {
        return members.size();
    }

    @Override
    public boolean isMember(UUID playerUUID) {
        if (isLeader(playerUUID)) return true;
        for (OfflinePlayer member : members) {
            if (member.getUniqueId().equals(playerUUID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMember(String memberName) {
        if (leader.getName().equals(memberName)) return true;
        for (OfflinePlayer member : members) {
            if (member.getName().equals(memberName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Party copy() {
        PartyImpl party = new PartyImpl(partyUUID, leader, maxSize);
        party.members.addAll(members);
        party.joinApplicationList.addAll(joinApplicationList);
        return party;
    }

    @Override
    public UUID getLeaderUid() {
        return leader.getUniqueId();
    }

    @Override
    public String getLeaderName() {
        return leader.getName();
    }

    @Override
    public boolean isSendAddJoinApplicationPlaceholder() {
        return this.sendAddJoinApplicationPlaceholder.get();
    }

    @Override
    public void setSendAddJoinApplicationPlaceholder(boolean sendAddJoinApplicationPlaceholder) {
        this.sendAddJoinApplicationPlaceholder.set(sendAddJoinApplicationPlaceholder);
    }
}

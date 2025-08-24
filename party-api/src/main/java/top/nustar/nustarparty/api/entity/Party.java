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
import org.bukkit.entity.Player;

/**
 * @author NuStar
 * @since 2025/8/7 00:29
 */
public interface Party {
    void addMember(OfflinePlayer member);

    void removeMember(OfflinePlayer member);

    OfflinePlayer removeMember(Predicate<OfflinePlayer> matcher);

    boolean joinApplicationExist(Player player);

    Optional<OfflinePlayer> popJoinApplicant(Predicate<OfflinePlayer> matcher, boolean isAccept);

    void addJoinApplication(Player player);

    boolean isLeader(UUID playerUUID);

    boolean isLeader(String playerName);

    boolean isFull();

    int getEmptyPositions();

    int getSize();

    boolean isMember(UUID playerUUID);

    boolean isMember(String memberName);

    Party copy();

    UUID getLeaderUid();

    String getLeaderName();

    UUID getPartyUUID();

    String getPartyName();

    OfflinePlayer getLeader();

    List<OfflinePlayer> getMembers();

    List<OfflinePlayer> getJoinApplicationList();

    void setPartyName(String partyName);

    void setLeader(OfflinePlayer leader);

    int getMaxSize();

    boolean isSendAddJoinApplicationPlaceholder();

    void setSendAddJoinApplicationPlaceholder(boolean sendAddJoinApplicationPlaceholder);
}

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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.core.entity.PartyImpl;

/**
 * @author NuStar
 * @since 2025/6/14 21:50
 */
@Component
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
public class PartyManager {
    private final Map<UUID, Party> partyMap = new LinkedHashMap<>();
    private final Map<UUID, Party> playerPartyMap = new ConcurrentHashMap<>();

    public Optional<Party> getParty(UUID uuid) {
        return Optional.ofNullable(partyMap.getOrDefault(uuid, playerPartyMap.get(uuid)));
    }

    public void setPlayerParty(OfflinePlayer player, Party party) {
        playerPartyMap.put(player.getUniqueId(), party);
    }

    public PartyImpl createParty(Player leader, int maxSize) {
        PartyImpl party = new PartyImpl(UUID.randomUUID(), leader, maxSize);
        partyMap.put(party.getPartyUUID(), party);
        playerPartyMap.put(leader.getUniqueId(), party);
        return party;
    }

    public void removePlayerParty(UUID leader) {
        playerPartyMap.remove(leader);
    }

    public void deleteParty(UUID leader) {
        Optional<Party> partyOptional = getParty(leader);
        if (!partyOptional.isPresent()) return;
        Party party = partyOptional.get();
        partyMap.computeIfPresent(party.getPartyUUID(), (uuid, playerParty) -> {
            party.getMembers().forEach(member -> playerPartyMap.remove(member.getUniqueId()));
            return null;
        });
        playerPartyMap.remove(leader);
    }

    public void updateParty(UUID uuid, Consumer<Party> function) {
        Optional<Party> partyOptional = getParty(uuid);
        if (!partyOptional.isPresent()) return;
        function.accept(partyOptional.get());
    }

    public List<Party> getPartyList() {
        return new ArrayList<>(partyMap.values());
    }
}

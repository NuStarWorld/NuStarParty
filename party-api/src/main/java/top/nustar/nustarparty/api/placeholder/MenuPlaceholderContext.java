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

package top.nustar.nustarparty.api.placeholder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.Scope;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.service.PartyService;

/**
 * @author NuStar
 * @since 2025/7/10 00:10
 */
@Data
@Component
@Scope(Scope.SINGLETON)
@SuppressWarnings("unused")
public class MenuPlaceholderContext {

    private static PartyService partyService;
    private OfflinePlayer player;
    private boolean needPlayerOnline;
    private final Map<String, Object> mapContext = new HashMap<>();

    public static MenuPlaceholderContext create(OfflinePlayer player) {
        MenuPlaceholderContext menuPlaceholderContext = new MenuPlaceholderContext();
        menuPlaceholderContext.setPlayer(player);
        return menuPlaceholderContext;
    }

    public Player getOnlinePlayer() {
        return this.player.getPlayer();
    }

    public Optional<Party> getParty() {
        return partyService.getParty(player.getUniqueId());
    }

    public MenuPlaceholderContext addContext(String key, Object value) {
        mapContext.put(key, value);
        return this;
    }

    public Object getContext(String key) {
        return mapContext.get(key);
    }

    public <T> T getContext(String key, Class<T> clazz) {
        Object value = mapContext.get(key);
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    public Map<String, Object> getMapContext() {
        return new HashMap<>(mapContext);
    }

    @Autowired
    public void setPartyService(PartyService partyService) {
        MenuPlaceholderContext.partyService = partyService;
    }
}

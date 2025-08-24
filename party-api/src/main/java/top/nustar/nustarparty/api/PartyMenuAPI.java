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

package top.nustar.nustarparty.api;

import java.util.function.Function;
import org.bukkit.entity.Player;
import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.Scope;
import top.nustar.nustarparty.api.menu.PartyButton;
import top.nustar.nustarparty.api.placeholder.MenuPlaceholderContext;
import top.nustar.nustarparty.api.service.MenuPlaceholderRegistry;
import top.nustar.nustarparty.api.service.PartyButtonEventBus;
import top.nustar.nustarparty.api.service.PartyService;

/**
 * @author NuStar
 * @since 2025/8/6 22:03
 */
@Component
@Scope(Scope.SINGLETON)
@SuppressWarnings("unused")
public class PartyMenuAPI {
    private static volatile PartyService partyService;
    private static volatile MenuPlaceholderRegistry menuPlaceholderRegistry;
    private static volatile PartyButtonEventBus partyButtonEventBus;

    /** @see PartyButtonEventBus#registerPartyButton(PartyButton) */
    public static void registerPartyButton(PartyButton partyButton) {
        partyButtonEventBus.registerPartyButton(partyButton);
    }

    /** @see MenuPlaceholderRegistry#registerPlaceholder(String, Function, boolean) */
    public static void registerPlaceholder(
            String placeholder, Function<MenuPlaceholderContext, Object> function, boolean requireOnlinePlayer) {
        menuPlaceholderRegistry.registerPlaceholder(placeholder, function, requireOnlinePlayer);
    }

    /** @see MenuPlaceholderRegistry#parse(String, MenuPlaceholderContext) */
    public static String parse(String string, MenuPlaceholderContext ctx) {
        return menuPlaceholderRegistry.parse(string, ctx);
    }

    /** @see PartyService#openPartyMenu(Player) */
    public static void openPartyMenu(Player player) {
        partyService.openPartyMenu(player);
    }

    /** @see PartyService#openInviteApplicationMenu(Player) */
    public static void openInviteApplicationMenu(Player player) {
        partyService.openInviteApplicationMenu(player);
    }

    /** @see PartyService#openPlayerListMenu(Player) */
    public static void openPlayerListMenu(Player player) {
        partyService.openPlayerListMenu(player);
    }

    /** @see PartyService#openMyPartyMenu(Player) */
    public static void openMyPartyMenu(Player player) {
        partyService.openMyPartyMenu(player);
    }

    /** @see PartyService#openJoinApplicationMenu(Player) */
    public static void openJoinApplicationMenu(Player player) {
        partyService.openJoinApplicationMenu(player);
    }

    @Autowired
    public void setMenuPlaceholderRegistry(MenuPlaceholderRegistry menuPlaceholderRegistry) {
        PartyMenuAPI.menuPlaceholderRegistry = menuPlaceholderRegistry;
    }

    @Autowired
    public void setPartyService(PartyService partyService) {
        PartyMenuAPI.partyService = partyService;
    }
}

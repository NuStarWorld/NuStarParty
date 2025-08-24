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

package top.nustar.nustarparty.core.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import team.idealstate.sugar.next.context.annotation.feature.Scope;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.placeholder.MenuPlaceholderContext;
import top.nustar.nustarparty.api.placeholder.PlaceholderFunction;
import top.nustar.nustarparty.api.service.MenuPlaceholderRegistry;
import top.nustar.nustarparty.core.exception.NuStarPartyException;

/**
 * @author NuStar
 * @since 2025/7/9 23:56
 */
@Component
@Scope(Scope.SINGLETON)
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
@SuppressWarnings("unused")
public class MenuPlaceholderRegistryImpl implements MenuPlaceholderRegistry {
    private final Map<String, PlaceholderFunction> placeholderMap = new ConcurrentHashMap<>();

    public MenuPlaceholderRegistryImpl() {
        registerPlaceholder("name", ctx -> ctx.getPlayer().getName(), false);
        registerPlaceholder("level", ctx -> ctx.getOnlinePlayer().getLevel(), true);
        registerPlaceholder(
                "health", ctx -> String.format("%.2f", ctx.getOnlinePlayer().getHealth()), true);
        registerPlaceholder(
                "maxHealth", ctx -> String.format("%.2f", ctx.getOnlinePlayer().getMaxHealth()), true);
        registerPlaceholder(
                "last_online",
                ctx -> {
                    long lastPlayed = ctx.getPlayer().getLastPlayed();
                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
                    return formatter.format(Instant.ofEpochMilli(lastPlayed));
                },
                false);
        registerPlaceholder(
                "slots",
                ctx -> ctx.getParty()
                        .map(party -> party.getEmptyPositions() <= 0 ? "满人" : party.getEmptyPositions())
                        .orElse("不存在的队伍"),
                false);
        registerPlaceholder(
                "partyLeader",
                ctx -> ctx.getParty().map(party -> party.getLeader().getName()).orElse("无"),
                false);
        registerPlaceholder(
                "partySize",
                ctx -> ctx.getParty().map(party -> party.getMembers().size()).orElse(0) + 1,
                false);
        registerPlaceholder(
                "partyMaxSize", ctx -> ctx.getParty().map(Party::getMaxSize).orElse(0), false);
    }

    public void registerPlaceholder(
            String placeholder, Function<MenuPlaceholderContext, Object> function, boolean requireOnlinePlayer) {
        if (placeholderMap.containsKey(placeholder))
            throw new NuStarPartyException("placeholder " + placeholder + " has already been registered");
        placeholderMap.put(placeholder, new PlaceholderFunction(function, requireOnlinePlayer));
    }

    public String parse(String string, MenuPlaceholderContext ctx) {
        for (Map.Entry<String, PlaceholderFunction> entry : placeholderMap.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            PlaceholderFunction placeholderFunction = entry.getValue();
            if (placeholderFunction.isRequireOnlinePlayer()) {
                if (!ctx.getPlayer().isOnline() || ctx.getOnlinePlayer() == null) continue;
            }
            Object apply = placeholderFunction.getFunction().apply(ctx);
            String replacement = apply != null ? String.valueOf(apply) : placeholder;
            string = string.replace(placeholder, replacement);
        }
        return string;
    }
}

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

package top.nustar.nustarparty.api.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import top.nustar.nustarparty.api.entity.Party;

/**
 * @author NuStar
 * @since 2025/7/20 07:01
 */
public abstract class QuitPartyEvent extends Event {
    @Getter
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Pre extends QuitPartyEvent implements Cancellable {
        private final Player player;
        private final Party quitParty;

        @Setter
        private boolean cancelled = false;
    }

    @Getter
    @RequiredArgsConstructor
    public static class After extends QuitPartyEvent {
        private final Player player;
        private final Party quitParty;
    }
}

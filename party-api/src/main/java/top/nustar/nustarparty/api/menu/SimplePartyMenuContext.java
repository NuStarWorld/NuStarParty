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

package top.nustar.nustarparty.api.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;
import top.nustar.nustargui.entity.AbsNuStarGui;

/**
 * @author NuStar
 * @since 2025/8/6 21:41
 */
@Data
@AllArgsConstructor
public class SimplePartyMenuContext implements PartyMenuContext {
    private final Player holder;
    private final AbsNuStarGui gui;
}

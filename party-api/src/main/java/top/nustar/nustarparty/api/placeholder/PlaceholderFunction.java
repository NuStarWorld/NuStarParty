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

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author NuStar
 * @since 2025/7/16 22:18
 */
@AllArgsConstructor
@Getter
public class PlaceholderFunction {
    private final Function<MenuPlaceholderContext, Object> function;
    private final boolean requireOnlinePlayer;
}

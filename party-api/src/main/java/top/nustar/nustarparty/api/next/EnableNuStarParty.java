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

package top.nustar.nustarparty.api.next;

import java.lang.annotation.*;
import team.idealstate.sugar.next.context.annotation.feature.RegisterProperty;
import team.idealstate.sugar.next.context.annotation.feature.Scan;

/**
 * @author NuStar<br>
 * @since 2025/8/18 21:25<br>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Scan("top.nustar.nustarparty")
@RegisterProperty(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "true")
@SuppressWarnings("unused")
public @interface EnableNuStarParty {}

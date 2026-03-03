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

package top.nustar.nustarparty.hook.mythicdungeons;

import team.idealstate.sugar.next.context.annotation.component.Service;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.hook.MythicDungeonsHook;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;

/**
 * @author NuStar
 * @since 2025/7/24 23:25
 */
@SuppressWarnings("unused")
@Service
@DependsOn(
        classes = "net.playavalon.mythicdungeons.MythicDungeons",
        properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
public class MythicDungeonsHookImpl implements MythicDungeonsHook {
    @Override
    public Party triggerMythicDungeonParty(Party party) {
        return new MythicDungeonPartyProxy(party);
    }
}

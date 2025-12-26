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

package top.nustar.nustarparty.core.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import team.idealstate.sugar.next.context.Context;
import team.idealstate.sugar.next.context.annotation.component.Configuration;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import team.idealstate.sugar.next.context.annotation.feature.Scope;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;

/**
 * @author NuStar
 * @since 2025/6/19 20:57
 */
@Data
@Configuration(uri = "config.yml", release = Context.RESOURCE_EMBEDDED + "config.yml")
@Scope(Scope.PROTOTYPE)
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
public class PartyConfiguration {
    @JsonProperty("party-max-size")
    private int partyMaxSize;

    private boolean debug;

    @JsonProperty("enable-join-dungeon-midway")
    private boolean enableJoinDungeonMidway;
}

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

package top.nustar.nustarparty.core.entity.menu.button;

import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import top.nustar.nustarparty.api.NuStarPartyAPI;
import top.nustar.nustarparty.api.menu.AbstractPartyButton;
import top.nustar.nustarparty.api.menu.MenuExecuteResult;
import top.nustar.nustarparty.api.menu.PartyMenuContext;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;

/**
 * @author NuStar
 * @since 2025/8/6 22:20
 */
@Component
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
@SuppressWarnings("unused")
public class QuitPartyButton extends AbstractPartyButton {

    @Override
    protected MenuExecuteResult doExecute(PartyMenuContext ctx) {
        if (NuStarPartyAPI.quitParty(ctx.getHolder())) {
            ctx.getHolder().closeInventory();
        }
        return MenuExecuteResult.success();
    }

    @Override
    public String getType() {
        return "quitParty";
    }
}

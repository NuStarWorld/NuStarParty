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

import java.util.List;
import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import team.idealstate.sugar.next.context.annotation.feature.Scope;
import team.idealstate.sugar.validate.Validation;
import team.idealstate.sugar.validate.annotation.NotNull;
import top.nustar.nustarparty.api.menu.MenuExecuteResult;
import top.nustar.nustarparty.api.menu.PartyButton;
import top.nustar.nustarparty.api.menu.PartyMenuContext;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.service.PartyButtonEventBus;

/**
 * @author NuStar<br>
 * @since 2025/8/18 21:35<br>
 */
@Component
@Scope(Scope.SINGLETON)
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "true"))
@SuppressWarnings("unused")
public class PartyButtonEventBusImplDelegation implements PartyButtonEventBus {

    @NotNull
    private PartyButtonEventBusImpl getDelegation() {
        return Validation.requireNotNull(PartyButtonEventBusImpl.instance, "PartyButtonEventBus is not initialize");
    }

    @Override
    public MenuExecuteResult post(String type, PartyMenuContext ctx) {
        return getDelegation().post(type, ctx);
    }

    @Override
    public PartyButtonEventBus registerPartyButton(PartyButton partyButton) {
        return getDelegation().registerPartyButton(partyButton);
    }

    @Autowired
    public void addPartyButton(List<PartyButton> partyButtonList) {
        getDelegation().addPartyButton(partyButtonList);
    }
}

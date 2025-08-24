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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import team.idealstate.sugar.next.context.annotation.feature.Scope;
import team.idealstate.sugar.next.context.lifecycle.Initializable;
import team.idealstate.sugar.validate.Validation;
import top.nustar.nustarparty.api.menu.MenuExecuteResult;
import top.nustar.nustarparty.api.menu.PartyButton;
import top.nustar.nustarparty.api.menu.PartyMenuContext;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.service.PartyButtonEventBus;
import top.nustar.nustarparty.core.exception.NuStarPartyException;

/**
 * @author NuStar
 * @since 2025/8/6 22:27
 */
@Component
@Scope(Scope.SINGLETON)
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
@SuppressWarnings("unused")
public class PartyButtonEventBusImpl implements PartyButtonEventBus, Initializable {
    private final Map<String, PartyButton> partyButtonMap = new HashMap<>();
    static volatile PartyButtonEventBusImpl instance;

    @Override
    public void initialize() {
        Validation.isNull(instance, "PartyButtonEventBus instance already initialized");
        instance = this;
    }

    public PartyButtonEventBus registerPartyButton(PartyButton partyButton) {
        String buttonType = partyButton.getType();
        if (partyButtonMap.containsKey(buttonType)) {
            throw new NuStarPartyException("PartyButton type " + buttonType + " has been registered");
        }
        partyButtonMap.put(buttonType, partyButton);
        return this;
    }

    public MenuExecuteResult post(String type, PartyMenuContext ctx) {
        if (!partyButtonMap.containsKey(type)) {
            throw new NuStarPartyException("PartyButton type " + type + " has not been registered");
        }
        return partyButtonMap.get(type).execute(ctx);
    }

    @Autowired
    public void addPartyButton(List<PartyButton> partyButtonList) {
        this.partyButtonMap.putAll(
                partyButtonList.stream().collect(Collectors.toMap(PartyButton::getType, Function.identity())));
    }
}

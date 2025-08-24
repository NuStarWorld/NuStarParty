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

package top.nustar.nustarparty.core.subscriber;

import static team.idealstate.sugar.next.function.Functional.functional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import team.idealstate.sugar.next.context.annotation.component.Subscriber;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import team.idealstate.sugar.next.context.annotation.feature.Scope;
import top.nustar.nustargui.entity.AbsNuStarGui;
import top.nustar.nustarparty.api.event.inv.MenuClickEvent;
import top.nustar.nustarparty.api.menu.MenuExecuteResult;
import top.nustar.nustarparty.api.menu.PartyMenuContext;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.service.PartyButtonEventBus;
import top.nustar.nustarparty.api.service.PartyService;
import top.nustar.nustarparty.core.entity.component.AbstractTypeButton;
import top.nustar.nustarparty.core.entity.menu.AbstractMenu;
import top.nustar.nustarparty.core.exception.NuStarPartyException;
import top.nustar.nustarparty.core.factory.PartyMenuFactory;

/**
 * @author NuStar
 * @since 2025/7/15 21:31
 */
@Subscriber
@Scope(Scope.SINGLETON)
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
@SuppressWarnings("unused")
public class MenuSubscriber implements Listener {
    private volatile PartyService partyService;
    private volatile PartyMenuFactory partyMenuFactory;
    private volatile PartyButtonEventBus partyButtonEventBus;

    @EventHandler
    public void on(MenuClickEvent event) {
        AbsNuStarGui gui = event.getHolder().getAbsNuStarGui();
        ItemStack currentItem = event.getCurrentItem();
        Player player = event.getPlayer();
        switch (event.getMenuType()) {
            case PLAYER_LIST_MENU:
            case PARTY_MENU:
            case JOIN_APPLICATION_MENU:
            case INVITE_MENU:
            case MY_PARTY_MENU:
                handlePlayerButton(event, currentItem, player, gui);
                break;
        }
    }

    private void handlePlayerButton(MenuClickEvent clickEvent, ItemStack currentItem, Player player, AbsNuStarGui gui) {
        Map<String, AbstractTypeButton> typeButtonMap;
        switch (clickEvent.getMenuType()) {
            case PLAYER_LIST_MENU:
                typeButtonMap = convertTypeButtonMap(partyMenuFactory.getPlayerListButtonMap());
                break;
            case PARTY_MENU:
                typeButtonMap = convertTypeButtonMap(partyMenuFactory.getPartyButtonMap());
                break;
            case MY_PARTY_MENU:
                typeButtonMap = convertTypeButtonMap(partyMenuFactory.getMyPartyButtonMap());
                break;
            case JOIN_APPLICATION_MENU:
                typeButtonMap = convertTypeButtonMap(partyMenuFactory.getApplicantButtonMap());
                break;
            case INVITE_MENU:
                typeButtonMap = convertTypeButtonMap(partyMenuFactory.getInviterButtonMap());
                break;
            default:
                throw new NuStarPartyException("Invalid menu type");
        }
        Optional<AbstractTypeButton> typeButtonOpt = typeButtonMap.values().stream()
                .filter(it -> it.getButton().isSimilar(currentItem))
                .findFirst();
        if (typeButtonOpt.isPresent()) {
            AbstractTypeButton button = typeButtonOpt.get();
            if (button.getType() == null) return;
            handlePredefinedButton(button.getType(), player, gui);
            return;
        }

        AbstractMenu abstractMenu = (AbstractMenu) gui;
        Optional<Map.Entry<ItemStack, UUID>> dynamicMemberButtonEntry =
                abstractMenu.getDynamicButtonMap().entrySet().stream()
                        .filter(entry -> entry.getKey().isSimilar(currentItem))
                        .findFirst();
        if (dynamicMemberButtonEntry.isPresent()) {
            Map.Entry<ItemStack, UUID> entry = dynamicMemberButtonEntry.get();
            switch (clickEvent.getMenuType()) {
                case PLAYER_LIST_MENU:
                    partyService.invitePlayer(player, Bukkit.getPlayer(entry.getValue()), "通过玩家列表邀请");
                    break;
                case PARTY_MENU:
                    partyService.addJoinPartyRequest(player, entry.getValue());
                    break;
                case INVITE_MENU:
                    switch (clickEvent.getClickType()) {
                        case RIGHT:
                            if (partyService.refuseInviteApplication(player, entry.getValue())) {
                                partyService.openInviteApplicationMenu(player);
                            }
                            break;
                        case LEFT:
                            partyService.acceptInviteApplication(player, entry.getValue());
                            partyService.openInviteApplicationMenu(player);
                            break;
                    }
                    break;
                case MY_PARTY_MENU:
                    if (clickEvent.getClickType() == ClickType.SHIFT_LEFT) {
                        if (partyService.kickMember(player, entry.getValue(), "无")) {
                            partyService.openMyPartyMenu(player);
                        }
                    }
                    break;
                case JOIN_APPLICATION_MENU:
                    switch (clickEvent.getClickType()) {
                        case RIGHT:
                            if (partyService.refuseJoinApplication(player, entry.getValue())) {
                                partyService.openJoinApplicationMenu(player);
                            }
                            break;
                        case LEFT:
                            if (partyService.acceptJoinApplication(player, entry.getValue())) {
                                partyService.openJoinApplicationMenu(player);
                            }
                            break;
                    }
                    break;
            }
        }
    }

    private void handlePredefinedButton(String buttonType, Player player, AbsNuStarGui gui) {
        MenuExecuteResult result = partyButtonEventBus.post(buttonType, PartyMenuContext.create(player, gui));
        if (!result.isSuccess()) {
            partyService.openPartyMenu(player);
        }
        if (result.getMessage() != null) {
            player.sendMessage(result.getMessage());
        }
    }

    private <T extends AbstractTypeButton> Map<String, AbstractTypeButton> convertTypeButtonMap(
            Map<String, T> buttonMap) {
        return functional(buttonMap)
                .convert(it -> it.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> (AbstractTypeButton) e.getValue())))
                .it();
    }

    @Autowired
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Autowired
    public void setPartyButtonManager(PartyButtonEventBus partyButtonEventBusImpl) {
        this.partyButtonEventBus = partyButtonEventBusImpl;
    }

    @Autowired
    public void setPartyMenuFactory(PartyMenuFactory partyMenuFactory) {
        this.partyMenuFactory = partyMenuFactory;
    }
}

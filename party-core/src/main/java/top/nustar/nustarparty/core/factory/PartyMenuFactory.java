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

package top.nustar.nustarparty.core.factory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import team.idealstate.sugar.next.context.ContextHolder;
import team.idealstate.sugar.next.context.annotation.component.Component;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import team.idealstate.sugar.next.context.annotation.feature.Scope;
import team.idealstate.sugar.next.context.aware.ContextHolderAware;
import team.idealstate.sugar.next.context.lifecycle.Initializable;
import team.idealstate.sugar.validate.annotation.NotNull;
import top.nustar.nustargui.MenuUtil;
import top.nustar.nustargui.entity.MenuTemplate;
import top.nustar.nustarparty.NuStarParty;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.core.entity.component.PartyButton;
import top.nustar.nustarparty.core.entity.component.PlayerButton;
import top.nustar.nustarparty.core.entity.menu.*;

/**
 * @author NuStar
 * @since 2025/6/19 00:16
 */
@Component
@Getter
@Scope(Scope.PROTOTYPE)
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
// TODO 丑陋的工厂类，待优化
public class PartyMenuFactory implements ContextHolderAware, Initializable {
    private volatile NuStarParty plugin;

    private String partyMenuType;
    private MenuTemplate partyMenuTemplate;
    private final Map<String, PartyButton> partyButtonMap = new HashMap<>();

    private String myPartyMenuType;
    private MenuTemplate myPartyMenuTemplate;
    private final Map<String, PlayerButton> myPartyButtonMap = new HashMap<>();

    private String joinApplicationMenuType;
    private MenuTemplate joinApplicationMenuTemplate;
    private final Map<String, PlayerButton> applicantButtonMap = new HashMap<>();

    private String inviteMenuType;
    private MenuTemplate inviteMenuTemplate;
    private final Map<String, PlayerButton> inviterButtonMap = new HashMap<>();

    private String playerListMenuType;
    private MenuTemplate playerListMenuTemplate;
    private final Map<String, PlayerButton> playerListButtonMap = new HashMap<>();

    public ItemStack createPartyButton(Party party) {
        return partyButtonMap.get("party").getButton(party);
    }

    public ItemStack getPartyEmptyButton() {
        return partyButtonMap.get("empty").getButton();
    }

    public ItemStack createLeaderButton(Player holder, OfflinePlayer leader) {
        Player onlinePlayer = leader.getPlayer();
        if (onlinePlayer == null || !onlinePlayer.isOnline()) {
            return myPartyButtonMap.get("leader").getButton(holder, leader);
        } else {
            return myPartyButtonMap.get("leader").getButton(holder, onlinePlayer);
        }
    }

    public ItemStack createMemberButton(Player holder, OfflinePlayer member) {
        Player onlinePlayer = member.getPlayer();
        if (onlinePlayer == null || !member.isOnline()) {
            return myPartyButtonMap.get("member").getButton(holder, member);
        } else {
            return myPartyButtonMap.get("member").getButton(holder, onlinePlayer);
        }
    }

    public ItemStack getMyPartyEmptyButton(Player holder, OfflinePlayer offlinePlayer) {
        return myPartyButtonMap.get("empty").getButton(holder, offlinePlayer);
    }

    public ItemStack createApplicantButton(Player holder, OfflinePlayer applicant) {
        Player onlinePlayer = applicant.getPlayer();
        if (onlinePlayer == null || !applicant.isOnline()) {
            return applicantButtonMap.get("applicant").getButton(holder, applicant);
        } else {
            return applicantButtonMap.get("applicant").getButton(holder, onlinePlayer);
        }
    }

    public ItemStack getApplicationEmptyButton(Player holder, OfflinePlayer offlinePlayer) {
        return applicantButtonMap.get("empty").getButton(holder, offlinePlayer);
    }

    public ItemStack createInviterButton(Player holder, OfflinePlayer inviter) {
        Player onlinePlayer = inviter.getPlayer();
        if (onlinePlayer == null || !inviter.isOnline()) {
            return inviterButtonMap.get("inviter").getButton(holder, inviter);
        } else {
            return inviterButtonMap.get("inviter").getButton(holder, onlinePlayer);
        }
    }

    public ItemStack getInviteEmptyButton(Player holder, OfflinePlayer offlinePlayer) {
        return inviterButtonMap.get("empty").getButton(holder, offlinePlayer);
    }

    public ItemStack getPlayerListEmptyButton(Player holder, OfflinePlayer offlinePlayer) {
        return playerListButtonMap.get("empty").getButton(holder, offlinePlayer);
    }

    public ItemStack createPlayerButton(Player holder, OfflinePlayer inviter) {
        Player onlinePlayer = inviter.getPlayer();
        if (onlinePlayer == null || !inviter.isOnline()) {
            return playerListButtonMap.get("player").getButton(holder, inviter);
        } else {
            return playerListButtonMap.get("player").getButton(holder, onlinePlayer);
        }
    }

    public PartyMenu createPartyMenu(Player holder) {
        return new PartyMenu(
                partyMenuTemplate.getMenuTitle(), partyMenuType, partyMenuTemplate.getInventory(), null, holder, this);
    }

    public MyPartyMenu createMyPartyMenu(Party party, Player holder) {
        return new MyPartyMenu(
                myPartyMenuTemplate.getMenuTitle(),
                partyMenuType,
                myPartyMenuTemplate.getInventory(),
                party,
                holder,
                this);
    }

    public JoinApplicationMenu createJoinApplicationMenu(Player holder, Party party) {
        return new JoinApplicationMenu(
                joinApplicationMenuTemplate.getMenuTitle(),
                joinApplicationMenuType,
                joinApplicationMenuTemplate.getInventory(),
                party,
                holder,
                this);
    }

    public InviteMenu createInviteMenu(Player holder) {
        return new InviteMenu(
                inviteMenuTemplate.getMenuTitle(),
                inviteMenuType,
                inviteMenuTemplate.getInventory(),
                null,
                holder,
                this);
    }

    public PlayerListMenu createPlayerListMenu(Player holder) {
        return new PlayerListMenu(
                playerListMenuTemplate.getMenuTitle(),
                playerListMenuType,
                playerListMenuTemplate.getInventory(),
                null,
                holder,
                this);
    }

    @Override
    public void initialize() {
        File partyMenuFile = new File(plugin.getDataFolder(), "party-menu.yml");
        if (!partyMenuFile.exists()) {
            plugin.saveResource("party-menu.yml", false);
        }
        partyMenuTemplate = MenuUtil.buildMenu(partyMenuFile);
        partyMenuType = partyMenuTemplate.getMenuName();
        YamlConfiguration partyYml = YamlConfiguration.loadConfiguration(partyMenuFile);
        for (String key : partyYml.getConfigurationSection("Buttons").getKeys(false)) {
            partyButtonMap.put(key, new PartyButton(partyYml.getConfigurationSection("Buttons." + key)));
        }

        File myPartyMenuFile = new File(plugin.getDataFolder(), "my-party-menu.yml");
        if (!myPartyMenuFile.exists()) {
            plugin.saveResource("my-party-menu.yml", false);
        }
        myPartyMenuTemplate = MenuUtil.buildMenu(myPartyMenuFile);
        myPartyMenuType = myPartyMenuTemplate.getMenuName();
        YamlConfiguration myPartyYml = YamlConfiguration.loadConfiguration(myPartyMenuFile);
        for (String key : myPartyYml.getConfigurationSection("Buttons").getKeys(false)) {
            myPartyButtonMap.put(key, new PlayerButton(myPartyYml.getConfigurationSection("Buttons." + key)));
        }

        File joinApplicationMenuFile = new File(plugin.getDataFolder(), "party-join-application-menu.yml");
        if (!joinApplicationMenuFile.exists()) {
            plugin.saveResource("party-join-application-menu.yml", false);
        }
        joinApplicationMenuTemplate = MenuUtil.buildMenu(joinApplicationMenuFile);
        joinApplicationMenuType = joinApplicationMenuTemplate.getMenuName();
        YamlConfiguration joinApplicationYml = YamlConfiguration.loadConfiguration(joinApplicationMenuFile);
        for (String key : joinApplicationYml.getConfigurationSection("Buttons").getKeys(false)) {
            applicantButtonMap.put(key, new PlayerButton(joinApplicationYml.getConfigurationSection("Buttons." + key)));
        }

        File inviteMenuFile = new File(plugin.getDataFolder(), "invite-menu.yml");
        if (!inviteMenuFile.exists()) {
            plugin.saveResource("invite-menu.yml", false);
        }
        inviteMenuTemplate = MenuUtil.buildMenu(inviteMenuFile);
        inviteMenuType = inviteMenuTemplate.getMenuName();
        YamlConfiguration inviteYml = YamlConfiguration.loadConfiguration(inviteMenuFile);
        for (String key : inviteYml.getConfigurationSection("Buttons").getKeys(false)) {
            inviterButtonMap.put(key, new PlayerButton(inviteYml.getConfigurationSection("Buttons." + key)));
        }

        File playerListMenuFile = new File(plugin.getDataFolder(), "player-list-menu.yml");
        if (!playerListMenuFile.exists()) {
            plugin.saveResource("player-list-menu.yml", false);
        }
        playerListMenuTemplate = MenuUtil.buildMenu(playerListMenuFile);
        playerListMenuType = playerListMenuTemplate.getMenuName();
        YamlConfiguration playerListYml = YamlConfiguration.loadConfiguration(playerListMenuFile);
        for (String key : playerListYml.getConfigurationSection("Buttons").getKeys(false)) {
            playerListButtonMap.put(key, new PlayerButton(playerListYml.getConfigurationSection("Buttons." + key)));
        }
    }

    @Override
    public void setContextHolder(@NotNull ContextHolder contextHolder) {
        this.plugin = (NuStarParty) contextHolder;
    }
}

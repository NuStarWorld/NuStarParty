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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import team.idealstate.sugar.next.context.annotation.component.Subscriber;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.service.PartyService;

/**
 * @author NuStar
 * @since 2025/7/20 21:21
 */
@Subscriber
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
public class PlayerSubscriber implements Listener {

    private volatile PartyService service;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        Entity entity = event.getEntity();
        if (!(damagerEntity instanceof Player) || !(entity instanceof Player)) return;
        Player damager = (Player) damagerEntity;
        Player player = (Player) entity;
        if (service.isInSameParty(damager.getUniqueId(), player.getUniqueId())) {
            event.setDamage(0);
            event.setCancelled(true);
        }
    }

    @Autowired
    public void setService(PartyService service) {
        this.service = service;
    }
}

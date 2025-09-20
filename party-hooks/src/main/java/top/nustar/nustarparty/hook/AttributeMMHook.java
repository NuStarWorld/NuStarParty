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

package top.nustar.nustarparty.hook;

import me.monsterxz.monsterapi.attributeskill.api.event.DamageAPEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import team.idealstate.sugar.next.context.annotation.component.Subscriber;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.service.PartyService;

/**
 * @author NuStar<br>
 * @since 2025/8/28 22:37<br>
 */
@Subscriber
@DependsOn(
        classes = "me.monsterxz.monsterapi.attributeskill.api.event.DamageAPEvent",
        properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
@SuppressWarnings("unused")
public class AttributeMMHook implements Listener {
    private volatile PartyService service;

    @EventHandler
    public void on(DamageAPEvent.Attack event) {
        LivingEntity attacker = event.getAttacker();
        LivingEntity entity = event.getEntity();
        if (service.isInSameParty(attacker.getUniqueId(), entity.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @Autowired
    public void setService(PartyService service) {
        this.service = service;
    }
}

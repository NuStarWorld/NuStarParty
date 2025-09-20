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

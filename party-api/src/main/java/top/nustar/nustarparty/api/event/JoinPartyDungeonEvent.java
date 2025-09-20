package top.nustar.nustarparty.api.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import top.nustar.nustarparty.api.entity.Party;

/**
 * @author NuStar<br>
 * @since 2025/9/20 21:40<br>
 * 中途加入地牢事件
 */
public abstract class JoinPartyDungeonEvent extends Event {
    @Getter
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Pre extends JoinPartyDungeonEvent implements Cancellable {
        private final Player player;
        private final Party party;
        @Setter
        private boolean cancelled = false;
    }

    @Getter
    @RequiredArgsConstructor
    public static class After extends JoinPartyDungeonEvent {
        private final Player player;
        private final Party party;
    }
}

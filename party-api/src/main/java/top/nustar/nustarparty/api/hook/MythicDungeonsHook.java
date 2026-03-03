package top.nustar.nustarparty.api.hook;

import top.nustar.nustarparty.api.entity.Party;

/**
 * @author NuStar
 * @since 2026/3/3 21:14
 */
public interface MythicDungeonsHook {

    /**
     * 通知神话副本队伍创建了
     * @param party 插件原始队伍
     * @return 代理后的队伍
     */
    Party triggerMythicDungeonParty(Party party);
}

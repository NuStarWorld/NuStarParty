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

package top.nustar.nustarparty.core.controller;

import static top.nustar.nustarparty.core.utils.PlayerUtil.getPlayer;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import team.idealstate.sugar.logging.Log;
import team.idealstate.sugar.next.command.Command;
import team.idealstate.sugar.next.command.CommandContext;
import team.idealstate.sugar.next.command.CommandResult;
import team.idealstate.sugar.next.command.annotation.CommandArgument;
import team.idealstate.sugar.next.command.annotation.CommandArgument.ConverterResult;
import team.idealstate.sugar.next.command.annotation.CommandHandler;
import team.idealstate.sugar.next.command.exception.CommandArgumentConversionException;
import team.idealstate.sugar.next.context.Bean;
import team.idealstate.sugar.next.context.Context;
import team.idealstate.sugar.next.context.annotation.component.Controller;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import team.idealstate.sugar.next.context.aware.ContextAware;
import team.idealstate.sugar.next.context.lifecycle.Initializable;
import team.idealstate.sugar.validate.Validation;
import team.idealstate.sugar.validate.annotation.NotNull;
import top.nustar.nustarparty.NuStarParty;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.entity.component.InviteApplication;
import top.nustar.nustarparty.api.language.PartyLanguage;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;
import top.nustar.nustarparty.api.service.PartyService;
import top.nustar.nustarparty.core.configuration.PartyConfiguration;
import top.nustar.nustarparty.core.factory.PartyMenuFactory;
import top.nustar.nustarparty.core.service.PartyServiceImpl;
import top.nustar.nustarparty.core.subscriber.MenuSubscriber;

/**
 * @author NuStar
 * @since 2025/6/16 22:51
 */
@Controller(name = "nustarparty")
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
@SuppressWarnings("unused")
public class PartyController implements Command, ContextAware, Initializable {
    private volatile PartyService partyService;
    private volatile Context context;

    @CommandHandler(value = "reload", permission = "NuStarPartyAdminPerm")
    @NotNull
    public CommandResult reload() {
        try {
            Bean<PartyConfiguration> configurationBean = context.getBean(PartyConfiguration.class);
            Validation.notNull(configurationBean, "configurationBean cannot be null");
            assert configurationBean != null;
            ((PartyServiceImpl) partyService).setPartyConfiguration(configurationBean.getInstance());

            Bean<PartyLanguage> languageBean = context.getBean(PartyLanguage.class);
            Validation.notNull(languageBean, "languageBean cannot be null");
            assert languageBean != null;
            ((PartyServiceImpl) partyService).setPartyLanguage(languageBean.getInstance());

            Bean<PartyMenuFactory> menuFactoryBean = context.getBean(PartyMenuFactory.class);
            Validation.notNull(menuFactoryBean, "menuFactoryBean cannot be null");
            assert menuFactoryBean != null;
            ((PartyServiceImpl) partyService).setPartyMenuFactory(menuFactoryBean.getInstance());

            Bean<MenuSubscriber> menuSubscriberBean = context.getBean(MenuSubscriber.class);
            assert menuSubscriberBean != null;
            menuSubscriberBean.getInstance().setPartyMenuFactory(menuFactoryBean.getInstance());
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure("重载失败，请查看后台日志输出");
        }
        return CommandResult.success("重载成功");
    }

    @NotNull
    @CommandHandler(value = "help", permission = "NuStarPartyAdminPerm")
    public CommandResult help(@NotNull CommandContext context) {
        UUID uniqueId = context.getSender().getUniqueId();
        Player player = Bukkit.getPlayer(uniqueId);
        if (player == null) return CommandResult.failure();

        // 标题分隔线
        player.sendMessage("§6§l◆========== §e队伍系统帮助 §6§l==========◆");

        // 命令列表
        player.sendMessage(formatCommand("/nsparty help", "显示本帮助"));
        player.sendMessage(formatCommand("/nsparty reload", "重载插件配置"));
        player.sendMessage(formatCommand("/nsparty openPartyMenu", "打开公共队伍菜单"));
        player.sendMessage(formatCommand("/nsparty openMyPartyMenu", "打开我的队伍管理"));
        player.sendMessage(formatCommand("/nsparty openJoinApplicationMenu", "打开队伍申请列表菜单"));
        player.sendMessage(formatCommand("/nsparty openInviteMenu", "打开邀请列表菜单"));
        player.sendMessage(formatCommand("/nsparty openPlayerListMenu", "打开玩家列表菜单"));
        player.sendMessage(formatCommand("/nsparty createParty", "创建新队伍"));
        player.sendMessage(formatCommand("/nsparty quitParty", "退出当前队伍"));
        player.sendMessage(formatCommand("/nsparty disband", "解散当前队伍"));
        player.sendMessage(formatCommand("/nsparty invitePlayer <玩家> <理由>", "邀请玩家加入"));
        player.sendMessage(formatCommand("/nsparty kickMember <玩家> <理由>", "踢出指定成员"));
        player.sendMessage(formatCommand("/nsparty acceptJoinRequest <玩家>", "接受加入请求"));

        // 底部提示
        player.sendMessage("§7提示: §8< > §7表示必填参数，命令区分大小写");
        return CommandResult.success();
    }

    private String formatCommand(String cmd, String description) {
        return "§a" + cmd + " §8┃ §7" + description;
    }

    @NotNull
    @CommandHandler(value = "openPlayerListMenu", permission = "NuStarPartyPlayerPerm")
    public CommandResult openPlayerListMenu(@NotNull CommandContext context) {
        try {
            getPlayer(context.getSender()).ifPresent(player -> partyService.openPlayerListMenu(player));
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure(String.format("打开队伍玩家列表菜单失败: %s", e.getMessage()));
        }
        return CommandResult.success();
    }

    @NotNull
    @CommandHandler(value = "openJoinApplicationMenu", permission = "NuStarPartyPlayerPerm")
    public CommandResult openPartyJoinApplicationMenu(@NotNull CommandContext context) {
        try {
            getPlayer(context.getSender()).ifPresent(player -> partyService.openJoinApplicationMenu(player));
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure(String.format("打开队伍申请列表菜单失败: %s", e.getMessage()));
        }
        return CommandResult.success();
    }

    @NotNull
    @CommandHandler(value = "openInviteMenu", permission = "NuStarPartyPlayerPerm")
    public CommandResult openInviteMenu(@NotNull CommandContext context) {
        try {
            getPlayer(context.getSender()).ifPresent(player -> partyService.openInviteApplicationMenu(player));
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure(String.format("打开邀请列表菜单失败: %s", e.getMessage()));
        }
        return CommandResult.success();
    }

    @NotNull
    @CommandHandler(value = "openPartyMenu", permission = "NuStarPartyPlayerPerm")
    public CommandResult openPartyMenu(@NotNull CommandContext context) {
        try {
            getPlayer(context.getSender()).ifPresent(player -> partyService.openPartyMenu(player));
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure(String.format("打开队伍菜单失败: %s", e.getMessage()));
        }
        return CommandResult.success();
    }

    @CommandHandler(value = "openMyPartyMenu", permission = "NuStarPartyPlayerPerm")
    public CommandResult openMyPartyMenu(@NotNull CommandContext context) {
        try {
            getPlayer(context.getSender()).ifPresent(player -> partyService.openMyPartyMenu(player));
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure(String.format("打开我的队伍菜单失败: %s", e.getMessage()));
        }
        return CommandResult.success();
    }

    @CommandHandler(value = "acceptJoinRequest {request}", permission = "NuStarPartyPlayerPerm")
    @NotNull
    public CommandResult acceptJoinRequest(
            @NotNull CommandContext context,
            @NotNull @CommandArgument(completer = "completeJoinRequest") String request) {
        try {
            getPlayer(context.getSender()).ifPresent(player -> partyService.acceptJoinApplication(player, request));
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure(String.format("接受加入请求失败: %s", request));
        }
        return CommandResult.success();
    }

    @NotNull
    @CommandHandler(value = "kickMember {memberName} {kickReason}", permission = "NuStarPartyPlayerPerm")
    public CommandResult kickMember(
            @NotNull CommandContext context,
            @NotNull @CommandArgument(completer = "completeMemberName") String memberName,
            @NotNull @CommandArgument() String kickReason) {
        try {
            getPlayer(context.getSender()).ifPresent(player -> partyService.kickMember(player, memberName, kickReason));
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure(String.format("踢人失败: %s", memberName));
        }
        return CommandResult.success();
    }

    @NotNull
    @CommandHandler(value = "disband", permission = "NuStarPartyPlayerPerm")
    public CommandResult disband(@NotNull CommandContext context) {
        try {
            return getPlayer(context.getSender())
                    .filter(player -> partyService.disbandParty(player))
                    .map(player -> CommandResult.success())
                    .orElse(CommandResult.failure());
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure("解散队伍失败");
        }
    }

    @NotNull
    @CommandHandler(value = "quitParty", permission = "NuStarPartyPlayerPerm")
    public CommandResult quitParty(@NotNull CommandContext context) {
        try {
            return getPlayer(context.getSender())
                    .filter(player -> partyService.quitParty(player))
                    .map(player -> CommandResult.success())
                    .orElse(CommandResult.failure());
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure("退出队伍失败");
        }
    }

    @NotNull
    @CommandHandler(value = "createParty", permission = "NuStarPartyPlayerPerm")
    public CommandResult createParty(@NotNull CommandContext context) {
        try {
            return getPlayer(context.getSender())
                    .filter(player -> partyService.createParty(player))
                    .map(player -> CommandResult.success())
                    .orElse(CommandResult.failure());
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure("创建队伍失败");
        }
    }

    @NotNull
    @CommandHandler(value = "invitePlayer {invitedPlayer} {inviteReason}", permission = "NuStarPartyPlayerPerm")
    public CommandResult invitePlayer(
            @NotNull CommandContext context,
            @NotNull @CommandArgument(completer = "completePlayer", converter = "convertToPlayer") Player invitedPlayer,
            @NotNull @CommandArgument() String inviteReason) {
        try {
            return getPlayer(context.getSender())
                    .filter(player -> partyService.invitePlayer(player, invitedPlayer, inviteReason))
                    .map(player -> CommandResult.success())
                    .orElse(CommandResult.failure());
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure("邀请玩家失败");
        }
    }

    @NotNull
    @CommandHandler(value = "acceptInviteRequest {inviterName}", permission = "NuStarPartyPlayerPerm")
    public CommandResult acceptInviteRequest(
            @NotNull CommandContext context,
            @NotNull @CommandArgument(completer = "completeInviteRequestName") String inviterName) {
        try {
            return getPlayer(context.getSender())
                    .filter(player -> partyService.acceptInviteApplication(player, inviterName))
                    .map(player -> CommandResult.success())
                    .orElse(CommandResult.failure());
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure("接受邀请请求失败");
        }
    }

    @NotNull
    @CommandHandler(value = "refuseInviteRequest {inviterName}", permission = "NuStarPartyPlayerPerm")
    public CommandResult refuseInviteRequest(
            @NotNull CommandContext context,
            @NotNull @CommandArgument(completer = "completeInviteRequestName") String inviterName) {
        try {
            return getPlayer(context.getSender())
                    .filter(player -> partyService.refuseInviteApplication(player, inviterName))
                    .map(player -> CommandResult.success())
                    .orElse(CommandResult.failure());
        } catch (Throwable e) {
            Log.error(e);
            return CommandResult.failure("拒绝邀请请求失败");
        }
    }

    @NotNull
    public ConverterResult<Player> convertToPlayer(
            @NotNull CommandContext context, @NotNull String argument, boolean onConversion)
            throws CommandArgumentConversionException {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        boolean canBeConvert =
                !players.isEmpty() && players.stream().map(Player::getName).anyMatch(name -> name.equals(argument));
        if (!onConversion) {
            return canBeConvert ? ConverterResult.success() : ConverterResult.failure();
        }
        if (!canBeConvert) {
            throw new CommandArgumentConversionException(String.format("参数 '%s' 无法转换到 player.", argument));
        }
        return ConverterResult.success(Bukkit.getPlayer(argument));
    }

    @NotNull
    public List<String> completePlayer(@NotNull CommandContext context, @NotNull String argument) {
        Optional<Player> sender = getPlayer(context.getSender());
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        if (argument.isEmpty()) {
            return Arrays.stream(players)
                    .map(Player::getName)
                    .filter(name ->
                            !sender.isPresent() || !sender.get().getName().equals(name))
                    .collect(Collectors.toList());
        }
        return players.length == 0
                ? Collections.emptyList()
                : Arrays.stream(players)
                        .map(Player::getName)
                        .filter(name ->
                                !sender.isPresent() || !sender.get().getName().equals(name))
                        .filter(name -> name.startsWith(argument))
                        .collect(Collectors.toList());
    }

    @NotNull
    public List<String> completeJoinRequest(@NotNull CommandContext context, @NotNull String argument) {
        return completePlayerNames(context, argument, Party::getJoinApplicationList);
    }

    @NotNull
    public List<String> completeMemberName(@NotNull CommandContext context, @NotNull String argument) {
        return completePlayerNames(context, argument, Party::getMembers);
    }

    @NotNull
    public List<String> completeInviteRequestName(@NotNull CommandContext context, @NotNull String argument) {
        return getPlayer(context.getSender())
                .flatMap(player -> partyService.getPlayerInvite(player.getUniqueId()))
                .map(invite -> invite.getInviteApplications().stream()
                        .map(InviteApplication::getInviter)
                        .map(OfflinePlayer::getName)
                        .filter(Objects::nonNull)
                        .filter(name -> name.startsWith(argument))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @NotNull
    private List<String> completePlayerNames(
            @NotNull CommandContext context,
            @NotNull String argument,
            @NotNull Function<Party, List<OfflinePlayer>> membersExtractor) {
        Player leader = Bukkit.getPlayer(context.getSender().getUniqueId());
        return partyService
                .getParty(leader.getUniqueId())
                .map(party -> {
                    List<OfflinePlayer> players = membersExtractor.apply(party);
                    if (argument.isEmpty()) {
                        return players.stream().map(OfflinePlayer::getName).collect(Collectors.toList());
                    }
                    return players.isEmpty()
                            ? Collections.<String>emptyList()
                            : players.stream()
                                    .map(OfflinePlayer::getName)
                                    .filter(name -> name.startsWith(argument))
                                    .collect(Collectors.toList());
                })
                .orElse(Collections.emptyList());
    }

    @Autowired
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Override
    public void setContext(@NotNull Context context) {
        this.context = context;
    }

    @Override
    public void initialize() {
        NuStarParty holder = (NuStarParty) context.getHolder();
        Metrics metrics = new Metrics(holder, 26855);
        if (holder.getServer().getPluginManager().isPluginEnabled("NuStarCoreBridge")) {
            Metrics coreBridgeMetrics = new Metrics(holder, 26856);
            coreBridgeMetrics.addCustomChart(new SimplePie("usedplugin", () -> "NuStarParty"));
        }
    }
}

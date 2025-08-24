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

import static team.idealstate.sugar.next.function.Functional.functional;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import team.idealstate.sugar.logging.Log;
import team.idealstate.sugar.next.context.annotation.component.Service;
import team.idealstate.sugar.next.context.annotation.feature.Autowired;
import team.idealstate.sugar.next.context.annotation.feature.Scope;
import team.idealstate.sugar.validate.Validation;
import top.nustar.nustarparty.api.entity.Invite;
import top.nustar.nustarparty.api.entity.Party;
import top.nustar.nustarparty.api.entity.component.InviteApplication;
import top.nustar.nustarparty.api.event.*;
import top.nustar.nustarparty.api.language.PartyLanguage;
import top.nustar.nustarparty.api.service.PartyService;
import top.nustar.nustarparty.core.configuration.PartyConfiguration;
import top.nustar.nustarparty.core.entity.PartyImpl;
import top.nustar.nustarparty.core.factory.PartyMenuFactory;
import top.nustar.nustarparty.core.manager.InviteManager;
import top.nustar.nustarparty.core.manager.PartyManager;

/**
 * @author NuStar
 * @since 2025/6/14 21:53
 */
@Service
@Scope(Scope.SINGLETON)
@SuppressWarnings("unused")
public class PartyServiceImpl implements PartyService {
    private volatile PartyManager partyManager;
    private volatile InviteManager inviteManager;
    private volatile PartyMenuFactory partyMenuFactory;
    private volatile PartyLanguage partyLanguage;
    private volatile PartyConfiguration partyConfiguration;

    @Override
    public Optional<Party> getParty(UUID playerUUID) {
        Optional<Party> partyOptional = partyManager.getParty(playerUUID);
        return partyOptional.map(Party::copy);
    }

    @Override
    public List<Party> getPartyList() {
        return partyManager.getPartyList().stream().map(Party::copy).collect(Collectors.toList());
    }

    @Override
    public void openPartyMenu(Player player) {
        partyMenuFactory.createPartyMenu(player).open(player);
    }

    @Override
    public void openMyPartyMenu(Player player) {
        Optional<Party> partyOptional = getParty(player.getUniqueId());
        if (partyOptional.isPresent()) {
            partyMenuFactory.createMyPartyMenu(partyOptional.get(), player).open(player);
        } else {
            functional(player).run(it -> partyLanguage.getOnNotJoinParty().use(it::sendMessage));
        }
    }

    @Override
    public void openJoinApplicationMenu(Player player) {
        Optional<Party> partyOptional = getParty(player.getUniqueId());
        if (partyOptional.isPresent()) {
            Party party = partyOptional.get();
            if (!party.getLeader().getUniqueId().equals(player.getUniqueId())) {
                functional(player).run(it -> partyLanguage
                        .getOnNonLeaderOpenJoinApplicationMenu()
                        .use(it::sendMessage));
                return;
            }
            partyMenuFactory.createJoinApplicationMenu(player, party).open(player);
        } else {
            functional(player).run(it -> partyLanguage.getOnPartyNotExist().use(it::sendMessage));
        }
    }

    @Override
    public void openInviteApplicationMenu(Player player) {
        partyMenuFactory.createInviteMenu(player).open(player);
    }

    @Override
    public void openPlayerListMenu(Player player) {
        partyMenuFactory.createPlayerListMenu(player).open(player);
    }

    @Override
    public boolean createParty(Player player) {
        Log.debug(player.getName() + "尝试创建队伍");
        Optional<Party> partyOptional = partyManager.getParty(player.getUniqueId());
        if (partyOptional.isPresent()) {
            partyLanguage.getOnPartyAlreadyExist().use(player::sendMessage);
            return false;
        }
        CreatePartyEvent.Pre createPartyEvent = new CreatePartyEvent.Pre(player);
        Bukkit.getPluginManager().callEvent(createPartyEvent);
        if (createPartyEvent.isCancelled()) return false;
        PartyImpl createParty = partyManager.createParty(player, partyConfiguration.getPartyMaxSize());
        Bukkit.getPluginManager().callEvent(new CreatePartyEvent.After(player, createParty));
        partyLanguage.getOnCreateParty().use(player::sendMessage);
        return true;
    }

    @Override
    public boolean addJoinPartyRequest(Player player, UUID partyUUID) {
        Optional<Party> partyOptional = partyManager.getParty(partyUUID);
        if (!partyOptional.isPresent()) return false;
        Party party = partyOptional.get();
        Map<String, Object> variables = new HashMap<>();
        variables.put("leader", party.getLeader().getName());
        variables.put("player", player.getName());
        if (party.isMember(player.getUniqueId())
                || getParty(player.getUniqueId()).isPresent()) {
            functional(player).run(it -> partyLanguage.getOnPartyAlreadyJoined().use(it::sendMessage));
            return false;
        }
        if (party.isFull()) {
            functional(player).run(it -> partyLanguage.getOnPartyFull().use(variables, it::sendMessage));
            return false;
        }

        if (party.getJoinApplicationList().stream()
                .anyMatch(offlinePlayer -> offlinePlayer.getUniqueId().equals(player.getUniqueId()))) {
            functional(player).run(it -> partyLanguage
                    .getOnJoinPartyApplicationAlreadyExist()
                    .use(it::sendMessage));
            return false;
        }

        AddJoinApplicationEvent.Pre addJoinRequestEvent = new AddJoinApplicationEvent.Pre(player, party);
        Bukkit.getPluginManager().callEvent(addJoinRequestEvent);
        if (addJoinRequestEvent.isCancelled()) return false;
        partyManager.updateParty(partyUUID, playerParty -> party.addJoinApplication(player));
        Bukkit.getPluginManager().callEvent(new AddJoinApplicationEvent.After(player, party));

        functional(player).run(it -> partyLanguage.getOnJoinPartyApplication().use(variables, it::sendMessage));
        functional(party.getLeader())
                .convert(OfflinePlayer::getPlayer)
                .when(Objects::nonNull)
                .run(it -> partyLanguage.getOnSomeoneJoinPartyApplication().use(variables, it::sendMessage));
        return true;
    }

    @Override
    public boolean quitParty(Player member) {
        Optional<Party> partyOptional = partyManager.getParty(member.getUniqueId());
        if (!partyOptional.isPresent()) return false;
        Party party = partyOptional.get();
        if (party.isLeader(member.getUniqueId())) {
            functional(member).run(it -> partyLanguage.getOnLeaderQuitParty().use(it::sendMessage));
            return false;
        }

        QuitPartyEvent.Pre quitPartyPreEvent = new QuitPartyEvent.Pre(member, party);
        Bukkit.getPluginManager().callEvent(quitPartyPreEvent);
        if (quitPartyPreEvent.isCancelled()) return false;
        partyManager.updateParty(party.getPartyUUID(), playerParty -> playerParty.removeMember(member));
        partyManager.removePlayerParty(member.getUniqueId());

        Map<String, Object> variables =
                Collections.singletonMap("leader", party.getLeader().getName());
        functional(member).run(it -> partyLanguage.getOnQuitParty().use(variables, it::sendMessage));
        broadcastMessage(
                party, partyLanguage.getOnMemberQuitParty(), Collections.singletonMap("member", member.getName()));
        Bukkit.getPluginManager().callEvent(new QuitPartyEvent.After(member, party));
        return true;
    }

    @Override
    public boolean addPartyMember(OfflinePlayer leader, UUID memberUUID) {
        Optional<Party> partyOptional = partyManager.getParty(leader.getUniqueId());
        if (!partyOptional.isPresent()) return false;
        Party party = partyOptional.get();
        if (party.isFull()) {
            functional(leader)
                    .convert(OfflinePlayer::getPlayer)
                    .when(Objects::nonNull)
                    .run(it -> partyLanguage.getOnPartyFull().use(it::sendMessage));
            return false;
        }
        if (party.isMember(memberUUID) || partyManager.getParty(memberUUID).isPresent()) {
            functional(memberUUID)
                    .convert(Bukkit::getPlayer)
                    .when(Objects::nonNull)
                    .run(it -> partyLanguage.getOnPartyAlreadyJoined().use(it::sendMessage));
            return false;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUUID);
        broadcastMessage(
                party,
                partyLanguage.getOnSomeoneJoinedParty(),
                Collections.singletonMap("member", offlinePlayer.getName()));

        partyManager.updateParty(leader.getUniqueId(), playerParty -> party.addMember(offlinePlayer));
        partyManager.setPlayerParty(offlinePlayer, party);

        functional(offlinePlayer)
                .convert(OfflinePlayer::getPlayer)
                .when(Objects::nonNull)
                .run(it -> partyLanguage
                        .getOnJoinParty()
                        .use(Collections.singletonMap("leader", leader.getName()), it::sendMessage));
        return true;
    }

    @Override
    public boolean disbandParty(Player leader) {
        Log.debug(leader.getName() + "尝试解散队伍");
        Optional<Party> partyOptional = partyManager.getParty(leader.getUniqueId());
        if (!partyOptional.isPresent()) return false;
        Party party = partyOptional.get();
        if (!party.isLeader(leader.getUniqueId())) {
            functional(leader)
                    .run(it -> partyLanguage.getOnNonLeaderDisbandParty().use(it::sendMessage));
            return false;
        }
        DisbandPartyEvent.Pre disbandPartyPreEvent = new DisbandPartyEvent.Pre(leader, party);
        Bukkit.getPluginManager().callEvent(disbandPartyPreEvent);
        if (disbandPartyPreEvent.isCancelled()) return false;
        partyManager.deleteParty(leader.getUniqueId());
        functional(leader).run(it -> partyLanguage.getOnPartyDisband().use(it::sendMessage));
        broadcastMessage(
                party,
                partyLanguage.getOnLeaderDisbandParty(),
                Collections.singletonMap("leader", leader.getName()),
                leader.getUniqueId());
        Bukkit.getPluginManager().callEvent(new DisbandPartyEvent.After(leader, party));
        return true;
    }

    @Override
    public boolean kickMember(Player source, UUID memberUUID, String reason) {
        return kickMember(source, offlinePlayer -> offlinePlayer.getUniqueId().equals(memberUUID), reason);
    }

    @Override
    public boolean kickMember(Player source, String memberName, String reason) {
        return kickMember(source, offlinePlayer -> offlinePlayer.getName().equals(memberName), reason);
    }

    @Override
    public boolean invitePlayer(Player member, Player invitedPlayer, String inviteReason) {
        if (member.equals(invitedPlayer)) return false;
        Optional<Party> partyOptional = partyManager.getParty(member.getUniqueId());
        if (!partyOptional.isPresent()) {
            functional(member).run(it -> partyLanguage.getOnPartyNotExist().use(it::sendMessage));
            return false;
        }

        Optional<Invite> inviteOptional = inviteManager.getInvite(invitedPlayer.getUniqueId());
        boolean applicationAlreadyExist = inviteOptional
                .map(invite -> invite.getInviteApplication(member.getUniqueId()).isPresent())
                .orElse(false);
        Map<String, Object> variables = new HashMap<>();
        variables.put("invited", invitedPlayer.getName());
        if (applicationAlreadyExist) {
            functional(member)
                    .run(it ->
                            partyLanguage.getOnInviteApplicationAlreadyExist().use(variables, it::sendMessage));
            return false;
        }

        Party party = partyOptional.get();
        if (party.isMember(invitedPlayer.getUniqueId())) {
            functional(member)
                    .run(it -> partyLanguage.getOnInviteAlreadyJoinedParty().use(variables, it::sendMessage));
            return false;
        }

        inviteManager.updateInvite(
                invitedPlayer.getUniqueId(), invite -> invite.addInviteRequest(party, member, inviteReason));

        variables.put("inviter", member.getName());
        variables.put("leader", party.getLeader().getName());
        functional(member).run(it -> partyLanguage.getOnInviteJoinParty().use(variables, it::sendMessage));
        functional(invitedPlayer)
                .run(it -> partyLanguage.getOnSomeoneInviteJoinParty().use(variables, it::sendMessage));
        return true;
    }

    @Override
    public boolean acceptInviteApplication(Player player, String inviterName) {
        return handleInviteApplication(
                player,
                inviteApplication -> inviteApplication.getInviter().getName().equals(inviterName),
                true);
    }

    @Override
    public boolean acceptInviteApplication(Player player, UUID inviterUid) {
        return handleInviteApplication(
                player,
                inviteApplication ->
                        inviteApplication.getInviter().getUniqueId().equals(inviterUid),
                true);
    }

    @Override
    public boolean refuseInviteApplication(Player player, String inviterName) {
        return handleInviteApplication(
                player,
                inviteApplication -> inviteApplication.getInviter().getName().equals(inviterName),
                false);
    }

    @Override
    public boolean refuseInviteApplication(Player player, UUID inviterUid) {
        return handleInviteApplication(
                player,
                inviteApplication ->
                        inviteApplication.getInviter().getUniqueId().equals(inviterUid),
                false);
    }

    @Override
    public Optional<Invite> getPlayerInvite(UUID playerUUID) {
        Optional<Invite> inviteOptional = inviteManager.getInvite(playerUUID);
        return inviteOptional.map(Invite::copy);
    }

    @Override
    public boolean acceptJoinApplication(Player leader, UUID requesterUUID) {
        return handleJoinApplication(
                leader, offlinePlayer -> offlinePlayer.getUniqueId().equals(requesterUUID), true);
    }

    @Override
    public boolean acceptJoinApplication(Player leader, String requesterName) {
        return handleJoinApplication(
                leader, offlinePlayer -> offlinePlayer.getName().equals(requesterName), true);
    }

    @Override
    public boolean refuseJoinApplication(Player leader, UUID requesterUUID) {
        return handleJoinApplication(
                leader, offlinePlayer -> offlinePlayer.getUniqueId().equals(requesterUUID), false);
    }

    @Override
    public boolean refuseJoinApplication(Player leader, String requesterName) {
        return handleJoinApplication(
                leader, offlinePlayer -> offlinePlayer.getName().equals(requesterName), false);
    }

    @Override
    public boolean isInSameParty(UUID playerUUID1, UUID playerUUID2) {
        Optional<Party> partyOptional1 = partyManager.getParty(playerUUID1);
        Optional<Party> partyOptional2 = partyManager.getParty(playerUUID2);
        if (!partyOptional1.isPresent() || !partyOptional2.isPresent()) return false;
        return partyOptional1.get().equals(partyOptional2.get());
    }

    @Override
    public List<Player> getNonPartyPlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> !partyManager.getParty(player.getUniqueId()).isPresent())
                .collect(Collectors.toList());
    }

    private boolean handleInviteApplication(Player player, Predicate<InviteApplication> matcher, boolean isAccept) {
        Optional<Invite> inviteOptional = inviteManager.getInvite(player.getUniqueId());
        Map<String, Object> variables = new HashMap<>();
        variables.put("invited", player.getName());
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Invite inviteImpl = inviteOptional.get();
        Optional<InviteApplication> applicantOptional = inviteImpl.popInviteRequest(matcher, isAccept);
        if (!applicantOptional.isPresent()) {
            return false;
        }
        OfflinePlayer inviter = applicantOptional.get().getInviter();
        variables.put("inviter", inviter.getName());
        if (!isAccept) {
            functional(player)
                    .run(it -> partyLanguage.getOnInviteJoinPartyRefuse().use(variables, it::sendMessage));
            functional(inviter)
                    .convert(OfflinePlayer::getPlayer)
                    .when(Objects::nonNull)
                    .run(it -> partyLanguage.getOnSomeoneInviteJoinPartyRefuse().use(variables, it::sendMessage));
            return true;
        }
        InviteApplication inviteApplication = applicantOptional.get();
        Party inviteParty = inviteApplication.getInviteParty();
        if (inviteApplication.getInviter().getUniqueId().equals(inviteParty.getLeaderUid())) {
            if (!addPartyMember(inviteParty.getLeader(), player.getUniqueId())) return false;
        } else {
            if (!addJoinPartyRequest(player, inviteParty.getPartyUUID())) return false;
        }
        functional(player).run(it -> partyLanguage.getOnInviteJoinPartyAccept().use(variables, it::sendMessage));
        return true;
    }

    private boolean kickMember(Player source, Predicate<OfflinePlayer> matcher, String reason) {
        Optional<Party> partyOptional = partyManager.getParty(source.getUniqueId());
        if (!partyOptional.isPresent()) return false;
        Party party = partyOptional.get();

        if (!party.isLeader(source.getUniqueId())) {
            functional(source).run(it -> partyLanguage.getOnKickPartyFail().use(it::sendMessage));
            return false;
        }

        OfflinePlayer offlineKickedPlayer = party.removeMember(matcher);

        if (offlineKickedPlayer == null) {
            functional(source)
                    .run(it -> partyLanguage.getOnKickPartyNotInParty().use(it::sendMessage));
            return false;
        }
        partyManager.removePlayerParty(offlineKickedPlayer.getUniqueId());

        Log.debug(source.getName() + "踢出玩家 " + offlineKickedPlayer.getName());
        Map<String, Object> variables = new HashMap<>();
        variables.put("source", source.getName());
        variables.put("leader", party.getLeader().getName());
        variables.put("member", offlineKickedPlayer.getName());
        variables.put("reason", reason);

        functional(offlineKickedPlayer)
                .convert(OfflinePlayer::getPlayer)
                .when(Objects::nonNull)
                .run(it -> partyLanguage.getOnKickedOutParty().use(variables, it::sendMessage));

        broadcastMessage(party, partyLanguage.getOnKickParty(), variables);
        Bukkit.getPluginManager().callEvent(new KickPartyEvent(offlineKickedPlayer, source, party));
        return true;
    }

    private boolean handleJoinApplication(Player leader, Predicate<OfflinePlayer> matcher, boolean isAccept) {
        Optional<Party> partyOptional = partyManager.getParty(leader.getUniqueId());
        if (!partyOptional.isPresent()) return false;
        Party party = partyOptional.get();
        Validation.notNull(party, leader.getName() + "'s party is null");

        Map<String, Object> variables = new HashMap<>();
        variables.put("leader", leader.getName());

        if (party.isFull()) {
            functional(leader).run(it -> partyLanguage.getOnPartyFull().use(variables, it::sendMessage));
            return false;
        }

        Optional<OfflinePlayer> applicantOptional = party.popJoinApplicant(matcher, isAccept);
        if (!applicantOptional.isPresent()) {
            return false;
        }

        OfflinePlayer applicant = applicantOptional.get();
        variables.put("applicant", applicant.getName());
        if (!isAccept) {
            broadcastMessage(party, partyLanguage.getOnJoinPartyRefuse(), variables);
            functional(applicant)
                    .convert(OfflinePlayer::getPlayer)
                    .when(Objects::nonNull)
                    .run(it -> partyLanguage.getOnPartyRefuseJoinApplication().use(variables, it::sendMessage));
            return true;
        }

        partyManager.setPlayerParty(applicant, party);
        functional(applicant)
                .convert(OfflinePlayer::getPlayer)
                .when(Objects::nonNull)
                .run(it -> functional(it)
                        .run(it1 -> partyLanguage.getOnJoinPartyAccept().use(variables, it::sendMessage)));
        broadcastMessage(
                party,
                partyLanguage.getOnSomeoneJoinedParty(),
                Collections.singletonMap("member", applicant.getName()),
                applicant.getUniqueId());
        return true;
    }

    private static Player getPlayerSafe(String input) {
        try {
            UUID uuid = UUID.fromString(input);
            return Bukkit.getPlayer(uuid);
        } catch (IllegalArgumentException e) {
            return Bukkit.getPlayer(input);
        }
    }

    private void broadcastMessage(
            Party party, PartyLanguage.Language language, Map<String, Object> variables, UUID... removedPlayerUUIDs) {
        functional(party.getLeader())
                .convert(OfflinePlayer::getPlayer)
                .when(Objects::nonNull)
                .when(player -> {
                    if (removedPlayerUUIDs == null || removedPlayerUUIDs.length == 0) return true;
                    return Arrays.stream(removedPlayerUUIDs).noneMatch(uuid -> uuid.equals(player.getUniqueId()));
                })
                .run(it -> language.use(variables, it::sendMessage));
        functional(party.getMembers())
                .convert(it -> it.stream()
                        .map(offlinePlayer -> Bukkit.getPlayer(offlinePlayer.getUniqueId()))
                        .filter(Objects::nonNull)
                        .filter(player -> {
                            if (removedPlayerUUIDs == null || removedPlayerUUIDs.length == 0) return true;
                            return Arrays.stream(removedPlayerUUIDs)
                                    .noneMatch(uuid -> uuid.equals(player.getUniqueId()));
                        })
                        .collect(Collectors.toList()))
                .run(it -> it.forEach(onlinePlayer -> language.use(variables, onlinePlayer::sendMessage)));
    }

    @Autowired
    public void setPartyManager(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Autowired
    public void setInviteManager(InviteManager inviteManager) {
        this.inviteManager = inviteManager;
    }

    @Autowired
    public void setPartyLanguage(PartyLanguage partyLanguage) {
        this.partyLanguage = partyLanguage;
    }

    @Autowired
    public void setPartyMenuFactory(PartyMenuFactory partyMenuFactory) {
        this.partyMenuFactory = partyMenuFactory;
    }

    @Autowired
    public void setPartyConfiguration(PartyConfiguration partyConfiguration) {
        this.partyConfiguration = partyConfiguration;
    }

    @Override
    public PartyLanguage getPartyLanguage() {
        return partyLanguage;
    }
}

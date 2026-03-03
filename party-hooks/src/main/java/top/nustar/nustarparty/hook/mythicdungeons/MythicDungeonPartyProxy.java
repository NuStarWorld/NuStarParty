package top.nustar.nustarparty.hook.mythicdungeons;

import net.playavalon.mythicdungeons.api.party.IDungeonParty;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.nustar.nustarparty.api.entity.Party;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author NuStar
 * @since 2026/3/3 21:23
 */
public class MythicDungeonPartyProxy implements IDungeonParty, Party {

    private final Party party;

    public MythicDungeonPartyProxy(Party party) {
        this.party = party;

        initDungeonParty("NuStarParty");
    }

    @Override
    public void addPlayer(Player player) {
        party.addMember(player);
    }

    @Override
    public void removePlayer(Player player) {
        party.removeMember(player);
    }

    @Override
    public List<Player> getPlayers() {
        return party.getMembers().stream().map(OfflinePlayer::getPlayer).collect(Collectors.toList());
    }

    @Override
    public @NotNull Player getLeader() {
        return party.getLeader().getPlayer();
    }

    @Override
    public void addMember(OfflinePlayer member) {
        party.addMember(member);
    }

    @Override
    public void removeMember(OfflinePlayer member) {
        party.removeMember(member);
    }

    @Override
    public OfflinePlayer removeMember(Predicate<OfflinePlayer> matcher) {
        return party.removeMember(matcher);
    }

    @Override
    public boolean joinApplicationExist(Player player) {
        return party.joinApplicationExist(player);
    }

    @Override
    public Optional<OfflinePlayer> popJoinApplicant(Predicate<OfflinePlayer> matcher, boolean isAccept) {
        return party.popJoinApplicant(matcher, isAccept);
    }

    @Override
    public void addJoinApplication(Player player) {
        party.addJoinApplication(player);
    }

    @Override
    public boolean isLeader(UUID playerUUID) {
        return party.isLeader(playerUUID);
    }

    @Override
    public boolean isLeader(String playerName) {
        return party.isLeader(playerName);
    }

    @Override
    public boolean isFull() {
        return party.isFull();
    }

    @Override
    public int getEmptyPositions() {
        return party.getEmptyPositions();
    }

    @Override
    public int getSize() {
        return party.getSize();
    }

    @Override
    public boolean isMember(UUID playerUUID) {
        return party.isMember(playerUUID);
    }

    @Override
    public boolean isMember(String memberName) {
        return party.isMember(memberName);
    }

    @Override
    public Party copy() {
        return party.copy();
    }

    @Override
    public UUID getLeaderUid() {
        return party.getLeaderUid();
    }

    @Override
    public String getLeaderName() {
        return party.getLeaderName();
    }

    @Override
    public UUID getPartyUUID() {
        return party.getPartyUUID();
    }

    @Override
    public String getPartyName() {
        return party.getPartyName();
    }

    @Override
    public List<OfflinePlayer> getMembers() {
        return party.getMembers();
    }

    @Override
    public List<OfflinePlayer> getJoinApplicationList() {
        return party.getJoinApplicationList();
    }

    @Override
    public List<UUID> getMemberUids() {
        return party.getMemberUids();
    }

    @Override
    public List<UUID> getJoinApplicationUids() {
        return party.getJoinApplicationUids();
    }

    @Override
    public void setPartyName(String partyName) {
        party.setPartyName(partyName);
    }

    @Override
    public void setLeader(UUID leader) {
        party.setLeader(leader);
    }

    @Override
    public int getMaxSize() {
        return party.getMaxSize();
    }

    @Override
    public boolean isSendAddJoinApplicationPlaceholder() {
        return party.isSendAddJoinApplicationPlaceholder();
    }

    @Override
    public void setSendAddJoinApplicationPlaceholder(boolean sendAddJoinApplicationPlaceholder) {
        party.setSendAddJoinApplicationPlaceholder(sendAddJoinApplicationPlaceholder);
    }
}

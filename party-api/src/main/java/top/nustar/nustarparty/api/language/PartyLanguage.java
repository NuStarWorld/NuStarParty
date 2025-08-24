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

package top.nustar.nustarparty.api.language;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import lombok.Data;
import team.idealstate.sugar.next.context.Context;
import team.idealstate.sugar.next.context.annotation.component.Configuration;
import team.idealstate.sugar.next.context.annotation.feature.DependsOn;
import team.idealstate.sugar.next.context.annotation.feature.Scope;
import team.idealstate.sugar.next.function.closure.Action;
import team.idealstate.sugar.validate.Validation;
import team.idealstate.sugar.validate.annotation.NotNull;
import top.nustar.nustarparty.api.next.NuStarPartyProperties;

/**
 * @author NuStar
 * @since 2025/6/14 22:04
 */
@Data
@Configuration(uri = "language.yml", release = Context.RESOURCE_EMBEDDED + "language.yml")
@Scope(Scope.PROTOTYPE)
@DependsOn(properties = @DependsOn.Property(key = NuStarPartyProperties.IS_SUB_PLUGIN, value = "false"))
public class PartyLanguage {
    private static final Language NULL = new Language(null);

    @NotNull
    private Language onCannotDisbandPartyWhileInDungeon = NULL;

    @NotNull
    private Language onTeleportToDungeonConditionsNotMet = NULL;

    @NotNull
    private Language onTeleportToDungeon = NULL;

    @NotNull
    private Language onNonLeaderStartDungeon = NULL;

    @NotNull
    private Language onOtherMemberInDungeon = NULL;

    @NotNull
    private Language onInviteApplicationAlreadyExist = NULL;

    @NotNull
    private Language onSomeoneInviteJoinPartyRefuse = NULL;

    @NotNull
    private Language onKickPartyFail = NULL;

    @NotNull
    private Language onSomeoneJoinedParty = NULL;

    @NotNull
    private Language onNotJoinParty = NULL;

    @NotNull
    private Language onKickPartyNotInParty = NULL;

    @NotNull
    private Language onLeaderQuitParty = NULL;

    @NotNull
    private Language onJoinPartyAccept = NULL;

    @NotNull
    private Language onLeaderDisbandParty = NULL;

    @NotNull
    private Language onNonLeaderDisbandParty = NULL;

    @NotNull
    private Language onJoinPartyRefuse = NULL;

    @NotNull
    private Language onPartyRefuseJoinApplication = NULL;

    @NotNull
    private Language onSomeoneJoinPartyApplication = NULL;

    @NotNull
    private Language onJoinPartyApplicationAlreadyExist = NULL;

    @NotNull
    private Language onCreateParty = NULL;

    @NotNull
    private Language onJoinPartyApplication = NULL;

    @NotNull
    private Language onJoinParty = NULL;

    @NotNull
    private Language onQuitParty = NULL;

    @NotNull
    private Language onKickedOutParty = NULL;

    @NotNull
    private Language onKickParty = NULL;

    @NotNull
    private Language onPartyDisband = NULL;

    @NotNull
    private Language onPartyFull = NULL;

    @NotNull
    private Language onPartyNotExist = NULL;

    @NotNull
    private Language onPartyAlreadyExist = NULL;

    @NotNull
    private Language onPartyAlreadyJoined = NULL;

    @NotNull
    private Language onMemberQuitParty = NULL;

    @NotNull
    private Language onSomeoneInviteJoinParty = NULL;

    @NotNull
    private Language onInviteJoinParty = NULL;

    @NotNull
    private Language onInviteAlreadyJoinedParty = NULL;

    @NotNull
    private Language onInviteJoinPartyAccept = NULL;

    @NotNull
    private Language onInviteJoinPartyRefuse = NULL;

    @NotNull
    private Language onInviteJoinPartyTimeout = NULL;

    @NotNull
    private Language onPartyMerge = NULL;

    @NotNull
    private Language onPartyMergeRefuse = NULL;

    @NotNull
    private Language onPartyMergeAccept = NULL;

    @NotNull
    private Language onPartyTransferLeader = NULL;

    @NotNull
    private Language onChangePickupMode = NULL;

    @NotNull
    private Language onSetPartyDestination = NULL;

    @NotNull
    private Language onNonLeaderOpenJoinApplicationMenu = NULL;

    @NotNull
    private Language onSendGatherApplication = NULL;

    @Data
    @JsonDeserialize(using = LanguageDeserializer.class)
    public static class Language {
        private final String text;

        public void use(@NotNull Action<String> action) {
            use(Collections.emptyMap(), action);
        }

        public void use(@NotNull Map<String, Object> variables, @NotNull Action<String> action) {
            Validation.notNull(variables, "variables cannot be null");
            Validation.notNull(action, "action cannot be null");
            String text = this.text;
            if (text != null) {
                try {
                    for (Map.Entry<String, Object> entry : variables.entrySet()) {
                        text = text.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
                    }
                    action.execute(text);
                } catch (Throwable throwable) {
                    throw new RuntimeException();
                }
            }
        }
    }

    public static class LanguageDeserializer extends JsonDeserializer<Language> {
        @Override
        public Language deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            return new Language(jsonParser.getText());
        }
    }
}

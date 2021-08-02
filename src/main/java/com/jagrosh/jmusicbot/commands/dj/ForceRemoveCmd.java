/*
 * Copyright 2019 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Michaili K.
 */
public class ForceRemoveCmd extends DJCommand
{
    public ForceRemoveCmd(Bot bot)
    {
        super(bot);
        this.name = "forceremove";
        this.help = "removes all entries by a user from the queue";
        this.arguments = "<user>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = false;
        this.bePlaying = true;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.options = Collections.singletonList(new OptionData(OptionType.USER, "user", "The user to remove all entries from", true));
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {

        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getQueue().isEmpty())
        {
            event.reply(getClient().getError()+" There is nothing in the queue!")
                .setEphemeral(true)
                .queue();
            return;
        }

        removeAllEntries(event.getOption("user").getAsUser(), event);

    }

    private void removeAllEntries(User target, SlashCommandEvent event)
    {
        int count = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getQueue().removeAll(target.getIdLong());
        if (count == 0)
        {
            event.reply(client.getWarning()+" "+target.getAsMention()+" doesn't have any songs in the queue!")
                .setEphemeral(true)
                .queue();
        }
        else
        {
            event.reply(getClient().getSuccess()+" Successfully removed `"+count+"` entries from "+target.getAsMention())
                .allowedMentions(Collections.emptyList())
                .queue();
        }
    }
}

/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.admin;

import java.util.Collections;
import java.util.List;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SettcCmd extends AdminCommand 
{
    public SettcCmd(Bot bot)
    {
        this.name = "settc";
        this.help = "sets the text channel for music commands";
        this.arguments = "[channel]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "channel", "Sets the text channel. Leave it empty to remove the text channel.", false));
    }

    public void doCommand(SlashCommandEvent event)
    {
        Settings s = getClient().getSettingsFor(event.getGuild());
        OptionMapping channel = event.getOption("channel");

        if(channel == null)
        {
            s.setTextChannel(null);
            event.reply(getClient().getSuccess()+" Music commands can now be used in any channel")
                .queue();
        }
        else
        {
            if(channel.getAsGuildChannel().getType() != ChannelType.TEXT)
            {
                event.reply(getClient().getError()+" The channel must be a text channel!")
                    .setEphemeral(true)
                    .queue();
                return;
            }
            s.setTextChannel((TextChannel) channel.getAsGuildChannel());
            event.reply(getClient().getSuccess()+" Music commands can now only be used in "+channel.getAsGuildChannel().getAsMention())
                .queue();

        }
    }
    
}

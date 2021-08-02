/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class AutoplaylistCmd extends OwnerCommand
{
    private final Bot bot;
    
    public AutoplaylistCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "autoplaylist";
        this.arguments = "<name|NONE>";
        this.help = "sets the default playlist for the server";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "Playlist name, or none to remove", true));
    }

    @Override
    public void execute(SlashCommandEvent event)
    {
        if(!event.isFromGuild())
        {
            event.reply(getClient().getError()+" This command cannot be used in Direct messages").setEphemeral(true).queue();
            return;
        }

        String args = event.getOption("name").getAsString();

        if(args.equalsIgnoreCase("none"))
        {
            Settings settings = getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(null);
            event.reply(getClient().getSuccess()+" Cleared the default playlist for **"+event.getGuild().getName()+"**")
                    .allowedMentions(Collections.emptyList())
                    .queue();
            return;
        }
        String pname = args.replaceAll("\\s+", "_");
        if(bot.getPlaylistLoader().getPlaylist(pname)==null)
        {
            event.reply(getClient().getError()+" Could not find `"+pname+".txt`!")
                    .allowedMentions(Collections.emptyList())
                    .setEphemeral(true)
                    .queue();
        }
        else
        {
            Settings settings = getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(pname);
            event.reply(getClient().getSuccess()+" The default playlist for **"+event.getGuild().getName()+"** is now `"+pname+"`")
                    .allowedMentions(Collections.emptyList())
                    .queue();
        }
    }
}

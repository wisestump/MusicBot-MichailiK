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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sun.org.apache.xpath.internal.objects.XString;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class VolumeCmd extends DJCommand
{
    public VolumeCmd(Bot bot)
    {
        super(bot);
        this.name = "volume";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.help = "sets or shows volume";
        this.arguments = "[0-150]";
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "volume", "Volume in percent", false));
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        Settings settings = getClient().getSettingsFor(event.getGuild());
        int volume = handler.getPlayer().getVolume();
        if(event.getOption("volume") == null)
        {
            event.reply(FormatUtil.volumeIcon(volume)+" Current volume is `"+settings.getVolume()+"`")
                    .setEphemeral(true)
                    .queue();
        }
        else
        {
            int nvolume = (int) event.getOption("volume").getAsLong();
            if(nvolume<0 || nvolume>150)
                event.reply(getClient().getError()+" Volume must be a valid integer between 0 and 150!")
                        .setEphemeral(true)
                        .queue();
            else
            {
                handler.getPlayer().setVolume(nvolume);
                settings.setVolume(nvolume);
                event.reply(FormatUtil.volumeIcon(volume)+" Volume changed from `"+volume+"` to `"+nvolume+"`").queue();
            }
        }
    }
}

/*
 * Copyright 2021 John Grosh <john.a.grosh@gmail.com>.
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

import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.Collections;

public class ResumeCmd extends DJCommand
{
    public ResumeCmd(Bot bot)
    {
        super(bot);
        this.name = "resume";
        this.help = "resumes playback of music";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    @Override
    public void doCommand(SlashCommandEvent event)
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if(handler.getPlayer().getPlayingTrack() == null)
        {
            event.reply(getClient().getError()+" There must be music playing to use that!")
                .setEphemeral(true)
                .queue();
        }
        else if(!handler.getPlayer().isPaused())
        {
            event.reply(getClient().getError()+" The music isn't paused!")
                    .setEphemeral(true)
                    .queue();
        }
        else
        {
            handler.getPlayer().setPaused(false);
            event.reply(getClient().getSuccess() + " Resumed **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**.")
                    .allowedMentions(Collections.emptyList())
                    .queue();
        }
    }
}

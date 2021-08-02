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
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;


public class RemoveAllCmd extends MusicCommand
{
    public RemoveAllCmd(Bot bot)
    {
        super(bot);
        this.name = "removeall";
        this.help = "removes all your songs from the queue";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = false;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if(handler.getQueue().isEmpty())
        {
            event.reply(getClient().getError()+" There is nothing in the queue!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        int count = handler.getQueue().removeAll(event.getUser().getIdLong());
        if (count == 0)
            event.reply(getClient().getWarning()+" You don't have any songs in the queue!")
                    .setEphemeral(true)
                    .queue();
        else
            event.reply(getClient().getSuccess()+" Successfully removed your " + count + " entries.")
                    .setEphemeral(true)
                    .queue();
    }
}

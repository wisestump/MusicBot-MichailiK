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
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

public class PlayPlaylistCmd extends MusicCommand
{
    public PlayPlaylistCmd(Bot bot)
    {
        super(bot);
        this.name = "play-playlist";
        this.aliases = new String[]{"pl"};
        this.arguments = "<name>";
        this.help = "plays the provided playlist";
        this.beListening = true;
        this.bePlaying = false;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "Name of playlist to play", true));
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        String name;
        if(event.getOption("Name") == null)
            name = null;
        else
            name = event.getOption("Name").getAsString();

        if(name == null)
        {
            event.reply(getClient().getError()+" Please include a playlist name.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        PlaylistLoader.Playlist playlist = bot.getPlaylistLoader().getPlaylist(name);
        if(playlist==null)
        {
            event.reply(getClient().getError()+" I could not find `"+name+".txt` in the Playlists folder.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        event.deferReply(false).queue();

        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        playlist.loadTracks(bot.getPlayerManager(), (at) -> handler.addTrack(new QueuedTrack(at, event.getUser())), () -> {
            StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                    ? getClient().getWarning() + " No tracks were loaded!"
                    : getClient().getSuccess() + " Loaded **" + playlist.getTracks().size() + "** tracks!");
            if (!playlist.getErrors().isEmpty())
                builder.append("\nThe following tracks failed to load:");
            playlist.getErrors().forEach(err -> builder.append("\n`[").append(err.getIndex() + 1).append("]` **").append(err.getItem()).append("**: ").append(err.getReason()));
            String str = builder.toString();
            if (str.length() > 2000)
                str = str.substring(0, 1994) + " (...)";

            event.getHook().sendMessage(str)
                    .allowedMentions(Collections.emptyList())
                    .queue();
        });
    }
}

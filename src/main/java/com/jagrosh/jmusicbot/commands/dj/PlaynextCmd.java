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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class PlaynextCmd extends DJCommand
{
    private final String loadingEmoji;
    
    public PlaynextCmd(Bot bot)
    {
        super(bot);
        this.loadingEmoji = bot.getConfig().getLoading();
        this.name = "playnext";
        this.arguments = "<title|URL>";
        this.help = "plays a single song next";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "query", "Title or URL to play", true));
    }
    
    @Override
    public void doCommand(SlashCommandEvent event)
    {
        // TODO Attachments whenever they get supported
        // String args = event.getArgs().startsWith("<") && event.getArgs().endsWith(">")
        //         ? event.getArgs().substring(1,event.getArgs().length()-1)
        //         : event.getArgs().isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : event.getArgs();

        String args = event.getOption("query").getAsString();
        event.deferReply(false).queue();
        bot.getPlayerManager().loadItemOrdered(event.getGuild(), args, new ResultHandler(args, event,false));
    }
    
    private class ResultHandler implements AudioLoadResultHandler
    {
        private final String query;
        private final SlashCommandEvent event;
        private final boolean ytsearch;
        
        private ResultHandler(String query, SlashCommandEvent event, boolean ytsearch)
        {
            this.query = query;
            this.event = event;
            this.ytsearch = ytsearch;
        }
        
        private void loadSingle(AudioTrack track)
        {
            if(bot.getConfig().isTooLong(track))
            {
                event.getHook().sendMessage(getClient().getWarning()+" This track (**"+track.getInfo().title+"**) is longer than the allowed maximum: `"
                        + TimeUtil.formatTime(track.getDuration())+"` > `"+ TimeUtil.formatTime(bot.getConfig().getMaxSeconds()*1000)+"`")
                .allowedMentions(Collections.emptyList())
                .queue();
                return;
            }
            AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
            int pos = handler.addTrackToFront(new QueuedTrack(track, event.getUser()))+1;
            String addMsg = getClient().getSuccess()+" Added **"+track.getInfo().title
                    +"** (`"+ TimeUtil.formatTime(track.getDuration())+"`) "+(pos==0?"to begin playing":" to the queue at position "+pos);
            event.getHook().sendMessage(addMsg).allowedMentions(Collections.emptyList()).queue();
        }
        
        @Override
        public void trackLoaded(AudioTrack track)
        {
            loadSingle(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            AudioTrack single;
            if(playlist.getTracks().size()==1 || playlist.isSearchResult())
                single = playlist.getSelectedTrack()==null ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
            else if (playlist.getSelectedTrack()!=null)
                single = playlist.getSelectedTrack();
            else
                single = playlist.getTracks().get(0);
            loadSingle(single);
        }

        @Override
        public void noMatches()
        {
            if(ytsearch)
                event.getHook().sendMessage(getClient().getWarning()+" No results found for `"+query+"`.")
                        .allowedMentions(Collections.emptyList())
                        .queue();
            else
                bot.getPlayerManager().loadItemOrdered(event.getGuild(), "ytsearch:"+query, new ResultHandler(query,event,true));
        }

        @Override
        public void loadFailed(FriendlyException throwable)
        {
            if(throwable.severity==FriendlyException.Severity.COMMON)
                event.getHook().sendMessage(getClient().getError()+" Error loading: "+throwable.getMessage())
                        .allowedMentions(Collections.emptyList())
                        .queue();
            else
                event.getHook().sendMessage(getClient().getError()+" Error loading track.").queue();
        }
    }
}

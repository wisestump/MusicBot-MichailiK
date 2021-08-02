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
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader.Playlist;
import com.jagrosh.jmusicbot.utils.FormatUtil;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PlayCmd extends MusicCommand
{
    private final static String LOAD = "\uD83D\uDCE5"; // 📥
    private final static String CANCEL = "\uD83D\uDEAB"; // 🚫
    
    private final String loadingEmoji;
    
    public PlayCmd(Bot bot)
    {
        super(bot);
        this.loadingEmoji = bot.getConfig().getLoading();
        this.name = "play";
        this.arguments = "<title|URL>";
        this.help = "plays the provided song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "query", "Title or URL to play. Leave empty to unpause.", true));
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        String title;
        if(event.getOption("query") == null)
            title = null;
        else
            title = event.getOption("query").getAsString();


        if(title == null)
        {
            AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
            if(handler.getPlayer().getPlayingTrack()!=null && handler.getPlayer().isPaused())
            {
                if(DJCommand.checkDJPermission(event, getClient()))
                {
                    handler.getPlayer().setPaused(false);
                    event.reply(getClient().getSuccess()+" Resumed **"+handler.getPlayer().getPlayingTrack().getInfo().title+"**.")
                        .allowedMentions(Collections.emptyList())
                        .queue();
                }
                else
                    event.reply(getClient().getError()+" Only DJs can unpause the player!")
                            .setEphemeral(true)
                            .queue();
                return;
            }
            StringBuilder builder = new StringBuilder(getClient().getWarning()+" Play Commands:\n");
            builder.append("\n`/").append(name).append(" <song title>` - plays the first result from Youtube");
            builder.append("\n`/").append(name).append(" <URL>` - plays the provided song, playlist, or stream");
            for(Command cmd: children)
                builder.append("\n`/").append(name).append(" ").append(cmd.getName()).append(" ").append(cmd.getArguments()).append("` - ").append(cmd.getHelp());
            event.reply(builder.toString()).setEphemeral(true).queue();
            return;
        }
        String args = title.startsWith("<") && title.endsWith(">")
                ? title.substring(1,title.length()-1)
                  : title;
                // TODO Attachments whenever they get supported
                /*: title.isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : event.getArgs();*/
        event.deferReply(false).queue();
        bot.getPlayerManager().loadItemOrdered(event.getGuild(), args, new ResultHandler(args,event,false));
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
        
        private void loadSingle(AudioTrack track, AudioPlaylist playlist)
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
            int pos = handler.addTrack(new QueuedTrack(track, event.getUser()))+1;
            String addMsg = getClient().getSuccess()+" Added **"+track.getInfo().title
                    +"** (`"+ TimeUtil.formatTime(track.getDuration())+"`) "+(pos==0?"to begin playing":" to the queue at position "+pos);

            if(playlist==null)
                event.getHook().sendMessage(addMsg).allowedMentions(Collections.emptyList()).queue();
            else
            {
                event.getHook().sendMessage(addMsg+"\n"+getClient().getWarning()+" This track has a playlist of **"+playlist.getTracks().size()+"** tracks attached. Select "+LOAD+" to load playlist")
                        .addActionRow(
                                Button.primary("load", Emoji.fromUnicode(LOAD)),
                                Button.secondary("cancel", Emoji.fromUnicode(CANCEL))
                                )
                        .queue(message ->
                                bot.getWaiter().waitForEvent(
                                        ButtonClickEvent.class,
                                        e ->
                                        {
                                            if(e.getMessageIdLong() != message.getIdLong()) return false;
                                            if(e.getUser().getIdLong() != event.getUser().getIdLong())
                                            {
                                                e.deferEdit().queue();
                                                return false;
                                            }
                                            return true;
                                        },
                                        buttonEvent ->
                                        {
                                            if(buttonEvent.getButton().getId().equals("load"))
                                                message.editMessage(addMsg + "\n" + getClient().getSuccess() + " Loaded **" + loadPlaylist(playlist, track) + "** additional tracks!")
                                                        .setActionRows(Collections.emptyList())
                                                        .queue();
                                            else if(buttonEvent.getButton().getId().equals("cancel"))
                                                message.editMessageComponents(Collections.emptyList()).queue();
                                        },
                                        30,
                                        TimeUnit.SECONDS,
                                        () -> message.editMessageComponents(Collections.emptyList()).queue()
                                ));
            }
        }
        
        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude)
        {
            int[] count = {0};
            playlist.getTracks().stream().forEach((track) -> {
                if(!bot.getConfig().isTooLong(track) && !track.equals(exclude))
                {
                    AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
                    handler.addTrack(new QueuedTrack(track, event.getUser()));
                    count[0]++;
                }
            });
            return count[0];
        }
        
        @Override
        public void trackLoaded(AudioTrack track)
        {
            loadSingle(track, null);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            if(playlist.getTracks().size()==1 || playlist.isSearchResult())
            {
                AudioTrack single = playlist.getSelectedTrack()==null ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
                loadSingle(single, null);
            }
            else if (playlist.getSelectedTrack()!=null)
            {
                AudioTrack single = playlist.getSelectedTrack();
                loadSingle(single, playlist);
            }
            else
            {
                int count = loadPlaylist(playlist, null);
                if(count==0)
                {
                    event.getHook().sendMessage(getClient().getWarning()+" All entries in this playlist "+(playlist.getName()==null ? "" : "(**"+playlist.getName()
                            +"**) ")+"were longer than the allowed maximum (`"+bot.getConfig().getMaxTime()+"`)")
                            .allowedMentions(Collections.emptyList())
                            .queue();
                }
                else
                {
                    event.getHook().sendMessage(getClient().getSuccess()+" Found "
                            +(playlist.getName()==null?"a playlist":"playlist **"+playlist.getName()+"**")+" with `"
                            + playlist.getTracks().size()+"` entries; added to the queue!"
                            + (count<playlist.getTracks().size() ? "\n"+getClient().getWarning()+" Tracks longer than the allowed maximum (`"
                            + bot.getConfig().getMaxTime()+"`) have been omitted." : ""))
                                .allowedMentions(Collections.emptyList())
                                .queue();
                }
            }
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
            if(throwable.severity==Severity.COMMON)
                event.getHook().sendMessage(getClient().getError()+" Error loading: "+throwable.getMessage()).queue();
            else
                event.getHook().sendMessage(getClient().getError()+" Error loading track.").queue();
        }
    }
    

}

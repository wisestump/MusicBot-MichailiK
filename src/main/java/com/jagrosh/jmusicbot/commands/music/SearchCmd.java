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

import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SearchCmd extends MusicCommand 
{
    private final static String[] NUMBERS = new String[]{"1\u20E3","2\u20E3","3\u20E3",
            "4\u20E3","5\u20E3","6\u20E3","7\u20E3","8\u20E3","9\u20E3", "\uD83D\uDD1F"};
    private final static String CANCEL = "\u274C";


    protected String searchPrefix = "ytsearch:";
    private final String searchingEmoji;
    
    public SearchCmd(Bot bot)
    {
        super(bot);
        this.searchingEmoji = bot.getConfig().getSearching();
        this.name = "search";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.arguments = "<query>";
        this.help = "searches Youtube for a provided query";
        this.beListening = true;
        this.bePlaying = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "query", "The search query", true));
    }
    @Override
    public void doCommand(SlashCommandEvent event)
    {
        String query = event.getOption("query").getAsString();
        event.deferReply(false).queue();
        bot.getPlayerManager().loadItemOrdered(event.getGuild(), searchPrefix + query, new ResultHandler(query,event));
    }
    
    private class ResultHandler implements AudioLoadResultHandler 
    {
        private final String query;
        private final SlashCommandEvent event;

        private ResultHandler(String query, SlashCommandEvent event)
        {
            this.query = query;
            this.event = event;
        }
        
        @Override
        public void trackLoaded(AudioTrack track)
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
            event.getHook().sendMessage(getClient().getSuccess()+" Added **"+track.getInfo().title
                    +"** (`"+ TimeUtil.formatTime(track.getDuration())+"`) "+(pos==0 ? "to begin playing"
                        : " to the queue at position "+pos))
                    .allowedMentions(Collections.emptyList()).queue();
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            StringBuilder stringBuilder = new StringBuilder();

            for(int i=0; i<4 && i<playlist.getTracks().size(); i++)
            {
                AudioTrack track = playlist.getTracks().get(i);
                if(i != 0) stringBuilder.append("\n");
                stringBuilder
                        .append(i+1)
                        .append(". `[")
                        .append(TimeUtil.formatTime(track.getDuration()))
                        .append("]` [**").append(track.getInfo().title)
                        .append("**](")
                        .append(track.getInfo().uri)
                        .append(")");
            }


            event.getHook().sendMessage("Search results for `" + query + "`:")
                    .allowedMentions(Collections.emptyList())
                    .addEmbeds(
                            new EmbedBuilder()
                                    .setColor(event.getGuild().getSelfMember().getColor())
                                    .setDescription(stringBuilder.toString()).build()
                    )
                    .addActionRow(
                            Button.primary("1", Emoji.fromUnicode(NUMBERS[0])),
                            Button.primary("2", Emoji.fromUnicode(NUMBERS[1])),
                            Button.primary("3", Emoji.fromUnicode(NUMBERS[2])),
                            Button.primary("4", Emoji.fromUnicode(NUMBERS[3])),
                            Button.secondary("cancel", Emoji.fromUnicode(CANCEL))
                    )
                    .queue(message ->
                            bot.getWaiter().waitForEvent(
                                    ButtonClickEvent.class,
                                    e -> e.getMessageIdLong() == message.getIdLong() && e.getUser().getIdLong() == event.getUser().getIdLong(),
                                    buttonEvent ->
                                    {

                                        if(buttonEvent.getButton().getId().equals("cancel"))
                                            message.delete().queue();
                                        else
                                        {
                                            int i = Integer.parseInt(buttonEvent.getButton().getId());
                                            AudioTrack track = playlist.getTracks().get(i-1);
                                            if(bot.getConfig().isTooLong(track))
                                            {
                                                message.editMessage(getClient().getWarning()+" This track (**"+track.getInfo().title+"**) is longer than the allowed maximum: `"
                                                        + TimeUtil.formatTime(track.getDuration())+"` > `"+bot.getConfig().getMaxTime()+"`")
                                                        .allowedMentions(Collections.emptyList())
                                                        .setEmbeds(Collections.emptyList())
                                                        .setActionRows(Collections.emptyList())
                                                        .queue();
                                                return;
                                            }
                                            AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
                                            int pos = handler.addTrack(new QueuedTrack(track, event.getUser()))+1;
                                            message.editMessage(getClient().getSuccess()+" Added **" + FormatUtil.filter(track.getInfo().title)
                                                    + "** (`" + TimeUtil.formatTime(track.getDuration()) + "`) " + (pos==0 ? "to begin playing"
                                                    : " to the queue at position "+pos))
                                                    .allowedMentions(Collections.emptyList())
                                                    .setEmbeds(Collections.emptyList())
                                                    .setActionRows(Collections.emptyList())
                                                    .queue();
                                        }
                                    },
                                    30,
                                    TimeUnit.SECONDS,
                                    () -> message.editMessageComponents(Collections.emptyList()).queue()
                            ));
        }

        @Override
        public void noMatches() 
        {
            event.getHook().sendMessage(getClient().getWarning()+" No results found for `"+query+"`.")
            .allowedMentions(Collections.emptyList())
            .queue();
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

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
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jlyrics.LyricsClient;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class LyricsCmd extends MusicCommand
{
    private final LyricsClient client = new LyricsClient();
    
    public LyricsCmd(Bot bot)
    {
        super(bot);
        this.name = "lyrics";
        this.arguments = "[song name]";
        this.help = "shows the lyrics to the currently-playing song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.bePlaying = true;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "song", "Song Name, or empty for the playing song"));
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        event.deferReply(true).queue();

        String title;
        if(event.getOption("song") == null)
            title = null;
        else
            title = event.getOption("song").getAsString();

        if(title == null)
            title = ((AudioHandler)event.getGuild().getAudioManager().getSendingHandler()).getPlayer().getPlayingTrack().getInfo().title;

        String finalTitle = title;
        client.getLyrics(title).thenAccept(lyrics ->
        {
            if(lyrics == null)
            {
                event.getHook().sendMessage("Lyrics for `" + finalTitle + "` could not be found!").queue();
                return;
            }

            EmbedBuilder eb = new EmbedBuilder()
                    .setAuthor(lyrics.getAuthor())
                    .setColor(event.getGuild().getSelfMember().getColor())
                    .setTitle(lyrics.getTitle(), lyrics.getURL());
            if(lyrics.getContent().length()>15000)
            {
                event.getHook().sendMessage(getClient().getWarning()+" Lyrics for `" + finalTitle + "` found but likely not correct: " + lyrics.getURL())
                    .queue();
            }
            else if(lyrics.getContent().length()>2000)
            {
                String content = lyrics.getContent().trim();
                List<MessageEmbed> embeds = new ArrayList<>();
                while(content.length() > 4000)
                {
                    int index = content.lastIndexOf("\n\n", 4000);
                    if(index == -1)
                        index = content.lastIndexOf("\n", 4000);
                    if(index == -1)
                        index = content.lastIndexOf(" ", 4000);
                    if(index == -1)
                        index = 4000;
                    embeds.add(eb.setDescription(content.substring(0, index).trim()).build());
                    content = content.substring(index).trim();
                    eb.setAuthor(null).setTitle(null, null);
                }
                embeds.add(eb.setDescription(content).build());
                event.getHook().sendMessageEmbeds(embeds).queue();
            }
            else
                event.getHook().sendMessageEmbeds(eb.setDescription(lyrics.getContent()).build()).queue();
        });
    }
}

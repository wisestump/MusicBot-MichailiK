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

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class QueueCmd extends MusicCommand 
{
    private final static String REPEAT = "\uD83D\uDD01"; // 🔁
    
    public QueueCmd(Bot bot)
    {
        super(bot);
        this.name = "queue";
        this.help = "shows the current queue";
        this.arguments = "[pagenum]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
        this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION,Permission.MESSAGE_EMBED_LINKS};
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "page", "Optional page number", false));
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        int pagenum = 1;
        if(event.getOption("page") != null)
            pagenum = (int) event.getOption("page").getAsLong();

        AudioHandler ah = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        List<QueuedTrack> list = ah.getQueue().getList();
        if(list.isEmpty())
        {
            event.reply(getClient().getWarning()+" There is no music in the queue!")
                    .setEphemeral(true)
                    .complete();
            return;
        }

        event.deferReply(false).queue();

        String[] songs = new String[list.size()];
        long total = 0;
        for(int i=0; i<list.size(); i++)
        {
            total += list.get(i).getTrack().getDuration();
            songs[i] = list.get(i).toString();
        }
        Settings settings = getClient().getSettingsFor(event.getGuild());

        new QueueList(
                event.getHook(),
                event.getUser().getIdLong(),
                event.getGuild().getSelfMember().getColor(),
                songs,
                getQueueTitle(ah, getClient().getSuccess(), songs.length, total, settings.getRepeatMode()),
                bot.getWaiter(),
                pagenum - 1
        );
    }
    
    private String getQueueTitle(AudioHandler ah, String success, int songslength, long total, boolean repeatmode)
    {
        StringBuilder sb = new StringBuilder();
        if(ah.getPlayer().getPlayingTrack()!=null)
        {
            sb.append(ah.getPlayer().isPaused() ? JMusicBot.PAUSE_EMOJI : JMusicBot.PLAY_EMOJI).append(" **")
                    .append(ah.getPlayer().getPlayingTrack().getInfo().title).append("**\n");
        }
        return FormatUtil.filter(sb.append(success).append(" Current Queue | ").append(songslength)
                .append(" entries | `").append(TimeUtil.formatTime(total)).append("` ")
                .append(repeatmode ? "| " + REPEAT : "").toString());
    }

    private class QueueList
    {
        private static final int entriesPerPage = 10;
        private static final String LEFT = "\u25C0";
        private static final String STOP = "\u23F9";
        private static final String RIGHT = "\u25B6";

        private final InteractionHook hook;
        private final long userId;
        private final Color color;
        private final String[] songs;
        private final String text;
        private final EventWaiter waiter;

        private int page;
        private Message message = null;

        private QueueList(InteractionHook hook, long userId, Color color, String[] songs, String text, EventWaiter waiter, int page)
        {
            if(songs.length == 0)
                throw new IllegalArgumentException("Songs is empty");
            this.hook = hook;
            this.userId = userId;
            this.color = color;
            this.songs = songs;
            this.text = text;
            this.waiter = waiter;
            this.page = page;

            update();
            registerToWaiter();
        }

        private void registerToWaiter()
        {
            waiter.waitForEvent(ButtonClickEvent.class, this::onEventCheck, this::onEvent, 1, TimeUnit.MINUTES, this::cleanup);
        }

        private boolean onEventCheck(ButtonClickEvent event)
        {
            if(message == null || event.getMessageIdLong() != message.getIdLong())
                return false;
            if(event.getUser().getIdLong() != userId)
            {
                event.deferEdit().queue();
                return false;
            }
            return true;
        }

        private void onEvent(ButtonClickEvent event)
        {
            event.deferEdit().queue();
            Button button = event.getButton();

            if(button.getId().equals("prev"))
                page--;
            else if(button.getId().equals("next"))
                page++;
            else
            {
                cleanup();
                return;
            }
            update();
            registerToWaiter();
        }

        private void cleanup()
        {
            if(message == null) return;
            message.editMessageComponents(Collections.emptyList()).queue();
        }


        private void update()
        {

            MessageBuilder message = new MessageBuilder();
            message.setAllowedMentions(Collections.emptyList());
            message.setContent(text);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(color);

            if(page < 0)
                page = Math.floorDiv(songs.length, entriesPerPage);

            int startIndex = page * entriesPerPage;
            if(startIndex >= songs.length)
            {
                page = 0;
                startIndex = 0;
            }

            embed.setFooter("Page "+(page+1)+"/"+(Math.floorDiv(songs.length, entriesPerPage)+1));

            StringBuilder stringBuilder = new StringBuilder();
            for(int i = startIndex; i < startIndex+entriesPerPage && i < songs.length; i++)
                stringBuilder.append("`").append(i+1).append(".` ")
                        .append(songs[i]).append("\n");
            embed.setDescription(stringBuilder);

            message.setEmbeds(embed.build());

            hook.editOriginal(message.build())
                    .setActionRow(
                            Button.primary("prev", Emoji.fromUnicode(LEFT)),
                            Button.secondary("stop", Emoji.fromUnicode(STOP)),
                            Button.primary("next", Emoji.fromUnicode(RIGHT))
                    )
                    .queue(x -> this.message = x);
        }
    }
}

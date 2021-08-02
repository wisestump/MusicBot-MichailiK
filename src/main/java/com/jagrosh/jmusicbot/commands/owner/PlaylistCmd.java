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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader.Playlist;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PlaylistCmd extends OwnerCommand 
{
    protected final Bot bot;
    private PlaylistCmd(Bot bot)
    {
        this.bot = bot;
        this.subcommandGroup = new SubcommandGroupData("playlist", "playlists management");
    }
    
    public static class MakelistCmd extends PlaylistCmd
    {
        public MakelistCmd(Bot bot)
        {
            super(bot);
            this.name = "make";
            this.aliases = new String[]{"create"};
            this.help = "makes a new playlist";
            this.arguments = "<name>";
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "Playlist name", true));
        }

        @Override
        protected void execute(SlashCommandEvent event)
        {
            String pname = event.getOption("name").getAsString().replaceAll("\\s+", "_");
            if(bot.getPlaylistLoader().getPlaylist(pname)==null)
            {
                try
                {
                    bot.getPlaylistLoader().createPlaylist(pname);
                    event.reply(getClient().getSuccess()+" Successfully created playlist `"+pname+"`!")
                        .setEphemeral(true)
                        .queue();
                }
                catch(IOException e)
                {
                    event.reply(getClient().getError()+" I was unable to create the playlist: "+e.getLocalizedMessage())
                        .setEphemeral(true)
                        .queue();
                }
            }
            else
                event.reply(getClient().getError()+" Playlist `"+pname+"` already exists!")
                        .setEphemeral(true)
                        .queue();
        }
    }
    
    public static class DeletelistCmd extends PlaylistCmd
    {
        public DeletelistCmd(Bot bot)
        {
            super(bot);
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "deletes an existing playlist";
            this.arguments = "<name>";
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "Playlist name", true));
        }

        @Override
        protected void execute(SlashCommandEvent event)
        {
            String pname = event.getOption("name").getAsString().replaceAll("\\s+", "_");
            if(bot.getPlaylistLoader().getPlaylist(pname)==null)
                event.reply(getClient().getError()+" Playlist `"+pname+"` doesn't exist!")
                        .setEphemeral(true)
                        .queue();
            else
            {
                try
                {
                    bot.getPlaylistLoader().deletePlaylist(pname);
                    event.reply(getClient().getSuccess()+" Successfully deleted playlist `"+pname+"`!")
                        .setEphemeral(true)
                        .queue();
                }
                catch(IOException e)
                {
                    event.reply(getClient().getError()+" I was unable to delete the playlist: "+e.getLocalizedMessage())
                        .setEphemeral(true)
                        .queue();
                }
            }
        }
    }
    
    public static class AppendlistCmd extends PlaylistCmd
    {
        public AppendlistCmd(Bot bot)
        {
            super(bot);
            this.name = "append";
            this.aliases = new String[]{"add"};
            this.help = "appends songs to an existing playlist";
            this.arguments = "<name> <URL> | <URL> | ...";
            this.options = Arrays.asList(
                    new OptionData(OptionType.STRING, "name", "Playlist name", true),
                    new OptionData(OptionType.STRING, "urls", "One or more URLs separated by |", true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event)
        {
            String pname = event.getOption("name").getAsString().replaceAll("\\s+", "_");

            Playlist playlist = bot.getPlaylistLoader().getPlaylist(pname);
            if(playlist==null)
                event.reply(getClient().getError()+" Playlist `"+pname+"` doesn't exist!")
                        .setEphemeral(true)
                        .queue();
            else
            {
                StringBuilder builder = new StringBuilder();
                playlist.getItems().forEach(item -> builder.append("\r\n").append(item));
                String[] urls = event.getOption("urls").getAsString().split("\\|");
                for(String url: urls)
                {
                    String u = url.trim();
                    if(u.startsWith("<") && u.endsWith(">"))
                        u = u.substring(1, u.length()-1);
                    builder.append("\r\n").append(u);
                }
                try
                {
                    bot.getPlaylistLoader().writePlaylist(pname, builder.toString());
                    event.reply(getClient().getSuccess()+" Successfully added "+urls.length+" items to playlist `"+pname+"`!")
                        .setEphemeral(true)
                        .queue();
                }
                catch(IOException e)
                {
                    event.reply(getClient().getError()+" I was unable to append to the playlist: "+e.getLocalizedMessage())
                        .setEphemeral(true)
                        .queue();
                }
            }
        }
    }
    
    public static class DefaultlistCmd extends AutoplaylistCmd
    {
        public DefaultlistCmd(Bot bot)
        {
            super(bot);

            this.name = "setdefault";
            this.aliases = new String[]{"default"};
            this.arguments = "<playlistname|NONE>";
            this.subcommandGroup = new SubcommandGroupData("playlist", "playlists management");
            // This is already guild only in AutoplaylistCmd
        }
    }
    
    public static class ListCmd extends PlaylistCmd
    {
        public ListCmd(Bot bot)
        {
            super(bot);
            this.name = "all";
            this.aliases = new String[]{"available","list"};
            this.help = "lists all available playlists";
        }

        @Override
        protected void execute(SlashCommandEvent event)
        {
            if(!event.isFromGuild())
            {
                event.reply(getClient().getError()+" This command cannot be used in Direct messages").setEphemeral(true).queue();
                return;
            }

            if(!bot.getPlaylistLoader().folderExists())
                bot.getPlaylistLoader().createFolder();
            if(!bot.getPlaylistLoader().folderExists())
            {
                event.reply(getClient().getWarning()+" Playlists folder does not exist and could not be created!")
                    .setEphemeral(true)
                    .queue();
                return;
            }
            List<String> list = bot.getPlaylistLoader().getPlaylistNames();
            if(list==null)
                event.reply(getClient().getError()+" Failed to load available playlists!")
                        .setEphemeral(true)
                        .queue();
            else if(list.isEmpty())
                event.reply(getClient().getWarning()+" There are no playlists in the Playlists folder!")
                        .setEphemeral(true)
                        .queue();
            else
            {
                StringBuilder builder = new StringBuilder(getClient().getSuccess()+" Available playlists:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString())
                    .setEphemeral(true)
                    .queue();
            }
        }
    }
}

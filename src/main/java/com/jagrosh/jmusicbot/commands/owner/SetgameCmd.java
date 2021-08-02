/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetgameCmd extends OwnerCommand
{
   private SetgameCmd()
   {
        this.subcommandGroup = new SubcommandGroupData("setgame", "Sets game");
   }

    public static class SetnoneCmd extends SetgameCmd
    {
        public SetnoneCmd()
        {
            this.name = "none";
            this.help = "sets the game to nothing";
        }

        @Override
        protected void execute(SlashCommandEvent event)
        {
            event.getJDA().getPresence().setActivity(null);
            event.reply(getClient().getSuccess()+" "+event.getJDA().getSelfUser().getAsMention()+" is no longer playing anything.")
                .setEphemeral(true)
                .queue();
        }
    }

    public static class SetplayingCmd extends SetgameCmd
    {
        public SetplayingCmd()
        {
            this.name = "playing";
            this.help = "sets the game";
            this.arguments = "game";
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "The name to use", true));
        }

        @Override
        protected void execute(SlashCommandEvent event)
        {
            String arg = event.getOption("name").getAsString();
            event.getJDA().getPresence().setActivity(Activity.playing(arg));
            event.reply(getClient().getSuccess()+" "+event.getJDA().getSelfUser().getAsMention()+" is now playing `"+arg+"`")
                    .setEphemeral(true)
                    .queue();

        }
    }

    public static class SetstreamCmd extends SetgameCmd
    {
        public SetstreamCmd()
        {
            this.name = "stream";
            this.aliases = new String[]{"twitch","streaming"};
            this.help = "sets the game the bot is playing to a stream";
            this.arguments = "<username> <game>";
            this.options = Arrays.asList(
                    new OptionData(OptionType.STRING, "username", "The twitch username to use", true),
                    new OptionData(OptionType.STRING, "name", "The name to use", true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event)
        {
            String username = event.getOption("username").getAsString();
            String game = event.getOption("name").getAsString();
            try
            {
                event.getJDA().getPresence().setActivity(Activity.streaming(game, "https://twitch.tv/"+username));
                event.reply(getClient().getSuccess()+" "+event.getJDA().getSelfUser().getAsMention()
                        +" is now streaming `"+game+"`")
                .setEphemeral(true)
                .queue();
            }
            catch(Exception e)
            {
                event.reply(getClient().getError()+" The game could not be set!")
                    .setEphemeral(true)
                    .queue();
            }
        }
    }

    public static class SetlistenCmd extends SetgameCmd
    {
        public SetlistenCmd()
        {
            this.name = "listen";
            this.aliases = new String[]{"listening"};
            this.help = "sets the game the bot is listening to";
            this.arguments = "<title>";
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "The name to use", true));
        }

        @Override
        protected void execute(SlashCommandEvent event)
        {
            String arg = event.getOption("name").getAsString();
            String title = arg.toLowerCase().startsWith("to") ? arg.substring(2).trim() : arg;
            try
            {
                event.getJDA().getPresence().setActivity(Activity.listening(title));
                event.reply(getClient().getSuccess()+" "+event.getJDA().getSelfUser().getAsMention()+" is now listening to `"+title+"`")
                .setEphemeral(true)
                .queue();
            } catch(Exception e) {
                event.reply(getClient().getError()+" The game could not be set!")
                .setEphemeral(true)
                .queue();
            }
        }
    }

    public static class SetwatchCmd extends SetgameCmd
    {
        public SetwatchCmd()
        {
            this.name = "watch";
            this.aliases = new String[]{"watching"};
            this.help = "sets the game the bot is watching";
            this.arguments = "<title>";
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "The name to use", true));
        }

        @Override
        protected void execute(SlashCommandEvent event)
        {
            String title = event.getOption("name").getAsString();
            try
            {
                event.getJDA().getPresence().setActivity(Activity.watching(title));
                event.reply(getClient().getSuccess()+" "+event.getJDA().getSelfUser().getAsMention()+" is now watching `"+title+"`")
                    .setEphemeral(true)
                    .queue();
            } catch(Exception e) {
                event.reply(getClient().getError()+" The game could not be set!")
                    .setEphemeral(true)
                    .queue();
            }
        }
    }
}

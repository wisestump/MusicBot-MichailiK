/*
 * Copyright 2016 John Grosh (jagrosh).
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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class EvalCmd extends OwnerCommand 
{
    private final Bot bot;
    
    public EvalCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "eval";
        this.help = "evaluates nashorn code";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "code", "Nashorn Code to evaluate", true),
                new OptionData(OptionType.BOOLEAN, "ephemeral", "Whether to send a ephemeral response. Defaults to false", false)
        );
    }
    
    @Override
    protected void execute(SlashCommandEvent event)
    {
        if(!bot.getConfig().useEval())
        {
            event.reply("Code evaluation is disabled.").queue();
            return;
        }
        String args = event.getOption("code").getAsString();
        boolean ephemeral;
        if(event.getOption("ephemeral") == null)
            ephemeral = false;
        else
            ephemeral = event.getOption("ephemeral").getAsBoolean();



        ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
        se.put("bot", bot);
        se.put("event", event);
        se.put("jda", event.getJDA());
        se.put("guild", event.getGuild());
        se.put("channel", event.getChannel());

        event.deferReply(ephemeral).queue();
        try
        {
            event.getHook().sendMessage(getClient().getSuccess()+" Evaluated Successfully:\n```\n"+se.eval(args)+" ```").queue();
        } 
        catch(Exception e)
        {
            event.getHook().sendMessage(getClient().getError()+" An exception was thrown:\n```\n"+e+" ```").queue();
        }
    }
    
}

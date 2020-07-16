package com.github.dsipaint;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DMCommandListener extends ListenerAdapter
{
	//listen to DMs for commands
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent e)
	{
		//stops recursive command-calling- would be cool, but generally best to avoid it just to be sure
		if(e.getAuthor().equals(e.getJDA().getSelfUser()))
			return;
		
		/*
		 * command format should be >msg user1,user2,user3,user4 [message]
		 * 
		 * in order to account for possible sabotage/malfuntion due to injection (commas in names),
		 * use the fact that all specified users have tags at the end of their names, which they have no
		 * choice on being there. They are also not allowed # in their names- this imposes some form 
		 * of structure to the list of users. 
		 */
		
		String msg = e.getMessage().getContentRaw();
		
		//if first word is >msg (case insensitive) (assumes BotStrings.PREFIX does not need to be escaped)
		if(msg.matches("(?i)" + BotStrings.PREFIX + "msg .*"))
		{
			//if the command specifies users
			if(msg.matches("(?i)" + BotStrings.PREFIX + "msg " + BotStrings.USERLIST_REGEX + ".*"))
			{
				//if the command also specifies a message
				if(msg.matches("(?i)" + BotStrings.PREFIX + "msg " + BotStrings.USERLIST_REGEX + " .+"))
				{
					//get users from the command string (done this way because names might have spaces)
					Pattern user_list = Pattern.compile(BotStrings.USERLIST_REGEX);
					Matcher usermatcher = user_list.matcher(msg);
					String[] users = usermatcher.group(1) //take the message and extract the userlist using regex
							.split("#\\d{4},"); //split this list into separate users- discord does not allow names to use #s, and we take advantage of that here
					
					//retrieve message string
					String message_to_send = msg.substring((BotStrings.PREFIX + "msg ").length() +  usermatcher.group(1).length());
					
					ArrayList<String> success_names = new ArrayList<String>();
					ArrayList<String> failure_names = new ArrayList<String>();
					
					//loop through users
					for(String user : users)
					{
						User u = e.getJDA().getUserByTag(user);
						if(u != null) //if user is found and known by the bot
						{
							if(!u.isBot()) //if user is not bot
							{
								u.openPrivateChannel().queue(
										channel -> { //send the DM to the user
											channel.sendMessage(BotStrings.MSG_RECEIVED.replace("<user>", e.getAuthor().getName())
													+ ": " + message_to_send).queue(//(does the client want message-sending to be anonymous? If so, remove the first bit of this string)
													send_success -> {
														success_names.add(channel.getUser().getAsTag()); //if sent message, add name to list of successes
													},
													send_error -> {	//a user has blocked the bot
														e.getChannel().sendMessage("Error: User " + user + " has blocked me and the message was not delivered").queue();
														failure_names.add(channel.getUser().getAsTag()); //if didn't send message, add name to list of failures
													});
										});
							}
							else
								e.getChannel().sendMessage("Error: User " + user + " is a bot (cannot send messages to other bots)").queue();
						}
						else
							e.getChannel().sendMessage("Error: User " + user + " was not found").queue();
					}
					
					String completion_msg = "";
					if(success_names.size() > 0)
					{
						completion_msg += "Successfully sent message to ";
						for(String name : success_names)
							completion_msg += name + ",";
						
						completion_msg = completion_msg.substring(0, completion_msg.length() - 1) //remove comma after last name
								+ "\n";
					}
					if(failure_names.size() > 0)
					{
						completion_msg += "Failed to send message to ";
						for(String name : failure_names)
							completion_msg += name + ",";
						
						completion_msg = completion_msg.substring(0, completion_msg.length() - 1); //remove comma after last name
					}
					
					completion_msg += "!";
					
					//if-statements ensure at least one message is sent, and that it was either a success or failure- no blank message is ever sent here
					e.getChannel().sendMessage(completion_msg).queue();
				}
				else
				{
					//send error message- no message specified
					e.getChannel().sendMessage("Error: no message was specified to be sent").queue();
				}
			}
			else
			{
				//send error message- no users specified
				e.getChannel().sendMessage("Error: no users were specified").queue();
			}
		}
	}
}

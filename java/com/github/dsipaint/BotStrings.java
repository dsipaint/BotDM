package com.github.dsipaint;

public class BotStrings
{
	static final String PREFIX = ">",
			MSG_RECEIVED = "New message from <user>", //template message- <user> is replaced with the actual user when the message is sent
			MEMBERS_SENT = "Sent message to <list>!", //template message- <list> is replaced with a list of users the message is sent to
			MEMBERS_NOT_SENT = "Was not able to send message to <list>", //template message- <list> is replaced with a list of users the message is NOT sent to
			USERLIST_REGEX = "(((.+#\\d{4},)+(.+#\\d{4}))|(.+#\\d{4}))"; //regex representing a list of users with tags, separated with commas 
}

package me.blueishberry.bluesreplier;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BluesReplierClient implements ClientModInitializer {

	private static String lastSender = null;
	private static final Pattern whisperPattern = Pattern.compile("^(.*) whispers to you:.*");

	@Override
	public void onInitializeClient() {

		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, timestamp) -> {
			String text = message.getString();
			Matcher matcher = whisperPattern.matcher(text);
			if (matcher.find()) {
				lastSender = matcher.group(1);
			}
		});

		ClientCommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess) -> {
			commandDispatcher.register(ClientCommandManager.literal("r")
					.then(ClientCommandManager.argument("message", StringArgumentType.string())
							.executes(context -> {
								if (lastSender == null) {
									MinecraftClient.getInstance().player.sendMessage(Text.literal("No one has whispered to you yet."), false);
									return 0;
								}

								String msg = StringArgumentType.getString(context, "message");
								String command = "msg " + lastSender + " " + msg;
								MinecraftClient.getInstance().player.networkHandler.sendChatCommand(command);

								return 1;
							})
					)
			);
		}));
	}
}
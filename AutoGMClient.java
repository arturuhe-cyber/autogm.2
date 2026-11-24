package clientside.only.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.GameType;

public class AutoGMClient implements ClientModInitializer {
	private static final double FALL_THRESHOLD = 5.0; // сколько блоков падения до срабатывания
	private static final int COOLDOWN_TICKS = 100;    // 5 секунд между срабатываниями
	private static final int FLY_DELAY_TICKS = 10;    // пауза между /gm 1 и /fly

	private int flyDelay = -1;
	private int cooldown = 0;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
	}

	private void onTick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || client.gameMode == null) {
			flyDelay = -1;
			return;
		}

		if (cooldown > 0) cooldown--;

		if (flyDelay >= 0 && --flyDelay < 0) {
			if (!player.getAbilities().flying) {
				player.connection.sendCommand("fly");
			}
		}

		boolean falling = !player.onGround()
				&& player.fallDistance > FALL_THRESHOLD
				&& !player.getAbilities().flying
				&& !player.isFallFlying()
				&& !player.isInWater();

		if (falling && cooldown == 0) {
			if (client.gameMode.getPlayerMode() != GameType.CREATIVE) {
				player.connection.sendCommand("gm 1");
			}
			flyDelay = FLY_DELAY_TICKS;
			cooldown = COOLDOWN_TICKS;
		}
	}
}

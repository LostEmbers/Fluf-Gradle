package test;

import lostembers.fluf.api.FlufRegister;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TestClass {
	public TestClass() {
		FlufRegister.register(
				Registry.BLOCK_REGISTRY,
				new ResourceLocation("test:test"),
				new Block(BlockBehaviour.Properties.copy(Blocks.STONE))
		);
	}
}

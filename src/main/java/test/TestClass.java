package test;

//import net.minecraft.src.C_4705_;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TestClass {
	public TestClass() {
		System.out.println("hello");
		Registry.register(
				Registry.BLOCK,
				new ResourceLocation("test:test"),
				new Block(BlockBehaviour.Properties.copy(Blocks.STONE))
		);
	}
}
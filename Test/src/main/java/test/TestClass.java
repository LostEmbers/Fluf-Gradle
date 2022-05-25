package test;

import lostembers.fluf.api.FlufRegister;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class TestClass {
	public TestClass() {
		Supplier<Block> testBlock = FlufRegister.register(
				Registry.BLOCK_REGISTRY,
				new ResourceLocation("test:test"),
				// TODO: gradle plugin should automatically box it and give a warning when it does so
				() -> new TestBlock(BlockBehaviour.Properties.copy(Blocks.STONE))
		);
		
		Supplier<Item> testItem = FlufRegister.register(
				Registry.ITEM_REGISTRY,
				new ResourceLocation("test:test"),
				() -> new BlockItem(testBlock.get(), new Item.Properties())
		);
	}
}

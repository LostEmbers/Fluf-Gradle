package test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class TestBlock extends Block {
	public TestBlock(Properties $$0) {
		super($$0);
	}
	
	@Override
	public void fallOn(Level $$0, BlockState $$1, BlockPos $$2, Entity $$3, float $$4) {
		$$3.setDeltaMovement($$3.getDeltaMovement().scale(-30));
	}
	
	@Override
	public void setPlacedBy(Level $$0, BlockPos $$1, BlockState $$2, @Nullable LivingEntity $$3, ItemStack $$4) {
		$$3.setNoGravity(false);
	}
}

package com.mystdev.recicropal.content.vine_patch;

import com.mojang.serialization.Codec;
import com.mystdev.recicropal.content.trellis.TrellisVineBlock;
import com.mystdev.recicropal.content.trellis.VinelikeProps;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class VinePatchFeature extends Feature<VinePatchConfiguration> {
    public VinePatchFeature(Codec<VinePatchConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<VinePatchConfiguration> fpctx) {
        var origin = fpctx.origin();
        var level = fpctx.level();
        var block = fpctx.config().getBlockToPlace();
        if (block.isEmpty()) return false;

        var trellisCropBlock = block.get();
        var mayPlace = trellisCropBlock.mayPlaceOn(level.getBlockState(origin.below()), level, origin);

        if (!mayPlace) return false;
        var randAge = fpctx.random().nextInt(3);
        var stateToPlace = trellisCropBlock.defaultBlockState().setValue(TrellisVineBlock.AGE, randAge);

        var test = level.setBlock(origin, stateToPlace, 2);

        if (!test) return false;
        var vine = block.get().getVineBlock();
        var vineToPlace = vine.defaultBlockState()
                              .setValue(VinelikeProps.DOWN, Boolean.TRUE);
        var config = fpctx.config();
        this.getRandomPositions(
                    config.getSpreadRadius(),
                    config.getHeightRange(),
                    (blockGetter, pos) -> {
                        var mayPlaceOn = vine.mayPlaceOn(level.getBlockState(pos.below()),
                                                         blockGetter,
                                                         pos.below());
                        var isAir = blockGetter.getBlockState(pos).isAir();
                        return mayPlaceOn && isAir;
                    },
                    level, origin, fpctx.random())
            .forEach(pos -> level.setBlock(pos, vineToPlace.setValue(TrellisVineBlock.AGE, fpctx.random().nextInt(3)), 2));

        return true;
    }

    private List<BlockPos> getRandomPositions(int radius,
                                              int heightRange,
                                              BiFunction<BlockGetter, BlockPos, Boolean> placementPredicate,
                                              BlockGetter blockGetter,
                                              BlockPos origin,
                                              RandomSource random) {
        var blockPoss = BlockPos.withinManhattan(origin, radius, heightRange, radius).iterator();
        var result = new ArrayList<BlockPos>();
        while (blockPoss.hasNext()) {
            var pos = new BlockPos.MutableBlockPos();
            pos.move(blockPoss.next());
            if (Math.round(origin.distSqr(pos)) > radius) continue;
            var canPlace = placementPredicate.apply(blockGetter, pos);
            var rand = random.nextBoolean();
            if (canPlace && rand) result.add(pos);
        }

        return result;
    }
}

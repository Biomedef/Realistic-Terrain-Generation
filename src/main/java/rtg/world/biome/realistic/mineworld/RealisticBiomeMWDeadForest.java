package rtg.world.biome.realistic.mineworld;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

import rtg.api.config.BiomeConfig;
import rtg.api.util.BlockUtil;
import rtg.api.util.CliffCalculator;
import rtg.api.util.noise.OpenSimplexNoise;
import rtg.api.world.RTGWorld;
import rtg.world.biome.deco.DecoBase;
import rtg.world.biome.deco.DecoBaseBiomeDecorations;
import rtg.world.biome.deco.DecoBoulder;
import rtg.world.biome.deco.DecoFallenTree;
import rtg.world.biome.deco.helper.DecoHelperRandomSplit;
import rtg.world.gen.surface.SurfaceBase;
import rtg.world.gen.terrain.TerrainBase;
import static rtg.world.biome.deco.DecoFallenTree.LogCondition.NOISE_GREATER_AND_RANDOM_CHANCE;

public class RealisticBiomeMWDeadForest extends RealisticBiomeMWBase {

    public static Biome river = Biomes.RIVER;

    public RealisticBiomeMWDeadForest(Biome biome) {

        super(biome, river);
    }

    @Override
    public void initConfig() {

        this.getConfig().addProperty(this.getConfig().ALLOW_LOGS).set(true);

        this.getConfig().addProperty(this.getConfig().SURFACE_MIX_BLOCK).set("");
        this.getConfig().addProperty(this.getConfig().SURFACE_MIX_BLOCK_META).set(0);
    }

    @Override
    public TerrainBase initTerrain() {

        return new TerrainMWDeadForest(58f, 80f, 30f);
    }

    public class TerrainMWDeadForest extends TerrainBase {

        private float minHeight = 58f;
        private float maxHeight = 120f;
        private float hillStrength = 30f;
        private float deadForestGroundAmplitude = 10f;

        public TerrainMWDeadForest() {

        }

        public TerrainMWDeadForest(float minHeight, float maxHeight, float hillStrength) {

            this.minHeight = minHeight;
            this.maxHeight = (maxHeight > rollingHillsMaxHeight) ? rollingHillsMaxHeight : ((maxHeight < this.minHeight) ? rollingHillsMaxHeight : maxHeight);
            this.hillStrength = hillStrength;
        }

        @Override
        public float generateNoise(RTGWorld rtgWorld, int x, int y, float border, float river) {

            return terrainRollingHills(x, y, rtgWorld.simplex, river, hillStrength, maxHeight, groundNoise, deadForestGroundAmplitude, 0f);
        }
    }

    @Override
    public SurfaceBase initSurface() {

        return new SurfaceMWDeadForest(config,
            this.baseBiome.topBlock, //Block top
            this.baseBiome.fillerBlock, //Block filler,
            this.baseBiome.topBlock, //IBlockState mixTop,
            this.baseBiome.fillerBlock, //IBlockState mixFill,
            80f, //float mixWidth,
            -0.15f, //float mixHeight,
            10f, //float smallWidth,
            0.5f //float smallStrength
        );
    }

    public class SurfaceMWDeadForest extends SurfaceBase {


        private IBlockState blockMixTop;
        private IBlockState blockMixFiller;
        private float floMixWidth;
        private float floMixHeight;
        private float floSmallWidth;
        private float floSmallStrength;

        public SurfaceMWDeadForest(BiomeConfig config, IBlockState top, IBlockState filler, IBlockState mixTop, IBlockState mixFiller,
                                   float mixWidth, float mixHeight, float smallWidth, float smallStrength) {

            super(config, top, filler);

            blockMixTop = this.getConfigBlock(config.SURFACE_MIX_BLOCK.get(), config.SURFACE_MIX_BLOCK_META.get(), mixTop);
            blockMixFiller = mixFiller;

            floMixWidth = mixWidth;
            floMixHeight = mixHeight;
            floSmallWidth = smallWidth;
            floSmallStrength = smallStrength;
        }

        @Override
        public void paintTerrain(ChunkPrimer primer, int i, int j, int x, int z, int depth, RTGWorld rtgWorld, float[] noise, float river, Biome[] base) {

            Random rand = rtgWorld.rand;
            OpenSimplexNoise simplex = rtgWorld.simplex;
            float c = CliffCalculator.calc(x, z, noise);
            boolean cliff = c > 1.4f ? true : false;
            boolean mix = false;

            for (int k = 255; k > -1; k--) {
                Block b = primer.getBlockState(x, k, z).getBlock();
                if (b == Blocks.AIR) {
                    depth = -1;
                }
                else if (b == Blocks.STONE) {
                    depth++;

                    if (cliff) {
                        if (depth > -1 && depth < 2) {
                            if (rand.nextInt(3) == 0) {

                                primer.setBlockState(x, k, z, hcCobble(rtgWorld, i, j, x, z, k));
                            }
                            else {

                                primer.setBlockState(x, k, z, hcStone(rtgWorld, i, j, x, z, k));
                            }
                        }
                        else if (depth < 10) {
                            primer.setBlockState(x, k, z, hcStone(rtgWorld, i, j, x, z, k));
                        }
                    }
                    else {
                        if (depth == 0 && k > 61) {
                            if (simplex.noise2(i / floMixWidth, j / floMixWidth) + simplex.noise2(i / floSmallWidth, j / floSmallWidth)
                                * floSmallStrength > floMixHeight) {
                                primer.setBlockState(x, k, z, blockMixTop);

                                mix = true;
                            }
                            else {
                                primer.setBlockState(x, k, z, topBlock);
                            }
                        }
                        else if (depth < 4) {
                            if (mix) {
                                primer.setBlockState(x, k, z, blockMixFiller);
                            }
                            else {
                                primer.setBlockState(x, k, z, fillerBlock);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void initDecos() {

        DecoBoulder decoBoulder = new DecoBoulder();
        decoBoulder.setBoulderBlock(Blocks.COBBLESTONE.getDefaultState());
        decoBoulder.setChance(24);
        decoBoulder.setMaxY(95);
        decoBoulder.setStrengthFactor(2f);
        this.addDeco(decoBoulder);

        DecoFallenTree decoFallenTree1 = new DecoFallenTree();
        decoFallenTree1.setLoops(1);
        decoFallenTree1.getDistribution().setNoiseDivisor(100f);
        decoFallenTree1.getDistribution().setNoiseFactor(6f);
        decoFallenTree1.getDistribution().setNoiseAddend(0.8f);
        decoFallenTree1.setLogCondition(NOISE_GREATER_AND_RANDOM_CHANCE);
        decoFallenTree1.setLogConditionNoise(0f);
        decoFallenTree1.setLogConditionChance(10);
        decoFallenTree1.setMaxY(100);
        decoFallenTree1.setLogBlock(BlockUtil.getStateLog(1));
        decoFallenTree1.setLeavesBlock(BlockUtil.getStateLeaf(1));
        decoFallenTree1.setMinSize(3);
        decoFallenTree1.setMaxSize(5);

        DecoFallenTree decoFallenTree2 = new DecoFallenTree();
        decoFallenTree2.setLoops(1);
        decoFallenTree2.getDistribution().setNoiseDivisor(100f);
        decoFallenTree2.getDistribution().setNoiseFactor(6f);
        decoFallenTree2.getDistribution().setNoiseAddend(0.8f);
        decoFallenTree2.setLogCondition(NOISE_GREATER_AND_RANDOM_CHANCE);
        decoFallenTree2.setLogConditionNoise(0f);
        decoFallenTree2.setLogConditionChance(10);
        decoFallenTree2.setMaxY(100);
        decoFallenTree2.setLogBlock(BlockUtil.getStateLog(3));
        decoFallenTree2.setLeavesBlock(BlockUtil.getStateLeaf(3));
        decoFallenTree2.setMinSize(3);
        decoFallenTree2.setMaxSize(5);

        DecoHelperRandomSplit decoHelperRandomSplit = new DecoHelperRandomSplit();
        decoHelperRandomSplit.decos = new DecoBase[]{decoFallenTree2, decoFallenTree1};
        decoHelperRandomSplit.chances = new int[]{12, 8};
        this.addDeco(decoHelperRandomSplit, this.getConfig().ALLOW_LOGS.get());

        DecoBaseBiomeDecorations decoBaseBiomeDecorations = new DecoBaseBiomeDecorations();
        this.addDeco(decoBaseBiomeDecorations);
    }
}

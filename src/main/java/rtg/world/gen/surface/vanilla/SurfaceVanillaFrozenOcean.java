package rtg.world.gen.surface.vanilla;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.ChunkPrimer;
import rtg.api.biome.BiomeConfig;
import rtg.api.biome.vanilla.config.BiomeConfigVanillaFrozenOcean;
import rtg.util.CellNoise;
import rtg.util.OpenSimplexNoise;
import rtg.world.gen.surface.SurfaceBase;

import java.util.Random;

public class SurfaceVanillaFrozenOcean extends SurfaceBase
{
    
    private IBlockState mixBlock;
    private float width;
    private float height;
    private float mixCheck;
    private final int sandMetadata = 0;
    
    public SurfaceVanillaFrozenOcean(BiomeConfig config, Block top, Block filler, Block mix, float mixWidth, float mixHeight)
    {
    
        super(config, top, (byte)0, filler, (byte)0);
        
        mixBlock = this.getConfigBlock(config, BiomeConfigVanillaFrozenOcean.surfaceMixBlockId, BiomeConfigVanillaFrozenOcean.surfaceMixBlockMetaId,
                mix.getDefaultState());

        width = mixWidth;
        height = mixHeight;
    }
    
    @Override
    public void paintTerrain(ChunkPrimer primer, int i, int j, int x, int y, int depth, World world, Random rand,
                             OpenSimplexNoise simplex, CellNoise cell, float[] noise, float river, BiomeGenBase[] base)
    {
    
        for (int k = 255; k > -1; k--)
        {
            Block b = primer.getBlockState((y * 16 + x) * 256 + k).getBlock();
            if (b == Blocks.air)
            {
                depth = -1;
            }
            else if (b == Blocks.stone)
            {
                depth++;
                
                if (depth == 0 && k > 0 && k < 63)
                {
                    mixCheck = simplex.noise2(i / width, j / width);
                    
                    if (mixCheck > height) // > 0.27f, i / 12f
                    {
                        primer.setBlockState((y * 16 + x) * 256 + k, mixBlock);
                    }
                    else
                    {
                        primer.setBlockState((y * 16 + x) * 256 + k, topBlock);
                    }
                }
                else if (depth < 4 && k < 63)
                {
                    primer.setBlockState((y * 16 + x) * 256 + k, fillerBlock);
                }

                else if (depth == 0 && k <69){
                    primer.setBlockState((y * 16 + x) * 256 + k, Blocks.sand.getStateFromMeta(sandMetadata));

                }
            }
        }
    }
}

/*
 * Minecraft Forge
 * Copyright (c) 2016-2021.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.debug;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Tests that the values for defaulted optional tags defined in multiple places are combined.
 *
 * <p>The optional tag defined by this mod is deliberately not defined in a data pack, to cause it to 'default' and
 * trigger the behavior being tested.</p>
 *
 * @see <a href="https://github.com/MinecraftForge/MinecraftForge/issues/7570">MinecraftForge/MinecraftForge#7570</>
 */
@Mod(DuplicateOptionalTagTest.MODID)
public class DuplicateOptionalTagTest
{
    private static final Logger LOGGER = LogManager.getLogger();

    static final String MODID = "duplicate_optional_tag_test";
    private static final ResourceLocation TAG_NAME = new ResourceLocation(MODID, "test_optional_tag");

    private static final Set<Supplier<Block>> TAG_A_DEFAULTS = ImmutableSet.of(Blocks.BEDROCK.delegate);
    private static final Set<Supplier<Block>> TAG_B_DEFAULTS = ImmutableSet.of(Blocks.WHITE_WOOL.delegate);

    private static final Tags.IOptionalNamedTag<Block> TAG_A = ForgeTagHandler.createOptionalTag(ForgeRegistries.BLOCKS, TAG_NAME,
            TAG_A_DEFAULTS);
    private static final Tags.IOptionalNamedTag<Block> TAG_B = ForgeTagHandler.createOptionalTag(ForgeRegistries.BLOCKS, TAG_NAME,
            TAG_B_DEFAULTS);

    public DuplicateOptionalTagTest()
    {
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
    }

    private void onServerStarted(FMLServerStartedEvent event)
    {
        if (!TAG_A.isDefaulted())
        {
            LOGGER.warn("First instance of optional tag is not defaulted!");
        }

        if (!TAG_B.isDefaulted())
        {
            LOGGER.warn("Second instance of optional tag is not defaulted!");
        }

        if (!TAG_A.getValues().equals(TAG_B.getValues()))
        {
            LOGGER.error("Values of both optional tag instances are not the same: first instance: {}, second instance: {}",
                    TAG_A.getValues(), TAG_B.getValues());
            return;
        }

        final List<Block> expected = Sets.union(TAG_A_DEFAULTS, TAG_B_DEFAULTS).stream()
                .map(Supplier::get)
                .collect(Collectors.toList());
        if (!TAG_A.getValues().equals(expected))
        {
            LOGGER.error("Values of the optional tag do not match the expected union of their defaults: expected {}, got {}",
                    expected, TAG_A.getValues());
            return;
        }

        LOGGER.info("Optional tag instances match each other and the expected union of their defaults");
    }
}

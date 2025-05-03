/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.extensions.ILevelExtension;

public abstract class LanguageProvider implements DataProvider {
    private final Map<String, String> data = new TreeMap<>();
    private final PackOutput output;
    private final String modid;
    private final String locale;

    public LanguageProvider(PackOutput output, String modid, String locale) {
        this.output = output;
        this.modid = modid;
        this.locale = locale;
    }

    protected abstract void addTranslations();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        addTranslations();

        if (!data.isEmpty())
            return save(cache, this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(this.modid).resolve("lang").resolve(this.locale + ".json"));

        return CompletableFuture.allOf();
    }

    @Override
    public String getName() {
        return "Languages: " + locale + " for mod: " + modid;
    }

    private CompletableFuture<?> save(CachedOutput cache, Path target) {
        // TODO: DataProvider.saveStable handles the caching and hashing already, but creating the JSON Object this way seems unreliable. -C
        JsonObject json = new JsonObject();
        this.data.forEach(json::addProperty);

        return DataProvider.saveStable(cache, json, target);
    }

    public void add(Object key, String value) {
        // TODO: The basic solutions come together in one.
        switch (key) {
            case String s -> add(s, value);
            case Supplier<?> supplier -> add(supplier.get(), value);
            case Block block -> add(block, value);
            case Item item -> add(item, value);
            case ItemStack stack -> add(stack, value);
            case MobEffect effect -> add(effect, value);
            case EntityType<?> type -> add(type, value);
            case TagKey<?> tag -> add(tag, value);
            case TranslatableContents content -> add(content.getKey(), value);
            case Attribute attribute -> add(attribute, value);
            case ResourceKey<?> k -> addResourceKey(k, value);
            default -> {
                throw new IllegalArgumentException("Unknown key: " + key);
            }
        }
    }

    public void addResourceKey(ResourceKey<?> k, String name) {
        k.cast(Registries.ENCHANTMENT).ifPresent(rk -> addEnchantment(rk, name));
        k.cast(Registries.DIMENSION).ifPresent(rk -> addDimension(rk, name));
        k.cast(Registries.ADVANCEMENT).ifPresent(rk -> addAdvancement(rk, name));
        k.cast(Registries.BIOME).ifPresent(rk -> addBiome(rk, name));
    }

    public void addAdvancement(ResourceKey<Advancement> advancement, String name) {
        add(advancement.location().toLanguageKey("advancement", "description"), name);
    }

    public void addEnchantment(ResourceKey<Enchantment> enchantment, String name) {
        add(enchantment.location().toLanguageKey("enchantment"), name);
    }

    public void addBlock(Supplier<? extends Block> key, String name) {
        add(key.get(), name);
    }

    public void add(Block key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addItem(Supplier<? extends Item> key, String name) {
        add(key.get(), name);
    }

    public void add(Item key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addItemStack(Supplier<ItemStack> key, String name) {
        add(key.get(), name);
    }

    public void add(ItemStack key, String name) {
        add(key.getItem().getDescriptionId(), name);
    }

    public void addBiome(ResourceKey<Biome> biome, String name) {
        add(biome.location().toLanguageKey("biome"), name);
    }

    /*
    public void addBiome(Supplier<? extends Biome> key, String name) {
        add(key.get(), name);
    }
    
    public void add(Biome key, String name) {
        add(key.getTranslationKey(), name);
    }
    */

    public void addAttribute(Supplier<? extends Attribute> key, String name) {
        add(key.get(), name);
    }

    public void add(Attribute key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addEffect(Supplier<? extends MobEffect> key, String name) {
        add(key.get(), name);
    }

    public void add(MobEffect key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addEntityType(Supplier<? extends EntityType<?>> key, String name) {
        add(key.get(), name);
    }

    public void add(EntityType<?> key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addTag(Supplier<? extends TagKey<?>> key, String name) {
        add(key.get(), name);
    }

    public void add(TagKey<?> tagKey, String name) {
        add(Tags.getTagTranslationKey(tagKey), name);
    }

    public void add(String key, String value) {
        if (data.put(key, value) != null)
            throw new IllegalStateException("Duplicate translation key " + key);
    }

    public void addDimension(ResourceKey<Level> dimension, String value) {
        add(dimension.location().toLanguageKey(ILevelExtension.TRANSLATION_PREFIX), value);
    }
}

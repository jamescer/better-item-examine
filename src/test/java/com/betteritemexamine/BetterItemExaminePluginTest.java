package com.example;

import com.betteritemexamine.BetterItemExaminePlugin;
import com.betteritemexamine.PassiveEffectRepository;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BetterItemExaminePluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(BetterItemExaminePlugin.class);
        RuneLite.main(args);
    }
}
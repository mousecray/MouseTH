package ru.mousecray.mouseth.patch;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;

public class MouseTHLaunchPlugin implements ILaunchPluginService {
    @Override public String name() { return "mouseth_ldc_transformer"; }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        if (isEmpty || !RawTranslationsLoader.isEnabled()) return EnumSet.noneOf(Phase.class);
        if (RawTranslationsLoader.hasClassTranslations(classType.getClassName())) return EnumSet.of(Phase.AFTER);
        return EnumSet.noneOf(Phase.class);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType, String reason) {
        if (phase == Phase.AFTER) return LdcStringTransformer.transform(classNode);
        return false;
    }

    @Override
    public int processClassWithFlags(Phase phase, ClassNode classNode, Type classType, String reason) {
        return processClass(phase, classNode, classType, reason) ? ComputeFlags.SIMPLE_REWRITE : ComputeFlags.NO_REWRITE;
    }
}
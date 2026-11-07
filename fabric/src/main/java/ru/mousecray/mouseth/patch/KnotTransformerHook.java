package ru.mousecray.mouseth.patch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class KnotTransformerHook {
    private static boolean hooked = false;

    public static synchronized void hook() {
        if (hooked) return;
        hooked = true;

        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null || !cl.getClass().getName().contains("KnotClassLoader")) return;

            Object delegate = null;
            for (Field f : cl.getClass().getDeclaredFields()) {
                if (f.getType().getName().contains("KnotClassDelegate")) {
                    f.setAccessible(true);
                    delegate = f.get(cl);
                    break;
                }
            }
            if (delegate == null) return;

            Field transformerField = null;
            for (Field f : delegate.getClass().getDeclaredFields()) {
                if (IMixinTransformer.class.isAssignableFrom(f.getType())) {
                    transformerField = f;
                    break;
                }
            }
            if (transformerField == null) return;

            transformerField.setAccessible(true);
            IMixinTransformer original = (IMixinTransformer) transformerField.get(delegate);
            if (original == null) return;

            IMixinTransformer wrapped = (IMixinTransformer) Proxy.newProxyInstance(
                    cl,
                    new Class<?>[]{ IMixinTransformer.class },
                    (proxy, method, args) -> {
                        if ("transformClassBytes".equals(method.getName()) && args != null && args.length >= 3) {
                            byte[] bytes = (byte[]) method.invoke(original, args);
                            if (bytes == null) bytes = (byte[]) args[2];
                            if (bytes != null && args[0] instanceof String name) bytes = applyTransformation(name, bytes);
                            return bytes;
                        }
                        return method.invoke(original, args);
                    }
            );

            transformerField.set(delegate, wrapped);
        } catch (Throwable ignored) { }
    }

    private static byte[] applyTransformation(String className, byte[] bytes) {
        if (!RawTranslationsLoader.hasClassTranslations(className)) return bytes;

        try {
            ClassReader cr = new ClassReader(bytes);
            ClassNode   cn = new ClassNode();
            cr.accept(cn, 0);

            boolean modified = LdcStringTransformer.transform(cn);
            if (modified) {
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                cn.accept(cw);
                return cw.toByteArray();
            }
        } catch (Throwable ignored) { }

        return bytes;
    }
}
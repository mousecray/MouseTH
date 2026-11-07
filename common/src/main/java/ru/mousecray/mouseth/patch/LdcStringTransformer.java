package ru.mousecray.mouseth.patch;

import org.objectweb.asm.tree.*;
import ru.mousecray.mouseth.utils.LdcEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LdcStringTransformer {
    public static boolean transform(ClassNode classNode) {
        if (!RawTranslationsLoader.isEnabled()) return false;

        Map<String, Map<String, Map<String, String>>> methodMap = RawTranslationsLoader.getMethodTranslations(classNode.name);
        if (methodMap == null || methodMap.isEmpty()) return false;

        boolean modified          = false;
        boolean bindToLineNumbers = RawTranslationsLoader.isBindToLineNumbers();

        for (MethodNode method : classNode.methods) {
            String                           methodSig   = method.name + method.desc;
            Map<String, Map<String, String>> selectorMap = methodMap.get(methodSig);
            if (selectorMap == null) selectorMap = methodMap.get(method.name);
            if (selectorMap == null) continue;

            Map<String, List<LdcEntry>> occurrencesByString = new HashMap<>();
            int                         currentLine         = 1;

            for (int i = 0; i < method.instructions.size(); i++) {
                AbstractInsnNode insn = method.instructions.get(i);
                if (insn instanceof LineNumberNode lnn) currentLine = lnn.line;
                else if (insn instanceof LdcInsnNode ldcNode && ldcNode.cst instanceof String str) {
                    LdcEntry entry = new LdcEntry(ldcNode, str, currentLine);
                    occurrencesByString.computeIfAbsent(str, k -> new ArrayList<>()).add(entry);
                }
            }

            for (List<LdcEntry> list : occurrencesByString.values()) {
                for (int i = 0; i < list.size(); i++) list.get(i).setOccurrenceIndex(i);
            }

            if (!bindToLineNumbers) {
                for (Map<String, String> strEntries : selectorMap.values()) {
                    for (Map.Entry<String, String> entry : strEntries.entrySet()) {
                        List<LdcEntry> targets = occurrencesByString.get(entry.getKey());
                        if (targets != null) {
                            for (LdcEntry target : targets) {
                                target.getNode().cst = entry.getValue();
                                modified = true;
                            }
                        }
                    }
                }
            } else {
                for (Map.Entry<String, Map<String, String>> selectorEntry : selectorMap.entrySet()) {
                    String              selector     = selectorEntry.getKey();
                    Map<String, String> translations = selectorEntry.getValue();

                    for (Map.Entry<String, String> strEntry : translations.entrySet()) {
                        String         original   = strEntry.getKey();
                        String         translated = strEntry.getValue();
                        List<LdcEntry> targets    = occurrencesByString.get(original);
                        if (targets == null || targets.isEmpty()) continue;

                        if (selector.equals("*")) {
                            for (LdcEntry target : targets) {
                                target.getNode().cst = translated;
                                modified = true;
                            }
                        } else if (selector.startsWith("*+")) {
                            try {
                                int count = Integer.parseInt(selector.substring(2));
                                int limit = Math.min(count, targets.size());
                                for (int i = 0; i < limit; i++) {
                                    targets.get(i).getNode().cst = translated;
                                    modified = true;
                                }
                            } catch (Exception ignored) { }
                        } else if (selector.startsWith("*-")) {
                            try {
                                int count = Integer.parseInt(selector.substring(2));
                                int limit = Math.min(count, targets.size());
                                int start = Math.max(0, targets.size() - limit);
                                for (int i = start; i < targets.size(); i++) {
                                    targets.get(i).getNode().cst = translated;
                                    modified = true;
                                }
                            } catch (Exception ignored) { }
                        } else {
                            try {
                                int targetLine = Integer.parseInt(selector);
                                for (LdcEntry target : targets) {
                                    if (target.getLine() == targetLine) {
                                        target.getNode().cst = translated;
                                        modified = true;
                                    }
                                }
                            } catch (Exception ignored) { }
                        }
                    }
                }
            }
        }

        return modified;
    }
}
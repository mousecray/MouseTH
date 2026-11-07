package ru.mousecray.mouseth.scanner;

import com.google.gson.JsonObject;
import net.minecraft.locale.Language;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import ru.mousecray.mouseth.config.MouseTHConfig;
import ru.mousecray.mouseth.utils.ExportData;
import ru.mousecray.mouseth.utils.Stats;

import java.util.function.Function;

class MouseTHClassVisitor extends ClassVisitor {
    private final String                    className;
    private final ExportData                data;
    private final Language                  lang;
    private final MouseTHConfig             config;
    private final Stats                     stats;
    private final Function<String, Boolean> strValidator;

    public MouseTHClassVisitor(String className, ExportData data, Language lang, MouseTHConfig config, Stats stats, Function<String, Boolean> strValidator) {
        super(Opcodes.ASM9);
        this.className = className;
        this.data = data;
        this.lang = lang;
        this.config = config;
        this.stats = stats;
        this.strValidator = strValidator;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        String methodSig = name + descriptor;
        return new MethodVisitor(Opcodes.ASM9) {
            private int currentLine = 1;

            @Override
            public void visitLineNumber(int line, Label start) {
                currentLine = line;
                super.visitLineNumber(line, start);
            }

            @Override
            public void visitLdcInsn(Object value) {
                if (value instanceof String str) {
                    processFoundBytecodeString(str, className, methodSig, currentLine, data, lang, config, stats);
                }
                super.visitLdcInsn(value);
            }
        };
    }

    private void processFoundBytecodeString(String str, String className, String methodSig, int line, ExportData data, Language lang, MouseTHConfig config, Stats stats) {
        boolean looksLikeKey = str.matches("^[a-zA-Z0-9_.-]+(\\.[a-zA-Z0-9_.-]+)+$");
        if (looksLikeKey && (lang.has(str) || data.getKnownKeys().contains(str))) {
            return;
        }

        if (strValidator.apply(str)) {
            JsonObject classObj  = data.getRaw().has(className) ? data.getRaw().getAsJsonObject(className) : new JsonObject();
            JsonObject methodObj = classObj.has(methodSig) ? classObj.getAsJsonObject(methodSig) : new JsonObject();
            String     lineKey   = String.valueOf(line);
            JsonObject lineObj   = methodObj.has(lineKey) ? methodObj.getAsJsonObject(lineKey) : new JsonObject();

            if (!lineObj.has(str)) {
                lineObj.addProperty(str, str);
                methodObj.add(lineKey, lineObj);
                classObj.add(methodSig, methodObj);
                data.getRaw().add(className, classObj);
                stats.setStringsFound(stats.getStringsFound() + 1);
            }
        }
    }
}

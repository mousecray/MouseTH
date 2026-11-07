package ru.mousecray.mouseth.utils;

import org.objectweb.asm.tree.LdcInsnNode;

public class LdcEntry {
    private final LdcInsnNode node;
    private final String      text;
    private final int         line;
    private       int         occurrenceIndex;

    public LdcEntry(LdcInsnNode node, String text, int line) {
        this.node = node;
        this.text = text;
        this.line = line;
    }
    
    public LdcInsnNode getNode()                        { return node; }
    public String getText()                             { return text; }
    public int getLine()                                { return line; }
    public int getOccurrenceIndex()                     { return occurrenceIndex; }
    public void setOccurrenceIndex(int occurrenceIndex) { this.occurrenceIndex = occurrenceIndex; }
}

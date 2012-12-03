package com.headius.indifier;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A tool to translate at a bytecode level some or all calls on a given class
 * to invokedynamic dispatches with appropriate bootstrapping logic.
 */
public class Indifier {
    public static void main(String[] args) throws IOException {
        String classFile = args[0];

        ClassReader cr = new ClassReader(new FileInputStream(classFile));
        ClassWriter cw = new ClassWriter(Opcodes.ASM4);
        IndifierClassVisitor cv = new IndifierClassVisitor(cw);

        cr.accept(cv, 0);

        byte[] bytes = cw.toByteArray();

        new FileOutputStream(args[1]).write(bytes);
    }

    public static class IndifierClassVisitor extends ClassVisitor {
        private ClassWriter cw;
        public IndifierClassVisitor(ClassWriter cw) {
            super(Opcodes.ASM4);
            this.cw = cw;
        }

        public void visit(int i, int i1, java.lang.String s, java.lang.String s1, java.lang.String s2, java.lang.String[] strings) {
            cw.visit(i, i1, s, s1, s2, strings);
        }

        public void visitSource(java.lang.String s, java.lang.String s1) {
            cw.visitSource(s, s1);
        }

        public void visitOuterClass(java.lang.String s, java.lang.String s1, java.lang.String s2) {
            cw.visitOuterClass(s, s1, s2);
        }

        public org.objectweb.asm.AnnotationVisitor visitAnnotation(java.lang.String s, boolean b) {
            return cw.visitAnnotation(s, b);
        }

        public void visitAttribute(org.objectweb.asm.Attribute attribute) {
            cw.visitAttribute(attribute);
        }

        public void visitInnerClass(java.lang.String s, java.lang.String s1, java.lang.String s2, int i) {
            cw.visitInnerClass(s, s1, s2, i);
        }

        public org.objectweb.asm.FieldVisitor visitField(int i, java.lang.String s, java.lang.String s1, java.lang.String s2, java.lang.Object o) {
            return cw.visitField(i, s, s1, s2, o);
        }

        public org.objectweb.asm.MethodVisitor visitMethod(int i, java.lang.String s, java.lang.String s1, java.lang.String s2, java.lang.String[] strings) {
            return new IndifierMethodVisitor(cw.visitMethod(i, s, s1, s2, strings));
        }

        public void visitEnd() {
            cw.visitEnd();
        }
    }

    public static class IndifierMethodVisitor extends MethodVisitor {
        private MethodVisitor delegate;

        public IndifierMethodVisitor(MethodVisitor delegate) {
            super(Opcodes.ASM4, delegate);
            this.delegate = delegate;
        }

        public org.objectweb.asm.AnnotationVisitor visitAnnotationDefault() {
            return delegate.visitAnnotationDefault();
        }

        public org.objectweb.asm.AnnotationVisitor visitAnnotation(java.lang.String s, boolean b) {
            return delegate.visitAnnotation(s, b);
        }

        public org.objectweb.asm.AnnotationVisitor visitParameterAnnotation(int i, java.lang.String s, boolean b) {
            return delegate.visitParameterAnnotation(i, s, b);
        }

        public void visitAttribute(org.objectweb.asm.Attribute attribute) {
            delegate.visitAttribute(attribute);
        }

        public void visitCode() {
            delegate.visitCode();
        }

        public void visitFrame(int i, int i1, java.lang.Object[] objects, int i2, java.lang.Object[] objects1) {
            delegate.visitFrame(i, i1, objects, i2, objects1);
        }

        public void visitInsn(int i) {
            delegate.visitInsn(i);
        }

        public void visitIntInsn(int i, int i1) {
            delegate.visitIntInsn(i, i1);
        }

        public void visitVarInsn(int i, int i1) {
            delegate.visitVarInsn(i, i1);
        }

        public void visitTypeInsn(int i, java.lang.String s) {
            delegate.visitTypeInsn(i, s);
        }

        public void visitFieldInsn(int i, java.lang.String s, java.lang.String s1, java.lang.String s2) {
            delegate.visitFieldInsn(i, s, s1, s2);
        }

        public void visitMethodInsn(int i, java.lang.String s, java.lang.String s1, java.lang.String s2) {
            Type desc = Type.getMethodType(s2);
            Type[] params = desc.getArgumentTypes();

            switch (i) {
                case Opcodes.INVOKEINTERFACE:
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKESPECIAL:
                    Type[] newParams = new Type[params.length + 1];
                    newParams[0] = Type.getType(s);
                    System.arraycopy(params, 0, newParams, 1, params.length);
                    params = newParams;
            }

            switch (i) {
                case Opcodes.INVOKEINTERFACE:
                    visitInvokeDynamicInsn(
                            "invokeinterface:" + s1,
                            Type.getMethodDescriptor(desc.getReturnType(), params),
                            new Handle(Opcodes.H_INVOKESTATIC, Type.getType(Object.class).getInternalName(), "bootstrap", "()V"));
                    break;
                case Opcodes.INVOKEVIRTUAL:
                    visitInvokeDynamicInsn(
                            "invokevirtual:" + s1,
                            Type.getMethodDescriptor(desc.getReturnType(), params),
                            new Handle(Opcodes.H_INVOKESTATIC, Type.getType(Object.class).getInternalName(), "bootstrap", "()V"));
                    break;
                case Opcodes.INVOKESPECIAL:
                    visitInvokeDynamicInsn(
                            "invokespecial:" + s1,
                            Type.getMethodDescriptor(desc.getReturnType(), params),
                            new Handle(Opcodes.H_INVOKESTATIC, Type.getType(Object.class).getInternalName(), "bootstrap", "()V"));
                    break;
                case Opcodes.INVOKESTATIC:
                    visitInvokeDynamicInsn(
                            "invokestatic:" + s1,
                            Type.getMethodDescriptor(desc.getReturnType(), params),
                            new Handle(Opcodes.H_INVOKESTATIC, Type.getType(Object.class).getInternalName(), "bootstrap", "()V"));
                    break;
            }
        }

        public void visitInvokeDynamicInsn(java.lang.String s, java.lang.String s1, org.objectweb.asm.Handle handle, java.lang.Object... objects) {
            System.out.println("invokedynamic: " + s + " " + s1 + " " + handle);
            delegate.visitInvokeDynamicInsn(s, s1, handle, objects);
        }

        public void visitJumpInsn(int i, org.objectweb.asm.Label label) {
            delegate.visitJumpInsn(i, label);
        }

        public void visitLabel(org.objectweb.asm.Label label) {
            delegate.visitLabel(label);
        }

        public void visitLdcInsn(java.lang.Object o) {
            delegate.visitLdcInsn(o);
        }

        public void visitIincInsn(int i, int i1) {
            delegate.visitIincInsn(i, i1);
        }

        public void visitTableSwitchInsn(int i, int i1, org.objectweb.asm.Label label, org.objectweb.asm.Label... labels) {
            delegate.visitTableSwitchInsn(i, i1, label, labels);
        }

        public void visitLookupSwitchInsn(org.objectweb.asm.Label label, int[] ints, org.objectweb.asm.Label[] labels) {
            delegate.visitLookupSwitchInsn(label, ints, labels);
        }

        public void visitMultiANewArrayInsn(java.lang.String s, int i) {
            delegate.visitMultiANewArrayInsn(s, i);
        }

        public void visitTryCatchBlock(org.objectweb.asm.Label label, org.objectweb.asm.Label label1, org.objectweb.asm.Label label2, java.lang.String s) {
            delegate.visitTryCatchBlock(label, label1, label2, s);
        }

        public void visitLocalVariable(java.lang.String s, java.lang.String s1, java.lang.String s2, org.objectweb.asm.Label label, org.objectweb.asm.Label label1, int i) {
            delegate.visitLocalVariable(s, s1, s2, label, label1, i);
        }

        public void visitLineNumber(int i, org.objectweb.asm.Label label) {
            delegate.visitLineNumber(i, label);
        }

        public void visitMaxs(int i, int i1) {
            delegate.visitMaxs(i, i1);
        }

        public void visitEnd() {
            delegate.visitEnd();
        }
    }
}

package ru.merfemor.compactset

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import java.util.function.Consumer
import kotlin.reflect.KClass


/**
 * Generates [CompactSet] implementation class bytecode.
 * Implementation extends [AbstractHashCompactSet] and implements required methods.
 * It uses primitive array to store hash table elements.
 * `0` is treated as special value to indicate free hash table cells.
 * Because of this we need extra field [SPECIAL_EMPTY_VAL_ADDED_FIELD] to check whether `0` is added to set.
 *
 * @param typeParameter type parameter T in [CompactSet]
 * @param canonicalName canonical name for new generated class
 */
internal class PrimitiveTypeCompactSetClassBytecodeGenerator<T>(
    private val typeParameter: Class<T>,
    private val canonicalName: String
) {
    // TODO: generalize for Double + Long
    private val arrayTypeDescriptor = IntArray::class.descriptor
    private val arrayType = T_INT

    fun generateImplClassBytecode(): ByteArray {
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        addClassDeclaration(classWriter)
        addFields(classWriter)
        addConstructor(classWriter)
        addMethodImplementations(classWriter)
        classWriter.visitEnd()
        return classWriter.toByteArray()
    }

    private fun addClassDeclaration(cv: ClassVisitor) {
        val superClass = AbstractHashCompactSet::class.internalName
        val signature = "L$superClass<${typeParameter.descriptor}>;"
        cv.visit(
            V1_8, ACC_SUPER or ACC_FINAL or ACC_PUBLIC,
            canonicalName.toInternalName(),
            signature,
            superClass,
            arrayOf(CompactSet::class.internalName)
        )
    }

    private fun addFields(cv: ClassVisitor) {
        cv.visitField(ACC_PRIVATE, HASH_TABLE_FIELD, arrayTypeDescriptor, null, null)
        cv.visitField(ACC_PRIVATE, SPECIAL_EMPTY_VAL_ADDED_FIELD,
            Type.BOOLEAN_TYPE.descriptor, null, null)
    }

    private fun addConstructor(cv: ClassVisitor) {
        val mv = cv.visitMethod(ACC_PUBLIC, "<init>", "(I)V", null, null)
        mv.visitCode()
        // call super()
        mv.visitVarInsn(ALOAD, 0) // this
        mv.visitVarInsn(ILOAD, 1) // expectedSize
        mv.visitMethodInsn(INVOKESPECIAL, AbstractHashCompactSet::class.internalName,
            "<init>", "(I)V", false)

        // initialize hash table with an empty array
        mv.visitVarInsn(ALOAD, 0) // this
        mv.visitInsn(ICONST_0)
        mv.visitIntInsn(NEWARRAY, arrayType)
        mv.visitFieldInsn(PUTFIELD, canonicalName.toInternalName(), HASH_TABLE_FIELD, IntArray::class.descriptor)

        mv.visitInsn(RETURN)
        mv.visitMaxsAndEnd()
    }

    private fun addMethodImplementations(cv: ClassVisitor) {
        addMethodGetHashTableSize(cv)
        addMethodGetElementAt(cv)
        addMethodSetElementAt(cv)
        addMethodResizeHashTable(cv)
        addMethodCellIsFree(cv)
        addMethodContains(cv)
        addMethodAdd(cv)
    }

    private fun addMethodGetHashTableSize(cv: ClassVisitor) {
        val mv = cv.visitMethod(ACC_PROTECTED, "getHashTableSize", "()I", null, null)
        mv.visitCode()
        // get array.length
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, canonicalName.toInternalName(), HASH_TABLE_FIELD, arrayTypeDescriptor)
        mv.visitInsn(ARRAYLENGTH)
        mv.visitInsn(IRETURN)
        mv.visitMaxsAndEnd()
    }

    private fun addMethodGetElementAt(cv: ClassVisitor) {
        val mv = cv.visitMethod(ACC_PROTECTED, "getElementAt",
            "(I)${Object::class.descriptor}", null, null)
        mv.visitCode()

        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, canonicalName.toInternalName(), HASH_TABLE_FIELD, arrayTypeDescriptor)
        mv.visitVarInsn(ILOAD, 1)
        mv.visitInsn(IALOAD)
        mv.visitInsn(DUP)
        mv.visitVarInsn(ISTORE, 2) // save array[i] in local var

        // if array[index] == 0
        val cellIsNotEmpty = Label()
        mv.visitJumpInsn(IFNE, cellIsNotEmpty)

        // return null
        mv.visitInsn(ACONST_NULL)
        mv.visitInsn(ARETURN)

        // else return Integer.valueOf(array[i])
        mv.visitLabel(cellIsNotEmpty)
        mv.visitVarInsn(ILOAD, 2)
        mv.visitMethodInsn(INVOKESTATIC, Integer::class.internalName, "valueOf",
            "(I)${Integer::class.descriptor}", false)
        mv.visitInsn(ARETURN)
        mv.visitMaxsAndEnd()
    }

    private fun addMethodSetElementAt(cv: ClassVisitor) {
        val mv = cv.visitMethod(ACC_PROTECTED, "setElementAt", "(I${Object::class.descriptor})V", null, null)
        mv.visitCode()
        // array[i] = ((Integer) element).intValue()
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, canonicalName.toInternalName(), HASH_TABLE_FIELD, arrayTypeDescriptor)
        mv.visitVarInsn(ILOAD, 1)
        mv.visitVarInsn(ALOAD, 2)
        mv.visitTypeInsn(CHECKCAST, Integer::class.internalName)
        mv.visitMethodInsn(INVOKEVIRTUAL, Integer::class.internalName, "intValue", "()I", false)
        mv.visitInsn(IASTORE)
        mv.visitInsn(RETURN)
        mv.visitMaxsAndEnd()
    }

    private fun addMethodResizeHashTable(cv: ClassVisitor) {
        val mv = cv.visitMethod(ACC_PROTECTED, "resizeHashTable",
            "(I${Consumer::class.descriptor})V",
            "(IL${Consumer::class.internalName}<${Integer::class.descriptor}>;)V", null)
        mv.visitCode()
        // get array
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, canonicalName.toInternalName(), HASH_TABLE_FIELD, arrayTypeDescriptor)
        // save in local variable
        mv.visitVarInsn(ASTORE, 3)

        // array = new int[newSize]
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ILOAD, 1)
        mv.visitIntInsn(NEWARRAY, arrayType)
        mv.visitFieldInsn(PUTFIELD, canonicalName.toInternalName(), HASH_TABLE_FIELD, arrayTypeDescriptor)

        // if oldArray == null
        mv.visitVarInsn(ALOAD, 3)
        val endOfMethod = Label()
        mv.visitJumpInsn(IFNULL, endOfMethod)

        // for (int i = 0; i < oldArray.length; i++)
        mv.visitVarInsn(ALOAD, 3)
        mv.visitInsn(ARRAYLENGTH)
        mv.visitVarInsn(ISTORE, 4)
        mv.visitInsn(ICONST_0) // i
        mv.visitVarInsn(ISTORE, 5)

        val startOfCycle = Label()
        mv.visitLabel(startOfCycle)
        mv.visitVarInsn(ILOAD, 5)
        mv.visitVarInsn(ILOAD, 4) // length
        mv.visitJumpInsn(IF_ICMPGE, endOfMethod) // if i >= length

        // oldArray[i]
        mv.visitVarInsn(ALOAD, 3) // oldArray
        mv.visitVarInsn(ILOAD, 5) // i
        mv.visitInsn(IALOAD)
        mv.visitInsn(DUP)
        mv.visitVarInsn(ISTORE, 7)

        // if oldArray[i] == 0
        val gotoNextElement = Label()
        mv.visitJumpInsn(IFEQ, gotoNextElement)

        // transferOldElement.accept(Integer.valueOf(oldArray[i]))
        mv.visitVarInsn(ALOAD, 2) // transferOldElement
        mv.visitVarInsn(ILOAD, 7)
        mv.visitMethodInsn(INVOKESTATIC, Integer::class.internalName, "valueOf",
            "(I)${Integer::class.descriptor}", false)
        mv.visitMethodInsn(INVOKEINTERFACE, Consumer::class.internalName, "accept",
            "(${Object::class.descriptor})V", true)

        mv.visitLabel(gotoNextElement)
        mv.visitIincInsn(5, 1) // i++
        mv.visitJumpInsn(GOTO, startOfCycle)

        mv.visitLabel(endOfMethod)
        mv.visitInsn(RETURN)
        mv.visitMaxsAndEnd()
    }

    private fun addMethodCellIsFree(cv: ClassVisitor) {
        val mv = cv.visitMethod(ACC_PROTECTED, "cellIsFree", "(I)Z", null, null)
        mv.visitCode()

        // array[index]
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, canonicalName.toInternalName(), HASH_TABLE_FIELD, arrayTypeDescriptor)
        mv.visitVarInsn(ILOAD, 1)
        mv.visitInsn(IALOAD)

        // if array[index] != 0
        val returnFalse = Label()
        mv.visitJumpInsn(IFNE, returnFalse)
        // return true
        mv.visitInsn(ICONST_1)
        mv.visitInsn(IRETURN)
        mv.visitLabel(returnFalse)
        mv.visitInsn(ICONST_0)
        mv.visitInsn(IRETURN)
        mv.visitMaxsAndEnd()
    }

    private fun addMethodContains(cv: ClassVisitor) {
        val descriptor = "(${Object::class.descriptor})Z"
        val mv = cv.visitMethod(ACC_PUBLIC, "contains", descriptor, null, null)
        mv.visitCode()

        // if (value == null) call super
        mv.visitVarInsn(ALOAD, 1)
        val callSuper = Label()
        mv.visitJumpInsn(IFNULL, callSuper)

        // else if ((Integer) value).intValue() == 0
        mv.visitVarInsn(ALOAD, 1)
        mv.visitTypeInsn(CHECKCAST, Integer::class.internalName)
        mv.visitMethodInsn(INVOKEVIRTUAL, Integer::class.internalName, "intValue", "()I", false)
        mv.visitJumpInsn(IFNE, callSuper)

        // return specialEmptyValAdded
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, canonicalName.toInternalName(), SPECIAL_EMPTY_VAL_ADDED_FIELD, "Z")
        mv.visitInsn(IRETURN)

        // else return super.contains(value);
        mv.visitLabel(callSuper)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitMethodInsn(INVOKESPECIAL, AbstractHashCompactSet::class.internalName, "contains", descriptor, false)
        mv.visitInsn(IRETURN)
        mv.visitMaxsAndEnd()
    }

    private fun addMethodAdd(cv: ClassVisitor) {
        val descriptor = "(${Object::class.descriptor})Z"
        val mv = cv.visitMethod(ACC_PUBLIC, "add", descriptor, null, null)
        mv.visitCode()

        // if (value == null) call super
        mv.visitVarInsn(ALOAD, 1)
        val callSuper = Label()
        mv.visitJumpInsn(IFNULL, callSuper)

        // else if ((Integer) value).intValue() == 0
        mv.visitVarInsn(ALOAD, 1)
        mv.visitTypeInsn(CHECKCAST, Integer::class.internalName)
        mv.visitMethodInsn(INVOKEVIRTUAL, Integer::class.internalName, "intValue", "()I", false)
        mv.visitJumpInsn(IFNE, callSuper)

        // if (specialEmptyValAdded)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, canonicalName.toInternalName(), SPECIAL_EMPTY_VAL_ADDED_FIELD, "Z")
        val specialEmptyValAddedIsFalse = Label()
        mv.visitJumpInsn(IFEQ, specialEmptyValAddedIsFalse)

        // return false
        mv.visitInsn(ICONST_0)
        mv.visitInsn(IRETURN)

        // else
        mv.visitLabel(specialEmptyValAddedIsFalse)

        // specialEmptyValAdded = true
        mv.visitVarInsn(ALOAD, 0)
        mv.visitInsn(ICONST_1)
        mv.visitFieldInsn(PUTFIELD, canonicalName.toInternalName(), SPECIAL_EMPTY_VAL_ADDED_FIELD, "Z")

        // setSize(getSize() + 1)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKEVIRTUAL, canonicalName.toInternalName(), "getSize", "()I", false)
        mv.visitInsn(ICONST_1)
        mv.visitInsn(IADD)
        mv.visitMethodInsn(INVOKEVIRTUAL, canonicalName.toInternalName(), "setSize", "(I)V", false)

        // return true
        mv.visitInsn(ICONST_1)
        mv.visitInsn(IRETURN)

        // else return super.contains(value);
        mv.visitLabel(callSuper)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitMethodInsn(INVOKESPECIAL, AbstractHashCompactSet::class.internalName, "add", descriptor, false)
        mv.visitInsn(IRETURN)
        mv.visitMaxsAndEnd()
    }

    private fun String.toInternalName(): String = replace('.', '/')

    private val <T : Any> KClass<T>.internalName: String
        get() = Type.getInternalName(this.java)

    private val <T> Class<T>.descriptor: String
        get() = Type.getDescriptor(this)

    private val <T : Any> KClass<T>.descriptor: String
        get() = this.java.descriptor

    private fun MethodVisitor.visitMaxsAndEnd() {
        visitMaxs(0, 0) // computed automatically
        visitEnd()
    }

    private companion object {
        private const val HASH_TABLE_FIELD = "array"
        private const val SPECIAL_EMPTY_VAL_ADDED_FIELD = "specialEmptyValAdded"
    }
}
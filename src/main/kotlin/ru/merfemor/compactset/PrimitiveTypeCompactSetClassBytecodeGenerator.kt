package ru.merfemor.compactset

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import java.util.function.Consumer


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
    canonicalName: String
) {
    private val internalClassName = canonicalName.toInternalName()
    private val typeInfo = findTypeInfo(typeParameter)
    private fun findTypeInfo(typeParameter: Class<T>): CompactSetPrimitiveTypeInfo {
        for (value in CompactSetPrimitiveTypeInfo.values()) {
            if (value.primitiveType.javaObjectType == typeParameter ||
                value.primitiveType.javaPrimitiveType == typeParameter) {
                return value
            }
        }
        throw IllegalArgumentException("TypeParameter $typeParameter is not supported")
    }

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
            internalClassName,
            signature,
            superClass,
            arrayOf(CompactSet::class.internalName)
        )
    }

    private fun addFields(cv: ClassVisitor) {
        cv.visitField(ACC_PRIVATE, HASH_TABLE_FIELD, typeInfo.primitiveArrayType.descriptor, null, null)
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
        mv.visitIntInsn(NEWARRAY, typeInfo.newarrayType)
        mv.visitFieldInsn(
            PUTFIELD,
            internalClassName,
            HASH_TABLE_FIELD,
            typeInfo.primitiveArrayType.descriptor
        )

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
        mv.visitFieldInsn(
            GETFIELD,
            internalClassName,
            HASH_TABLE_FIELD,
            typeInfo.primitiveArrayType.descriptor
        )
        mv.visitInsn(ARRAYLENGTH)
        mv.visitInsn(IRETURN)
        mv.visitMaxsAndEnd()
    }

    private fun addMethodGetElementAt(cv: ClassVisitor) {
        val mv = cv.visitMethod(ACC_PROTECTED, "getElementAt",
            "(I)${Object::class.descriptor}", null, null)
        mv.visitCode()

        // array[i]
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(
            GETFIELD,
            internalClassName,
            HASH_TABLE_FIELD,
            typeInfo.primitiveArrayType.descriptor
        )
        mv.visitVarInsn(ILOAD, 1)
        mv.visitInsn(typeInfo.aloadInsn)
        mv.visitInsn(typeInfo.dupInsn)
        mv.visitVarInsn(typeInfo.storeInsn, 2) // save array[i] in local var

        // if array[index] == 0
        val cellIsNotEmpty = Label()
        addIfInsnWithTypecast(mv, IFNE, cellIsNotEmpty)

        // return null
        mv.visitInsn(ACONST_NULL)
        mv.visitInsn(ARETURN)

        // else return Integer.valueOf(array[i]) - example for T=int
        mv.visitLabel(cellIsNotEmpty)
        mv.visitVarInsn(typeInfo.loadInsn, 2)
        mv.visitMethodInsn(INVOKESTATIC, typeInfo.wrapperType.internalName, "valueOf",
            "(${typeInfo.primitiveType.descriptor})${typeInfo.wrapperType.descriptor}", false)
        mv.visitInsn(ARETURN)
        mv.visitMaxsAndEnd()
    }

    private fun addMethodSetElementAt(cv: ClassVisitor) {
        val mv = cv.visitMethod(ACC_PROTECTED, "setElementAt", "(I${Object::class.descriptor})V", null, null)
        mv.visitCode()
        // array[i] = ((Integer) element).intValue() - example for T=int
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(
            GETFIELD,
            internalClassName,
            HASH_TABLE_FIELD,
            typeInfo.primitiveArrayType.descriptor
        )
        mv.visitVarInsn(ILOAD, 1)
        mv.visitVarInsn(ALOAD, 2)
        mv.visitTypeInsn(CHECKCAST, typeInfo.wrapperType.internalName)
        mv.visitMethodInsn(INVOKEVIRTUAL, typeInfo.wrapperType.internalName, typeInfo.unboxingMethodName,
            "()${typeInfo.primitiveType.descriptor}", false)
        mv.visitInsn(typeInfo.astoreInsn)
        mv.visitInsn(RETURN)
        mv.visitMaxsAndEnd()
    }

    private fun addMethodResizeHashTable(cv: ClassVisitor) {
        val mv = cv.visitMethod(ACC_PROTECTED, "resizeHashTable",
            "(I${Consumer::class.descriptor})V",
            "(IL${Consumer::class.internalName}<${typeInfo.wrapperType.descriptor}>;)V", null)
        mv.visitCode()
        // get array
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(
            GETFIELD,
            internalClassName,
            HASH_TABLE_FIELD,
            typeInfo.primitiveArrayType.descriptor
        )
        // save in local variable
        mv.visitVarInsn(ASTORE, 3)

        // array = new int[newSize] - example for T=int
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ILOAD, 1)
        mv.visitIntInsn(NEWARRAY, typeInfo.newarrayType)
        mv.visitFieldInsn(
            PUTFIELD,
            internalClassName,
            HASH_TABLE_FIELD,
            typeInfo.primitiveArrayType.descriptor
        )

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
        mv.visitInsn(typeInfo.aloadInsn)
        mv.visitInsn(typeInfo.dupInsn)
        mv.visitVarInsn(typeInfo.storeInsn, 7)

        // if oldArray[i] == 0
        val gotoNextElement = Label()
        addIfInsnWithTypecast(mv, IFEQ, gotoNextElement)

        // transferOldElement.accept(Integer.valueOf(oldArray[i])) - example for T=int
        mv.visitVarInsn(ALOAD, 2) // transferOldElement
        mv.visitVarInsn(typeInfo.loadInsn, 7)
        mv.visitMethodInsn(INVOKESTATIC, typeInfo.wrapperType.internalName, "valueOf",
            "(${typeInfo.primitiveType.descriptor})${typeInfo.wrapperType.descriptor}", false)
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
        mv.visitFieldInsn(
            GETFIELD,
            internalClassName,
            HASH_TABLE_FIELD,
            typeInfo.primitiveArrayType.descriptor
        )
        mv.visitVarInsn(ILOAD, 1)
        mv.visitInsn(typeInfo.aloadInsn)

        // if array[index] != 0
        val returnFalse = Label()
        addIfInsnWithTypecast(mv, IFNE, returnFalse)
        // return true
        mv.visitInsn(ICONST_1)
        mv.visitInsn(IRETURN)
        mv.visitLabel(returnFalse)
        mv.visitInsn(ICONST_0)
        mv.visitInsn(IRETURN)
        mv.visitMaxsAndEnd()
    }

    private fun addIfInsnWithTypecast(mv: MethodVisitor, insn: Int, label: Label) {
        // For long/double we don't have instruction like IFNE, we need first to push 0 constant to stack
        // and use separate instruction for comparing which returns int.
        typeInfo.typecastForIfInfo?.let {
            mv.visitInsn(it.const0Insn)
            mv.visitInsn(it.cmpInsn)
        }
        mv.visitJumpInsn(insn, label)
    }

    private fun addMethodContains(cv: ClassVisitor) {
        val descriptor = "(${Object::class.descriptor})Z"
        val mv = cv.visitMethod(ACC_PUBLIC, "contains", descriptor, null, null)
        mv.visitCode()

        // if (value == null) call super
        mv.visitVarInsn(ALOAD, 1)
        val callSuper = Label()
        mv.visitJumpInsn(IFNULL, callSuper)

        // else if ((Integer) value).intValue() == 0 - example for T=int
        mv.visitVarInsn(ALOAD, 1)
        mv.visitTypeInsn(CHECKCAST, typeInfo.wrapperType.internalName)
        mv.visitMethodInsn(INVOKEVIRTUAL, typeInfo.wrapperType.internalName,
            typeInfo.unboxingMethodName, "()${typeInfo.primitiveType.descriptor}", false)
        addIfInsnWithTypecast(mv, IFNE, callSuper)

        // return specialEmptyValAdded
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, internalClassName, SPECIAL_EMPTY_VAL_ADDED_FIELD, "Z")
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

        // else if ((Integer) value).intValue() == 0 - example for T=int
        mv.visitVarInsn(ALOAD, 1)
        mv.visitTypeInsn(CHECKCAST, typeInfo.wrapperType.internalName)
        mv.visitMethodInsn(INVOKEVIRTUAL, typeInfo.wrapperType.internalName,
            typeInfo.unboxingMethodName, "()${typeInfo.primitiveType.descriptor}", false)
        addIfInsnWithTypecast(mv, IFNE, callSuper)

        // if (specialEmptyValAdded)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, internalClassName, SPECIAL_EMPTY_VAL_ADDED_FIELD, "Z")
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
        mv.visitFieldInsn(PUTFIELD, internalClassName, SPECIAL_EMPTY_VAL_ADDED_FIELD, "Z")

        // setSize(getSize() + 1)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKEVIRTUAL, internalClassName, "getSize", "()I", false)
        mv.visitInsn(ICONST_1)
        mv.visitInsn(IADD)
        mv.visitMethodInsn(INVOKEVIRTUAL, internalClassName, "setSize", "(I)V", false)

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

    private fun MethodVisitor.visitMaxsAndEnd() {
        visitMaxs(0, 0) // computed automatically
        visitEnd()
    }

    private companion object {
        private const val HASH_TABLE_FIELD = "array"
        private const val SPECIAL_EMPTY_VAL_ADDED_FIELD = "specialEmptyValAdded"
    }
}
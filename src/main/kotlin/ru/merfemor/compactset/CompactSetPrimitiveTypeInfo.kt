package ru.merfemor.compactset

import org.objectweb.asm.Opcodes.*
import kotlin.reflect.KClass

/**
 * Info about primitive type parameters of [CompactSet] needed during implementation bytecode generation.
 */
internal enum class CompactSetPrimitiveTypeInfo(
    /**
     * Class representing primitive type.
     */
    val primitiveType: KClass<*>,
    /**
     * Value for the type operand of the NEWARRAY instruction.
     */
    val newarrayType: Int,
    /**
     * Class representing primitive type array.
     */
    val primitiveArrayType: KClass<*>,
    /**
     * Method of wrapper class to unbox it to primitive type.
     */
    val unboxingMethodName: String,
    /**
     * Instruction for loading elements of this type from array.
     */
    val aloadInsn: Int,
    /**
     * Instruction for storing elements of this type into array.
     */
    val astoreInsn: Int,
    /**
     * Instruction for loading elements of this type from local variables.
     */
    val loadInsn: Int,
    /**
     * Instruction for storing elements of this type into local variables.
     */
    val storeInsn: Int,
    /**
     * DUP instruction for elements of this type.
     */
    val dupInsn: Int,
    /**
     * Info about typecasting before IF instructions. `null` if typecasting is not needed.
     */
    val typecastForIfInfo: TypecastForIfInfo?,
) {

    INT(
        primitiveType = Int::class,
        newarrayType = T_INT,
        primitiveArrayType = IntArray::class,
        unboxingMethodName = "intValue",
        aloadInsn = IALOAD,
        astoreInsn = IASTORE,
        loadInsn = ILOAD,
        storeInsn = ISTORE,
        dupInsn = DUP,
        typecastForIfInfo = null,
    ),
    DOUBLE(
        primitiveType = Double::class,
        newarrayType = T_DOUBLE,
        primitiveArrayType = DoubleArray::class,
        unboxingMethodName = "doubleValue",
        aloadInsn = DALOAD,
        astoreInsn = DASTORE,
        loadInsn = DLOAD,
        storeInsn = DSTORE,
        dupInsn = DUP2,
        typecastForIfInfo = TypecastForIfInfo(
            const0Insn = DCONST_0,
            cmpInsn = DCMPL,
        ),
    ),
    LONG(
        primitiveType = Long::class,
        newarrayType = T_LONG,
        primitiveArrayType = LongArray::class,
        unboxingMethodName = "longValue",
        aloadInsn = LALOAD,
        astoreInsn = LASTORE,
        loadInsn = LLOAD,
        storeInsn = LSTORE,
        dupInsn = DUP2,
        typecastForIfInfo = TypecastForIfInfo(
            const0Insn = LCONST_0,
            cmpInsn = LCMP,
        ),
    );

    val wrapperType: Class<*>
        get() = primitiveType.javaObjectType

}

/**
 * Info needed to typecast element to int before IF instructions.
 */
internal data class TypecastForIfInfo(
    /**
     * Instruction for pushing `0` constant of this type into stack.
     */
    val const0Insn: Int,
    /**
     * Instruction for comparing two values of this type on stack.
     */
    val cmpInsn: Int
)

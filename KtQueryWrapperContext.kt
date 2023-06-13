package com.junest.utils

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

inline fun <reified T> KtQueryWrapper() = KtQueryWrapper(T::class.java)

inline fun <reified T : Any> KtQueryWrapperContext(noinline context: KtQueryWrapperContext<T>.() -> Unit) =
    KtQueryWrapperContext(T::class, context)

class KtQueryWrapperContext<T : Any>(
    entityClass: KClass<T>,
    context: KtQueryWrapperContext<T>.() -> Unit
) : KtQueryWrapper<T>(entityClass.java) {

    init {
        this.context()
    }

    private var columnAndData: ColumnAndData? = null

    operator fun Any?.unaryPlus() = ColumnAndData(null, this).also { columnAndData = it }

    operator fun KProperty<*>.unaryPlus() = ColumnAndData(this, null).also { columnAndData = it }

    infix fun ColumnAndData.value(data: Any?): ColumnAndData {
        this.data = data
        return this
    }


    infix fun ColumnAndData.property(column: KProperty<*>?): ColumnAndData {
        this.column = column
        return this
    }

    infix fun ColumnAndData.run(function: (KProperty<*>, Any?) -> KtQueryWrapper<T>) {
        column?.let {
            notEmpty(data) { function(it, data) }
        } ?: throw IllegalArgumentException("缺少 KProperty")
        columnAndData = null
    }
}

fun <T : Any> KtQueryWrapper<T>.notEmpty(target: Any?, action: KtQueryWrapper<T>.() -> KtQueryWrapper<T>): KtQueryWrapper<T> {
    // Quick return
    if(target === null)
        return this
    val result = when (target) {
        is String -> target.isNotEmpty()
        is Collection<*> -> !target.isEmpty()
        else -> true
    }
    return if(result) action() else this
}

data class ColumnAndData(
    var column: KProperty<*>? = null,
    var data: Any? = null
)

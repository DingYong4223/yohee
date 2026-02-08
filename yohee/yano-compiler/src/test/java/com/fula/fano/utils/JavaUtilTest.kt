package com.fula.fano.utils

import org.junit.Test

class JavaUnitTest {
    @Test
    internal fun `test fot list`() {
        //声明一个数组
        val array1 = arrayListOf("str1", "str2", "str3")

        //将数组转化为集合
        val list1 = array1.toList()

        //将集合转化为数组
        val array2 = list1.toTypedArray()

        printItem(*array2)
    }

    private fun printItem(vararg str: String) {
        str.forEach {
            println(it)
        }
    }

}
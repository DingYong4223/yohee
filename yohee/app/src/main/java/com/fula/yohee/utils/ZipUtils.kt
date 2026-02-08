package com.fula.yohee.utils

import com.fula.CLog
import com.fula.yohee.extensions.tryUse
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipUtils {
    companion object {

        /**
         * 解压zip到指定的路径
         * @param zipFile ZIP的名称
         * @param outPath 要解压缩路径
         * @throws Exception
         */
        @Throws(Exception::class)
        @JvmStatic
        fun unZipFolder(zipFile: String, outPath: String) {
            val inZip = ZipInputStream(FileInputStream(zipFile))
            var zipEntry: ZipEntry?
            while (inZip.nextEntry.apply { zipEntry = this } != null) {
                var szName = zipEntry!!.name
                if (zipEntry!!.isDirectory) { //获取部件的文件夹名
                    szName = szName.substring(0, szName.length - 1)
                    val folder = File(outPath + File.separator + szName)
                    folder.mkdirs()
                } else {
                    CLog.i(outPath + File.separator + szName)
                    val file = File(outPath + File.separator + szName)
                    if (!file.exists()) {
                        CLog.i("Create the file:" + outPath + File.separator + szName)
                        file.parentFile.mkdirs()
                        file.createNewFile()
                    }
                    // 获取文件的输出流
                    val out = FileOutputStream(file)
                    var len: Int
                    val buffer = ByteArray(1024)
                    // 读取（字节）字节到缓冲区
                    while (inZip.read(buffer).apply { len = this } != -1) {
                        // 从缓冲区（0）位置写入（字节）字节
                        out.write(buffer, 0, len)
                        out.flush()
                    }
                    out.close()
                }
            }
            inZip.close()
        }

//        @Throws(Exception::class)
//        @JvmStatic
//        fun unZipFolder(zipFile: String, outPath: String) {
//            ZipInputStream(FileInputStream(zipFile)).tryUse { inZip ->
//                var zipEntry: ZipEntry?
//                var szName = ""
//                while (inZip.nextEntry.apply { zipEntry = this } != null) {
//                    szName = szName.substring(0, szName.length - 1)
//                    if (zipEntry!!.isDirectory) { //获取部件的文件夹名
//                        szName = szName.substring(0, szName.length - 1)
//                        val folder = File(outPath + File.separator + szName)
//                        folder.mkdirs()
//                    } else {
//                        CLog.i(outPath + File.separator + szName)
//                        val file = File(outPath + File.separator + szName)
//                        if (!file.exists()) {
//                            CLog.i("Create the file:" + outPath + File.separator + szName)
//                            file.parentFile.mkdirs()
//                            file.createNewFile()
//                        }
//                        FileOutputStream(file).tryUse {
//                            var len: Int
//                            val buffer = ByteArray(1024) // 读取（字节）字节到缓冲区
//                            while (inZip.read(buffer).apply { len = this } != -1) { // 从缓冲区（0）位置写入（字节）字节
//                                it.write(buffer, 0, len)
//                                it.flush()
//                            }
//                        }
//                    }
//                }
//            }
//        }

        /**
         * 压缩文件和文件夹
         * @param src 要压缩的文件或文件夹
         * @param zip 解压完成的Zip路径
         * @throws Exception
         */
        @Throws(Exception::class)
        @JvmStatic
        fun zipFolder(src: String, zip: String): Boolean {
            try {
                ZipOutputStream(FileOutputStream(zip))
                        .use {
                            val file = File(src)
                            zipFiles(file.parent + File.separator, file.name, it)
                            it.finish()
                        }
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        /**
         * 压缩文件
         * @param folderString
         * @param fileString
         * @param zos
         * @throws Exception
         */
        @Throws(Exception::class)
        @JvmStatic
        private fun zipFiles(folderString: String, fileString: String, zos: ZipOutputStream?) {
            if (zos == null) return
            val file = File(folderString + fileString)
            if (file.isFile) {
                val zipEntry = ZipEntry(fileString)
                FileInputStream(file).tryUse {
                    zos.putNextEntry(zipEntry)
                    var len: Int
                    val buffer = ByteArray(4096)
                    while (it.read(buffer).apply { len = this } != -1) {
                        zos.write(buffer, 0, len)
                    }
                    zos.closeEntry()
                }
            } else {
                //文件夹
                val fileList = file.list()
                //没有子文件和压缩
                if (fileList.isEmpty()) {
                    val zipEntry = ZipEntry(fileString + File.separator)
                    zos.putNextEntry(zipEntry)
                    zos.closeEntry()
                }
                //子文件和递归
                for (i in fileList.indices) {
                    zipFiles(folderString, fileString + File.separator + fileList[i], zos)
                }
            }
        }

        /**
         * 返回zip的文件输入流
         *
         * @param zipFileString zip的名称
         * @param fileString    ZIP的文件名
         * @return InputStream
         * @throws Exception
         */
        @Throws(Exception::class)
        @JvmStatic
        fun upZip(zipFileString: String, fileString: String): InputStream {
            val zipFile = ZipFile(zipFileString)
            val zipEntry = zipFile.getEntry(fileString)
            return zipFile.getInputStream(zipEntry)
        }

        /**
         * 返回ZIP中的文件列表（文件和文件夹）
         *
         * @param zipFileString  ZIP的名称
         * @param bContainFolder 是否包含文件夹
         * @param bContainFile   是否包含文件
         * @return
         * @throws Exception
         */
        @Throws(Exception::class)
        @JvmStatic
        fun getFileList(zipFileString: String, bContainFolder: Boolean, bContainFile: Boolean): List<File> {
            val fileList = ArrayList<File>()
            val inZip = ZipInputStream(FileInputStream(zipFileString))
            var zipEntry: ZipEntry
            while (inZip.nextEntry.apply { zipEntry = this } != null) {
                var szName = zipEntry.name
                if (zipEntry.isDirectory) {
                    // 获取部件的文件夹名
                    szName = szName.substring(0, szName.length - 1)
                    val folder = File(szName)
                    if (bContainFolder) {
                        fileList.add(folder)
                    }
                } else {
                    val file = File(szName)
                    if (bContainFile) {
                        fileList.add(file)
                    }
                }
            }
            inZip.close()
            return fileList
        }
    }
}
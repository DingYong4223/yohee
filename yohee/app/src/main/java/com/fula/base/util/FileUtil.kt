package com.fula.base.util


import java.io.*
import java.net.URL
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.util.regex.Pattern

/**
 * @author fisher
 * @description 文件工具类
 */

object FileUtil {

    /**
     * default buffer size
     */
    private const val BLKSIZ = 8192
    //private static final Log Debug = LogFactory.getLog(FileUtil.class);

    // 获取从classpath根目录开始读取文件注意转化成中文
    fun getCPFile(path: String): String? {
        val url = FileUtil::class.java.classLoader!!.getResource(path)
        val filepath = url.file
        val file = File(filepath)
        val retBuffer = ByteArray(file.length().toInt())
        val fis = FileInputStream(filepath)
        fis.use {
            fis.read(retBuffer)
        }
        return String(retBuffer, Charset.forName("GBK"))
    }


    /**
     * 利用java本地拷贝文件及文件夹,如何实现文件夹对文件夹的拷贝呢?如果文件夹里还有文件夹怎么办呢?
     *
     * @param objDir
     * 目标文件夹
     * @param srcDir
     * 源的文件夹
     * @throws IOException
     */
    @Throws(IOException::class)
    fun copyDirectiory(objDir: String, srcDir: String) {
        File(objDir).mkdirs()
        val file = File(srcDir).listFiles()
        for (i in file.indices) {
            if (file[i].isFile) {
                val input = FileInputStream(file[i])
                val output = FileOutputStream(objDir + "/"
                        + file[i].name)
                val b = ByteArray(1024 * 5)
                var len: Int
                while (input.read(b).apply { len = this } != -1) {
                    output.write(b, 0, len)
                }
                output.flush()
                output.close()
                input.close()
            }
            if (file[i].isDirectory) {
                copyDirectiory(objDir + "/" + file[i].name, srcDir + "/"
                        + file[i].name)
            }
        }
    }

    /**
     * 将一个文件inName拷贝到另外一个文件outName中
     *
     * @param inName
     * 源文件路径
     * @param outName
     * 目标文件路径
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Throws(FileNotFoundException::class, IOException::class)
    fun copyFile(inName: String, outName: String) {
        val `is` = BufferedInputStream(FileInputStream(
                inName))
        val os = BufferedOutputStream(
                FileOutputStream(outName))
        copyFile(`is`, os, true)
    }

    /**
     * Copy a file from an opened InputStream to opened OutputStream
     *
     * @param is
     * source InputStream
     * @param os
     * target OutputStream
     * @param close
     * 写入之后是否需要关闭OutputStream
     * @throws IOException
     */
    @Throws(IOException::class)
    fun copyFile(`is`: InputStream, os: OutputStream, close: Boolean) {
        var b: Int
        while (`is`.read().apply { b = this } != -1) {
            os.write(b)
        }
        `is`.close()
        if (close)
            os.close()
    }

    @Throws(IOException::class)
    fun copyFile(`is`: Reader, os: Writer, close: Boolean) {
        var b: Int
        while (`is`.read().apply { b = this } != -1) {
            os.write(b)
        }
        `is`.close()
        if (close)
            os.close()
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun copyFile(inName: String, pw: PrintWriter, close: Boolean) {
        val `is` = BufferedReader(FileReader(inName))
        copyFile(`is`, pw, close)
    }

    /**
     * 从文件inName中读取第一行的内容
     *
     * @param inName
     * 源文件路径
     * @return 第一行的内容
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Throws(FileNotFoundException::class, IOException::class)
    fun readLine(inName: String): String? {
        val `is` = BufferedReader(FileReader(inName))
        var line: String? = null
        line = `is`.readLine()
        `is`.close()
        return line
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun copyFileBuffered(inName: String, outName: String) {
        val `is` = FileInputStream(inName)
        val os = FileOutputStream(outName)
        var count = 0
        val b = ByteArray(BLKSIZ)
        while (`is`.read(b).apply { count = this } != -1) {
            os.write(b, 0, count)
        }
        `is`.close()
        os.close()
    }

    /**
     * 将String变成文本文件
     *
     * @param text
     * 源String
     * @param fileName
     * 目标文件路径
     * @throws IOException
     */
    @Throws(IOException::class)
    fun stringToFile(text: String, fileName: String) {
        val os = BufferedWriter(FileWriter(fileName))
        os.write(text)
        os.flush()
        os.close()
    }

    /**
     * 打开文件获得BufferedReader
     *
     * @param fileName
     * 目标文件路径
     * @return BufferedReader
     * @throws IOException
     */
    @Throws(IOException::class)
    fun openFile(fileName: String): BufferedReader {
        return BufferedReader(FileReader(fileName))
    }

    /**
     * 获取文件filePath的字节编码byte[]
     *
     * @param filePath
     * 文件全路径
     * @return 文件内容的字节编码
     * @roseuid 3FBE26DE027D
     */
    fun fileToBytes(filePath: String?): ByteArray? {
        if (filePath == null) {
            //Debug.info("路径为空：");
            return null
        }
        val tmpFile = File(filePath)
        val retBuffer = ByteArray(tmpFile.length().toInt())
        var fis: FileInputStream? = null
        fis = FileInputStream(filePath)
        return fis.use {
            fis.read(retBuffer)
            retBuffer
        }

    }

    /**
     * 将byte[]转化成文件fullFilePath
     *
     * @param fullFilePath
     * 文件全路径
     * @param content
     * 源byte[]
     */
    fun bytesToFile(fullFilePath: String?, content: ByteArray?) {
        if (fullFilePath == null || content == null) {
            return
        }

        // 创建相应的目录
        val f = File(getDir(fullFilePath))
        if (f == null || !f.exists()) {
            f.mkdirs()
        }

        try {
            val fos = FileOutputStream(fullFilePath)
            fos.write(content)
            fos.close()
        } catch (e: Exception) {
            //Debug.error("写入文件异常:" + e.toString());
        }

    }

    /**
     * 根据传入的文件全路径，返回文件所在路径
     *
     * @param fullPath
     * 文件全路径
     * @return 文件所在路径
     */
    fun getDir(fullPath: String): String {
        var iPos1 = fullPath.lastIndexOf("/")
        val iPos2 = fullPath.lastIndexOf("\\")
        iPos1 = if (iPos1 > iPos2) iPos1 else iPos2
        return fullPath.substring(0, iPos1 + 1)
    }

    /**
     * 根据传入的文件全路径，返回文件全名（包括后缀名）
     *
     * @param fullPath
     * 文件全路径
     * @return 文件全名（包括后缀名）
     */
    fun getFileName(fullPath: String): String {
        var iPos1 = fullPath.lastIndexOf("/")
        val iPos2 = fullPath.lastIndexOf("\\")
        iPos1 = if (iPos1 > iPos2) iPos1 else iPos2
        return fullPath.substring(iPos1 + 1)
    }

    /**
     * 获得文件名fileName中的后缀名
     *
     * @param fileName
     * 源文件名
     * @return String 后缀名
     */
    fun getFileSuffix(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf(".") + 1,
                fileName.length)
    }

    /**
     * 根据传入的文件全名（包括后缀名）或者文件全路径返回文件名（没有后缀名）
     *
     * @param fullPath
     * 文件全名（包括后缀名）或者文件全路径
     * @return 文件名（没有后缀名）
     */
    fun getPureFileName(fullPath: String): String {
        val fileFullName = getFileName(fullPath)
        return fileFullName.substring(0, fileFullName.lastIndexOf("."))
    }

    /**
     * 转换文件路径中的\\为/
     *
     * @param filePath
     * 要转换的文件路径
     * @return String
     */
    fun wrapFilePath(filePath: String): String {
        var filePath = filePath
        filePath.replace('\\', '/')
        if (filePath[filePath.length - 1] != '/') {
            filePath += "/"
        }
        return filePath
    }

    /**
     * 删除整个目录path,包括该目录下所有的子目录和文件
     *
     * @param path
     */
    fun deleteDirs(path: String) {
        val rootFile = File(path)
        val files = rootFile.listFiles() ?: return
        for (i in files.indices) {
            val file = files[i]
            if (file.isDirectory) {
                deleteDirs(file.path)
            } else {
                file.delete()
            }
        }
        rootFile.delete()
    }

    /**  */
    /**文件重命名
     * @param path 文件目录
     * @param oldname  原来的文件名
     * @param newname 新文件名
     */
    fun renameFile(path: String, oldname: String, newname: String) {
        if (oldname != newname) {//新的文件名和以前文件名不同时,才有必要进行重命名
            val oldfile = File(path + File.separator + oldname)
            val newfile = File(path + File.separator + newname)
            if (!oldfile.exists()) {
                return //重命名文件不存在
            }
            if (newfile.exists())
            //若在该目录下已经有一个文件和新文件名相同，则不允许重命名
                println(newname + "已经存在！")
            else {
                oldfile.renameTo(newfile)
            }
        } else {
            println("新文件名和旧文件名相同...")
        }
    }


    /**  */
    /**文件夹重命名
     */
    fun renameDir(oldDirPath: String, newDirPath: String): Boolean {
        val oleFile = File(oldDirPath) //要重命名的文件或文件夹
        val newFile = File(newDirPath)  //重命名为zhidian1
        return oleFile.renameTo(newFile)  //执行重命名
    }

    fun fileToString(filePath: String): String? {
        var fileString: StringBuilder? = null
        try {
            val os = BufferedReader(FileReader(filePath))
            fileString = StringBuilder()
            var valueString: String
            while (os.readLine().apply { valueString = this } != null) {
                fileString.append(valueString)
            }
            os.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return fileString?.toString()
    }

    fun getFormatedFileSize(size: Long): String {
        val result: String
        if (size == 0L) {
            return "0B"
        }
        val df = DecimalFormat("#.00")
        if (size < 1024) {
            result = df.format(size.toDouble()) + "B"
        } else if (size < 1048576) {
            result = df.format(size.toDouble() / 1024) + "KB"
        } else if (size < 1073741824) {
            result = df.format(size.toDouble() / 1048576) + "MB"
        } else {
            result = df.format(size.toDouble() / 1073741824) + "GB"
        }
        return result
    }

    fun getExtension(url: String): String {
        val fileName = URL(url).path
        return if (fileName.lastIndexOf(".") < 1) {
            ""
        } else fileName.substring(fileName.lastIndexOf(".") + 1)
    }


    fun getName(url: String): String {
        val fileName = URL(url).path
        return if (fileName.lastIndexOf(".") < 1) {
            fileName
        } else fileName.substring(0, fileName.lastIndexOf("."))
    }

    fun getFolderSize(file: File): Long {
        var size: Long = 0
        val fileList = file.listFiles()
        for (aFileList in fileList) {
            if (aFileList.isDirectory) {
                size = size + getFolderSize(aFileList)
            } else {
                size = size + aFileList.length()
            }
        }
        return size
    }

    fun fileNameFilter(fileName: String?): String? {
        val filePattern = Pattern.compile("[\\\\/:*?\"<>|.]")
        return if (fileName == null) null else filePattern.matcher(fileName).replaceAll("")
    }
}

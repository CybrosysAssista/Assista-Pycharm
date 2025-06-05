package utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.IOException

object FileUtils {

    fun createDirectory(parentDir: VirtualFile, dirName: String): VirtualFile? {
        var result: VirtualFile? = null

        ApplicationManager.getApplication().runWriteAction {
            try {
                result = parentDir.createChildDirectory(null, dirName)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return result
    }

    fun createFile(directory: VirtualFile, fileName: String, content: String): VirtualFile? {
        var result: VirtualFile? = null

        ApplicationManager.getApplication().runWriteAction {
            try {
                result = directory.createChildData(null, fileName)
                VfsUtil.saveText(result!!, content)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return result
    }

    fun replaceTemplatePlaceholders(content: String, moduleName: String): String {
        return content
            .replace("**modulename**", moduleName)
            .replace("{{modulename}}", moduleName)
            .replace("MODULE_NAME", moduleName)
    }

    fun getResourcePath(path: String): String {
        val resource = FileUtils::class.java.classLoader.getResource(path)
        return resource?.path ?: ""
    }

    fun readResourceFile(path: String): String {
        val resource = FileUtils::class.java.classLoader.getResourceAsStream(path)
        return resource?.bufferedReader()?.readText() ?: ""
    }
}
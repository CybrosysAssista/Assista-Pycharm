package standards

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import java.io.IOException

fun triggerChanges(old: String, new: String?, file: PsiFile, project: Project){
    val module: VirtualFile? = getModule(file)
    var parent =  ""
    if (file.containingDirectory != module){
        parent = "-${file.containingDirectory.name}"
    }
    val history = "odoo_standard_history-${module?.name}$parent-${file.name}.xml"
    val cache = CacheFileCreator(project)
    val content = """
        <fixed old="$old" new="$new"/>
    """.trimIndent()
    cache.createCacheFile(history, content)
}

class CacheFileCreator(private val project: Project) {
    fun createCacheFile(fileName: String, content: String) {
        try {
            val cacheDir = getOdooStandersDirectory()
            val cacheFile = cacheDir.findChild(fileName) ?: cacheDir.createChildData(this, fileName)
            cacheFile.setBinaryContent(content.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getCacheDirectory(): VirtualFile {
        val baseDir = project.baseDir
        return baseDir.findChild(".cache") ?: baseDir.createChildDirectory(this, ".cache")
    }

    private fun getOdooStandersDirectory(): VirtualFile {
        val baseDir = getCacheDirectory()
        return baseDir.findChild("odoo_standards") ?: baseDir.createChildDirectory(this, "odoo_standards")
    }
}

fun getModule(file: PsiFile): VirtualFile?{
    return getModule(file.virtualFile)
}
fun getModule(file: VirtualFile?): VirtualFile? {
    var nFile = file
    while (nFile != null) {
        if (nFile.isDirectory && nFile.findChild("__manifest__.py") != null) {
            return nFile
        }
        nFile = nFile.parent
    }
    return null
}
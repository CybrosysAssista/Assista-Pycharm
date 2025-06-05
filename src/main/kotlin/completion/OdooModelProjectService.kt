package completion

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyTargetExpression

@Service(Service.Level.PROJECT)
class OdooModelProjectService(private val project: Project) {

    private var cache: Map<String, List<String>> = emptyMap()

    fun getFieldsForModel(model: String): List<String> {
        if (cache.isEmpty()) buildCache()
        return cache[model] ?: emptyList()
    }

    private fun buildCache() {
        val result = mutableMapOf<String, MutableList<String>>()
        val files = FileTypeIndex.getFiles(PythonFileType.INSTANCE, GlobalSearchScope.projectScope(project))

        for (file in files) {
            val psiFile = PsiManager.getInstance(project).findFile(file) ?: continue
            val pyClasses = PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java)

            for (pyClass in pyClasses) {
                val modelAttr = pyClass.statementList.statements
                    .filterIsInstance<com.jetbrains.python.psi.PyAssignmentStatement>()
                    .find { it.targets.firstOrNull()?.name == "_name" }
                    ?.assignedValue
                    ?.let { it as? com.jetbrains.python.psi.PyStringLiteralExpression }
                    ?.stringValue
                    ?: continue

                val fields = pyClass.statementList.statements
                    .filterIsInstance<PyTargetExpression>()
                    .mapNotNull { it.name }

                result.getOrPut(modelAttr) { mutableListOf() }.addAll(fields)
            }
        }

        cache = result
    }

    fun invalidateCache() {
        cache = emptyMap()
    }
}
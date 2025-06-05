package indexing

import com.intellij.util.indexing.*
import com.intellij.util.io.*
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.psi.*
import com.intellij.util.indexing.FileContent

class OdooModelIndex : FileBasedIndexExtension<String, Void>() {

    companion object {
        val NAME = ID.create<String, Void>("OdooModelIndex")
    }

    override fun getName(): ID<String, Void> = NAME

    override fun getIndexer(): DataIndexer<String, Void, FileContent> {
        return DataIndexer { inputData ->
            val result = mutableMapOf<String, Void?>()
            val psiFile = inputData.psiFile

            if (psiFile is PyFile) {
                for (cls in psiFile.topLevelClasses) {
                    val isModel = cls.superClassExpressions.any {
                        it.text.contains("Model")
                    }

                    if (isModel) {
                        for (stmt in cls.statementList.statements) {
                            if (stmt is PyAssignmentStatement &&
                                stmt.leftHandSideExpression?.text == "_name") {

                                val assignedValue = stmt.assignedValue
                                if (assignedValue is PyStringLiteralExpression) {
                                    result[assignedValue.stringValue] = null
                                }
                            }
                        }
                    }
                }
            }
            result
        }
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return DefaultFileTypeSpecificInputFilter(PythonFileType.INSTANCE)
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getValueExternalizer(): DataExternalizer<Void> = VoidDataExternalizer.INSTANCE

    override fun dependsOnFileContent(): Boolean = true

    override fun getVersion(): Int = 1
}

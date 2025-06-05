package completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiManager
import com.intellij.util.ProcessingContext
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.icons.AllIcons
import com.intellij.util.indexing.FileBasedIndex
import indexing.OdooModelIndex
import com.intellij.util.Processor

class OdooModelCompletionProvider : CompletionProvider<CompletionParameters>() {

    private val logger = Logger.getInstance(OdooModelCompletionProvider::class.java)

    private var cachedModelNames: Set<String>? = null
    private var lastProjectChecked: Project? = null

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        logger.info("OdooModelCompletionProvider triggered")

        val project = parameters.position.project
        val psiElement = parameters.position
        val prefix = resultSet.prefixMatcher.prefix

        logger.info("Prefix: $prefix")

        val modelNames = getModelNames(project)

        for (model in modelNames) {
            if (resultSet.prefixMatcher.prefixMatches(model)) {
                logger.info("Adding model: $model")

                val element = LookupElementBuilder.create(model)
                    .withIcon(AllIcons.Nodes.Class)
                    .withTypeText("Odoo Model")
                    .withInsertHandler { context, item ->
                        val editor = context.editor
                        val document = editor.document
                        val caretOffset = editor.caretModel.offset

                        val modelName = item.lookupString

                        val currentElement = context.file.findElementAt(context.startOffset)
                        val stringLiteral = PsiTreeUtil.getParentOfType(
                            currentElement,
                            PyStringLiteralExpression::class.java
                        )

                        if (stringLiteral != null) {
                            val startOffset = stringLiteral.textRange.startOffset + 1
                            val endOffset = stringLiteral.textRange.endOffset - 1

                            val currentContent = stringLiteral.stringValue
                            if (currentContent != modelName) {
                                context.laterRunnable = Runnable {
                                    document.replaceString(startOffset, endOffset, modelName)
                                }
                            }
                        }

                        if (caretOffset >= document.textLength ||
                            document.text[caretOffset] != '"' ||
                            caretOffset + 1 >= document.textLength ||
                            document.text[caretOffset + 1] != ']') {
                            EditorModificationUtil.insertStringAtCaret(editor, "")
                        }

                        context.laterRunnable = Runnable {
                            editor.caretModel.moveToOffset(caretOffset + 2) // Move after "]
                        }
                    }
                    .withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE)

                resultSet.addElement(element)
            }
        }
    }

    private fun getModelNames(project: Project): Set<String> {
        if (project === lastProjectChecked && cachedModelNames != null) {
            return cachedModelNames!!
        }

        val modelNames = mutableSetOf<String>()
        val index = FileBasedIndex.getInstance()
        val scope = GlobalSearchScope.projectScope(project)

        index.processAllKeys(OdooModelIndex.NAME, Processor { key ->
            val values = index.getValues(OdooModelIndex.NAME, key, scope)
            if (values.isNotEmpty()) {
                modelNames.add(key)
            }
            true // Continue processing
        }, project)

        cachedModelNames = modelNames
        lastProjectChecked = project

        return modelNames
    }
}

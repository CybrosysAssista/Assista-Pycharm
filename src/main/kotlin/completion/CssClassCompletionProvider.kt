package completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.openapi.project.Project
import indexing.CssClassIndex
import indexing.OdooModelFieldIndex

class CssClassCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        val position = parameters.position
        val project: Project = position.project

        // Get all CSS class names from your index
        val classNames = FileBasedIndex.getInstance().getAllKeys(CssClassIndex.NAME, project)

        for (className in classNames) {
            resultSet.addElement(
                LookupElementBuilder.create(className)
                    .withTypeText("CSS Class", true)
                    .withInsertHandler { ctx, _ ->
                        val tailOffset = ctx.tailOffset
                        ctx.document.insertString(tailOffset, " ")
                        ctx.editor.caretModel.moveToOffset(tailOffset + 1)
                    }
            )
        }
    }
}

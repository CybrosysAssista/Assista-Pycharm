package completion

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.jetbrains.python.PyTokenTypes
import indexing.OdooModelFieldIndex
import com.intellij.codeInsight.lookup.LookupElementBuilder

class OdooInverseNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val stringLiteral = position.parent as? PyStringLiteralExpression ?: return

        val argumentList = PsiTreeUtil.getParentOfType(stringLiteral, PyArgumentList::class.java) ?: return
        val callExpression = argumentList.parent as? PyCallExpression ?: return
        val calleeName = callExpression.callee?.text ?: return

        // Only target fields.One2many
        if (calleeName != "fields.One2many") return

        val args = argumentList.arguments
        if (args.size < 2) return

        // Trigger only if cursor is on second argument
        if (stringLiteral != args[1]) return

        val relatedModelExpr = args[0] as? PyStringLiteralExpression ?: return
        val relatedModelName = relatedModelExpr.stringValue
        if (relatedModelName.isNullOrBlank()) return


        val project = parameters.position.project
        val fieldNames = FileBasedIndex.getInstance()
            .getValues(OdooModelFieldIndex.NAME, relatedModelName, GlobalSearchScope.allScope(project))
            .flatten()

        val relationalFields = fieldNames.filter { it.endsWith("_id") }
        println("this: $relationalFields")

        for (field in relationalFields) {
            result.addElement(LookupElementBuilder.create(field))
        }
    }
}

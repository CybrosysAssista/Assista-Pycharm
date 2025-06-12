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
import utils.PluginUtils

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

        for (field in fieldNames) {
            val fieldAttributes = PluginUtils.parseFieldAttributes(field)
            println(fieldAttributes)
            val fieldName = fieldAttributes["field_name"] ?: continue
            if (fieldName.endsWith("_id")) {
                val fieldType = fieldAttributes["field_type"] ?: "Unknown"
                val comodel = fieldAttributes["comodel_name"]

                // Build the completion suggestion
                var builder = LookupElementBuilder.create(fieldName)
                    .withTypeText(fieldType, true)
                if (comodel != null) {
                    builder = builder.withTailText(" → $comodel", true)
                }

                result.addElement(builder)
            }
        }
    }
}

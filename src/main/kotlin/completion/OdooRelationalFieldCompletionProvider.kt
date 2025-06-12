package completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.openapi.project.Project
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.python.psi.*
import indexing.OdooModelFieldIndex
import indexing.OdooModelIndex
import utils.PluginUtils

class OdooRelationalFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val pyReferenceExpr = PsiTreeUtil.getParentOfType(position, PyReferenceExpression::class.java) ?: return

        // Get the left side of the expression: e.g., partner_id in partner_id.name
        val qualifier = pyReferenceExpr.qualifier as? PyReferenceExpression ?: return
        val fieldName = qualifier.name ?: return

        // Find the class where this is being accessed
        val pyClass = PsiTreeUtil.getParentOfType(position, PyClass::class.java) ?: return
        val modelName = extractModelName(pyClass) ?: return
        val project = position.project

        // Find what model 'fieldName' refers to
        val callExpr = findFieldAssignment(pyClass, fieldName) ?: return
        val calleeText = callExpr.callee?.text ?: return

        if (!calleeText.startsWith("fields.")) return
        if (!calleeText.contains("Many2one") && !calleeText.contains("One2many") && !calleeText.contains("Many2many")) return

        val firstArg = callExpr.arguments.firstOrNull() as? PyStringLiteralExpression ?: return
        val targetModel = firstArg.stringValue

        // Suggest fields from the target model
        val suggestions = FileBasedIndex.getInstance()
            .getValues(OdooModelFieldIndex.NAME, targetModel, GlobalSearchScope.allScope(project))
            .flatten()

        for (field in suggestions) {
            val fieldAttributes = PluginUtils.parseFieldAttributes(field)
            val fieldName = fieldAttributes["field_name"] ?: continue
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

    private fun extractModelName(pyClass: PyClass): String? {
        for (stmt in pyClass.statementList.statements) {
            if (stmt is PyAssignmentStatement) {
                val lhs = stmt.leftHandSideExpression?.text
                val rhs = stmt.assignedValue as? PyStringLiteralExpression ?: continue

                when (lhs) {
                    "_name" -> return rhs.stringValue
                    "_inherit" -> return rhs.stringValue  // Handle inherited models
                }
            }
        }
        return null
    }

    private fun findFieldAssignment(pyClass: PyClass, fieldName: String): PyCallExpression? {
        for (stmt in pyClass.statementList.statements) {
            if (stmt is PyAssignmentStatement &&
                stmt.leftHandSideExpression?.text == fieldName) {
                return stmt.assignedValue as? PyCallExpression
            }
        }
        return null
    }
}
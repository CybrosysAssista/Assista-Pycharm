package completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.psi.search.GlobalSearchScope

import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveContext

import indexing.OdooModelFieldIndex

class OdooDependsFieldCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            psiElement().withParent(PyStringLiteralExpression::class.java),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    resultSet: CompletionResultSet
                ) {
                    val element = parameters.position.parent as? PyStringLiteralExpression ?: return
                    val argumentList = element.parent as? PyArgumentList ?: return
                    val decorator = PsiTreeUtil.getParentOfType(argumentList, PyDecorator::class.java)
                    if (decorator?.name != "depends" && decorator?.name != "onchange" && decorator?.name != "constrains") return

                    // Get class definition
                    val cls = PsiTreeUtil.getParentOfType(element, PyClass::class.java) ?: return
                    val modelName = cls.findClassAttributeValue("_name") ?: cls.findClassAttributeValue("_inherit") ?: return

                    // Use the OdooModelFieldIndex to get field names
                    val project = parameters.position.project
                    val indexValues = FileBasedIndex.getInstance().getValues(
                        OdooModelFieldIndex.NAME,
                        modelName,
                        GlobalSearchScope.allScope(project)
                    )

                    val allFields = indexValues.flatten().distinct()
                    for (field in allFields) {
                        resultSet.addElement(LookupElementBuilder.create(field))
                    }
                }
            }
        )
    }

    private fun PyClass.findClassAttributeValue(attrName: String): String? {
        return this.statementList.statements
            .filterIsInstance<PyAssignmentStatement>()
            .firstOrNull { it.leftHandSideExpression?.text == attrName }
            ?.assignedValue
            ?.let { it as? PyStringLiteralExpression }
            ?.stringValue
    }
}

package completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PatternCondition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.*
import indexing.OdooModelFieldIndex

class OdooRelationalFieldCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inside(PyReferenceExpression::class.java)
                .with(object : PatternCondition<PsiElement>("isChainedFieldAccess") {
                    override fun accepts(element: PsiElement, context: ProcessingContext): Boolean {
                        val expr = PsiTreeUtil.getParentOfType(element, PyReferenceExpression::class.java)
                        return expr?.text?.contains(".") == true
                    }
                }),
            OdooRelationalFieldCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inside(PyStringLiteralExpression::class.java)
                .withSuperParent(2, PyArgumentList::class.java)
                .with(object : PatternCondition<PsiElement>("insideX2ManyCall") {
                    override fun accepts(element: PsiElement, context: ProcessingContext): Boolean {
                        val argList = PsiTreeUtil.getParentOfType(element, PyArgumentList::class.java, false)
                        val callExpr = argList?.parent as? PyCallExpression ?: return false
                        val callee = callExpr.callee?.text ?: return false

                        val validCalls = listOf(
                            "fields.Many2one",
                            "fields.One2many",
                            "fields.Many2many"
                        )
                        if (callee !in validCalls) return false

                        val args = callExpr.arguments
                        if (args.isEmpty()) return false

                        val index = args.indexOfFirst { it == PsiTreeUtil.getParentOfType(element, PyStringLiteralExpression::class.java) }
                        // Only autocomplete for first or second arg
                        return index == 0
                    }
                }),
            OdooModelCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(PyStringLiteralExpression::class.java),
            OdooInverseNameCompletionProvider()
        )
    }
}

package completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PatternCondition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.*
import com.intellij.openapi.diagnostic.Logger

class OdooModelCompletionContributor : CompletionContributor() {
    private val logger = Logger.getInstance(OdooModelCompletionContributor::class.java)

    init {
        logger.info("Initializing OdooModelCompletionContributor")

        // Autocomplete for self.env["model"]
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inside(PyStringLiteralExpression::class.java)
                .withSuperParent(2, PySubscriptionExpression::class.java)
                .with(object : PatternCondition<PsiElement>("insideSelfEnv") {
                    override fun accepts(element: PsiElement, context: ProcessingContext): Boolean {
                        val subscriptionExpr = PsiTreeUtil.getParentOfType(
                            element,
                            PySubscriptionExpression::class.java,
                            false
                        )
                        val operand = subscriptionExpr?.operand?.text ?: return false
                        return operand == "self.env" || operand == "request.env"
                    }
                }),
            OdooModelCompletionProvider()
        )
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inside(PyStringLiteralExpression::class.java)
                .withSuperParent(2, PyAssignmentStatement::class.java)
                .with(object : PatternCondition<PsiElement>("insideInheritAssignment") {
                    override fun accepts(element: PsiElement, context: ProcessingContext): Boolean {
                        val assignment = PsiTreeUtil.getParentOfType(element, PyAssignmentStatement::class.java)
                        val target = assignment?.leftHandSideExpression?.text
                        return target == "_inherit"
                    }
                }),
            OdooModelCompletionProvider()
        )
    }
}

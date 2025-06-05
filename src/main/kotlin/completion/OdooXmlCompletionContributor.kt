package completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.xml.XmlTokenType
import com.intellij.psi.xml.XmlToken
import com.intellij.util.ProcessingContext
import com.intellij.lang.xml.XMLLanguage

class OdooXmlCompletionContributor : CompletionContributor() {
    init {
        val language = XMLLanguage.INSTANCE
        // Text between tags
        extend(
            CompletionType.BASIC,
            XmlPatterns.psiElement(XmlTokenType.XML_DATA_CHARACTERS).withLanguage(language),
            OdooXmlCompletionProvider()
        )

        // Inside tag or attribute names
        extend(
            CompletionType.BASIC,
            XmlPatterns.psiElement(XmlTokenType.XML_NAME).withLanguage(language),
            OdooXmlCompletionProvider()
        )

        // Inside the value of a "widget" attribute
        extend(
            CompletionType.BASIC,
            XmlPatterns.psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
                .withLanguage(language)
                .withParent(
                    XmlPatterns.xmlAttributeValue().withParent(
                        XmlPatterns.xmlAttribute().withName("widget")
                    )
                ),
            OdooWidgetAttributeCompletionProvider()
        )

    }
}

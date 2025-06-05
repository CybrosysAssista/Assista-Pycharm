package dialogs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.*
import java.awt.BorderLayout
import java.awt.Dimension

class ModuleNameDialog(project: Project) : DialogWrapper(project) {
    private val moduleNameField = JTextField()

    init {
        title = "Create Odoo Module"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        val label = JLabel("Module Name:")
        moduleNameField.preferredSize = Dimension(200, 30)

        val inputPanel = JPanel(BorderLayout())
        inputPanel.add(label, BorderLayout.WEST)
        inputPanel.add(moduleNameField, BorderLayout.CENTER)

        panel.add(inputPanel, BorderLayout.CENTER)
        return panel
    }

    override fun doValidate(): ValidationInfo? {
        val moduleName = moduleNameField.text
        if (moduleName.isEmpty()) {
            return ValidationInfo("Module name cannot be empty", moduleNameField)
        }

        if (!moduleName.matches(Regex("^[a-zA-Z][a-zA-Z0-9_]*$"))) {
            return ValidationInfo("Module name must start with a letter and contain only letters, numbers, and underscores", moduleNameField)
        }

        return null
    }

    fun getModuleName(): String = moduleNameField.text
}
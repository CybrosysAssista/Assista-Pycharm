package actions

import com.intellij.openapi.actionSystem.DefaultActionGroup
import icons.OdooIcons

class OdooFileAction : DefaultActionGroup("New Odoo File", true) {
    init {
        templatePresentation.icon = OdooIcons.ODOO_ICON
        add(OdooModelFileAction())
        add(OdooViewFileAction())
        add(OdooAccessFileAction())
    }
}

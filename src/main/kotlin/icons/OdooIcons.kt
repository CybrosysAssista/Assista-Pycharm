package icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object OdooIcons {
    val ODOO_ICON: Icon = IconLoader.getIcon("/icons/odoo.svg", OdooIcons::class.java)
    val INIT_ICON: Icon = IconLoader.getIcon("/icons/init.png", OdooIcons::class.java)
    val MANIFEST_ICON: Icon = IconLoader.getIcon("/icons/manifest.png", OdooIcons::class.java)
    val MODEL_ICON: Icon = IconLoader.getIcon("/icons/model.png", OdooIcons::class.java)
    val CONTROLLER_ICON: Icon = IconLoader.getIcon("/icons/controller.png", OdooIcons::class.java)
}
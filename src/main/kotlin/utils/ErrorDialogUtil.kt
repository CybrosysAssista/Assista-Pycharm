package utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

object ErrorDialogUtil {
    fun showFileExistsError(project: Project, filePath: String) {
        Messages.showErrorDialog(
            project,
            "File\n'$filePath' already exists.",
            "Cannot Create File"
        )
    }

    fun showGenericError(project: Project, message: String) {
        Messages.showErrorDialog(project, message, "Error")
    }
}

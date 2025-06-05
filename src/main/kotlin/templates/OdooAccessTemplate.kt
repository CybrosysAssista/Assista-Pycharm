package templates

object OdooAccessTemplate {
    fun createAccessCSV(fileName: String): String {
        val modelName = fileName.replace(".csv", "")
        val project = "ProjectName"
        val model = "${project}_${modelName}".replace('.', '_')

        return """
            id,name,model_id:id,group_id:id,perm_read,perm_write,perm_create,perm_unlink
            access_${model}_user,Access ${model},model_${model},${project}.group_${project}_employee,1,1,1,1
        """.trimIndent()
    }
}

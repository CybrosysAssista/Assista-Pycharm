package templates

object OdooAccessTemplate {
    fun createAccessCSV(fileName: String): String {

        return """
            id,name,model_id:id,group_id:id,perm_read,perm_write,perm_create,perm_unlink
            access_model_name_user,access.model.name.user,model_model_name,base.group_user,1,1,1,1
        """.trimIndent()
    }
}

package sample

import kotlin.browser.*
import org.w3c.dom.url.URL

@JsName("alertPass")
fun myAlert(s:String){
    window.alert(s)
}
fun main() {
    val l = URL(window.document.URL)
    window.addEventListener("load",{
        if(l.searchParams.get("erro")!=null)
            myAlert(l.searchParams.get("erro").toString())
    })
    if(l.pathname == "/" && l.searchParams.get("cadastro")!=null)
        window.confirm("Faça login para acessar :)")
}
package sample

import kotlin.browser.*
import org.w3c.dom.url.URL
import kotlin.browser.*

@JsName("alertPass")
fun myAlert(s:String){
    window.alert("$s")
}
fun main() {
    window.addEventListener("load",{
        val l = URL(window.document.URL)
        if(l.searchParams.get("erro")!=null)
            myAlert(l.searchParams.get("erro").toString())
    })
}
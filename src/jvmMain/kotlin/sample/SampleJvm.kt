package sample

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.Parameters
import io.ktor.http.content.*
import io.ktor.request.document
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.request.receiveOrNull
import io.ktor.response.header
import io.ktor.response.respondFile
import io.ktor.response.respondRedirect
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponse
import kotlinx.html.*
import kotlinx.html.dom.document
import java.io.*
import java.nio.file.FileStore
import java.nio.file.Files
import java.nio.file.Paths
import java.net.URL
import java.io.FileOutputStream
import java.io.InputStream
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.time.measureTimedValue


// classe de exemplo que guarda a informacao importante de uma sessao (nomedeusuario)
data class Sessao(val usuario:String)

fun main() {
    embeddedServer(Netty, port = 8080) {
        // instala o suporte a sessoes no servidor
        // as sessoes permitem que alguma informacao fique salva entre os varios diretorios
        // que um unico cliente possa acessar, eh basicamente um login
        install(Sessions) {

            // os dados da sessao ficam armazenados em cookies
            cookie<sample.Sessao>("COOKIE_ATUAL", SessionStorageMemory()) {
                // a sessao eh valida para todos os diretorios dentro de "/"
                cookie.path = "/"
            }
        }

        routing {
            static("/static") {
                // nao foi usado nenhum script (deixei aqui para caso seja necessario depois)
                files("build/js/packages_imported/kotlin/1.3.60/")
                files("build/js/packages/MiniDriveEster/kotlin/")
                // pasta onde sao salvas as pastas dos usuarios
                files("usuarios")
            }

            // diretorio para o formulario de upload
            get("/upload") {
                call.respondHtml {
                    head {
                        title("Upload")
                    }
                    body {
                        div {
                            p {
                                +"Primeiro botao: selecionar arquivo"
                                br {}
                                +"Segundo botao: enviar o arquivo selecionado"
                                br {}
                            }
                            br{}

                            // um form cria um espaço que pode ter botoes, caixas de texto, etc que podem
                            // receber informacoes, elas ficam guardadas e relacionadas ao elemento form
                            // no documento
                            // form criado com método post (devolve coisas), acao "/salva", que foi definida
                            // no get("/salva"), e tipo multipartFormData, que define ser possivel receber dados
                            // do tipo arquivo com o form
                            form (method = FormMethod.post, action = "/salva", encType = FormEncType.multipartFormData) {

                                // aqui colocamos os elementos visuais e importantes ao form
                                // esse primeiro é um input hidden, ou seja, escondido ele define que a
                                // entrada não pode ter mais que "value" em bytes
                                input(type = InputType.hidden, name = "MAX_FILE_SIZE") {
                                    value = "4194304"

                                    // esse input recebe um arquivo qualquer, é um botao na pagina
                                    input(type = InputType.file, name = "arquivo")
                                }
                                br {}

                                // esse botao entrega o formulario e chama a "action" do form
                                input(InputType.submit) {
                                    value = "Enviar"
                                }
                            }
                        }
                        br{}
                        a (href = "/") {
                           +"Inicio"
                        }
                    }
                }
            }

            get("/arquivos") {
                call.respondHtml {
                    head {
                        title("Arquivos Salvos")
                    }
                    body {
                        +"Seus arquivos:"
                        br{}
                        br{}
                        // pega o nome do usuario atualmente logado
                        val sessao = call.sessions.get<Sessao>()?.usuario

                        // um array que contem todos os arquivos presentes na pasta do usuario
                        // se ela existir
                        var arquivos:Array<File>? = File("usuarios/$sessao").listFiles()

                        if (arquivos != null && arquivos.size != 0) {

                            var extensao = ""
                            // para cada arquivo dentro do array...
                            for (f in arquivos) {
                                extensao = f.extension
                                div {

                                    // exibe seu nome...
                                    +"${f.name} "
                                    br {}

                                    // link para o get("/visualizar"), que eh aberto numa nova guia gracas
                                    // ao target blank
                                    a(href = "/visualizar?nome=${f.name}", target = ATarget.blank) {
                                        +"Visualizar"
                                    }
                                    br {}

                                    // link para o get("/download")
                                    a(href = "/download?nome=${f.name}") {
                                        +"Download"
                                    }
                                    br{}
                                    a(href = "/excluir?nome=${f.name}") {
                                        +"Excluir"
                                    }
                                }
                                br{}
                            }
                        } else {
                            +"Você não tem arquivos salvos :o"
                            br{}
                        }
                        br{}
                        a (href = "/") {
                            +"Voltar"
                        }
                    }
                }
            }

            get("/excluir") {
                val nomedoarquivo = call.parameters["nome"]
                val usuario = call.sessions.get<Sessao>()?.usuario

                if (usuario != null) {
                    val arquivo = File("usuarios/$usuario/$nomedoarquivo")

                    if (arquivo.exists()) {
                        arquivo.delete()
                        call.respondHtml {
                            head {
                                title("Arquivo Excluido")
                            }
                            body {
                                +"Excluido com sucesso"
                                br{}
                                a (href = "/arquivos") {
                                    +"Voltar"
                                }
                            }
                        }
                    } else {
                        call.respondHtml {
                            head {
                                title("Nada Encontrado")
                            }
                            body {
                                +"Algo aconteceu... nenhum arquivo ou sessão encontrados..."
                                br{}
                                a (href = "/") {
                                    +"Inicio"
                                }
                            }
                        }
                    }
                }
            }

            get("/visualizar") {

                // no parametro nome (que eh passado pela url "/visualizar?nome=coisa" esta
                // o nome do arquivo requerido
                val nomedoarquivo = call.parameters["nome"]

                // pega o usuario atual
                val usuario = call.sessions.get<Sessao>()?.usuario

                // guarda o arquivo requerido
                val arquivo = File("usuarios/$usuario/$nomedoarquivo")

                // se ele existir..
                if (arquivo.exists()) {

                    // o exibe no browser
                    call.respondFile(arquivo)
                } else {
                    call.respondHtml {
                        head {
                            title("Erro de exibição")
                        }
                        body {
                            +"Esse arquivo não existe!"
                            a(href = "/arquivos") {
                                +"Voltar para Arquivos Salvos"
                            }
                        }
                    }
                }
            }

            get("/download") {

                // contem o nome do arquivo
                val nomedoarquivo = call.parameters["nome"]

                // se o parametro existir...
                if(nomedoarquivo != null) {
                    val usuario = call.sessions.get<Sessao>()?.usuario

                    // salva o arquivo requerido
                    val arquivo = File("usuarios/${usuario}/$nomedoarquivo")

                    // se ele existir...
                    if (arquivo.exists()) {

                        // cabeçalho que diz a resposta Http que o arquivo deve ser mandado como um download
                        call.response.header("Content-Disposition", "attachment; filename=${nomedoarquivo}")

                        // o arquivo em si
                        call.respondFile(arquivo)
                    } else {
                        call.respondHtml {
                            head {
                                title("Erro de download")
                            }
                            body {
                                +"Esse arquivo não existe!"
                                a (href = "/arquivos") {
                                    +"Voltar para Arquivos Salvos"
                                }
                            }
                        }
                    }
                }
            }

            get("/login") {
                call.respondHtml {
                    head {
                        title("MiniDrive Login")
                        script(src = "/static/kotlin.js") {  }
                        script(src = "/static/MiniDriveEster.js") {  }
                    }

                    // outro form para fazer login
                    // dessa vez com tipo applicationXWwwFormUrlEncoded porque esse tipo facilita a
                    // leitura de dados do tipo texto recebidos pelo form (ver /verifica para entender)
                    body {
                        form (action = "/verifica", method = FormMethod.post, encType = FormEncType.applicationXWwwFormUrlEncoded) {
                            +"Nome de usuario: "
                            input (type = InputType.text, name = "nomedeusuario")
                            br {}
                            +"Senha: "

                            // password faz as letrinhas aparecerem como bolinhas :D
                            input (type = InputType.password, name = "senha")
                            br {}
                            input (type = InputType.submit) {value = "Entrar"}
                        }
                        br{}
                        a(href="/"){
                            +"Inicio"
                        }
                    }
                }
            }

            post("/verifica") {
                val f = File("logins/logins.txt")

                // aqui ele recebe do metodo get que o chamou os parametros armazenados
                // por algum meio, no nosso caso um form
                val conteudoForm = call.receiveOrNull() ?: Parameters.Empty

                // aqui a justificativa para o método do form, ele permite acessar o
                // conteudo com a relacao dado-nome do input como uma tabela hash
                val user = conteudoForm["nomedeusuario"]

                if (conteudoForm == Parameters.Empty)
                    call.respondRedirect("/login?erro=Erro! Tente novamente")
                var us= false
                var ps = false
                f.forEachLine {
                    if(it.contains(user.toString())){ //verifica se o usuario existe
                        us = true

                        //verifica se a senha bate
                        if(it.substring(it.indexOf('§')+1, it.indexOf('¬')) == conteudoForm["senha"].toString())
                            ps = true
                    }
                }
                if (ps && us) {
                    // se ta tudo certo com as informacoes de login ele define a sessao
                    // atual com o nome do usuario q entrou
                    call.sessions.set(Sessao(user!!))

                    // volta pra primeira pagina
                    call.respondRedirect("/")
                } else {
                    // se n digitou a senha correta
                    call.respondRedirect("/login?erro=Usuario ou senha nao encontrados")
                }
            }

            get("/restorepassword"){
                call.respondHtml {
                    head {
                        title("MiniDrive Login")
                        script(src = "/static/kotlin.js") {  }
                        script(src = "/static/MiniDriveEster.js") {  }
                    }
                    body {
                        form (action = "/restore", method = FormMethod.post, encType = FormEncType.applicationXWwwFormUrlEncoded) {
                            +"Digite seu nome de usuário e a resposta da sua pergunta de segurança para recuperar sua senha."
                            br{}
                            +"Nome de usuário:"
                            input (type = InputType.text, name = "user")
                            br{}
                            +"Pergunta de segurança: "
                            select {
                                name = "sel"
                                +"Escolha a pergunta de segurança definida no cadastro"
                                option {
                                    value = "1"
                                    +"Qual o nome da usa mãe?"
                                }
                                option {
                                    value = "2"
                                    +"Qual o nome da rua onde você cresceu?"
                                }
                                option {
                                    value = "3"
                                    +"Qual o nome do seu animal de estimação na infância?"
                                }
                            }
                            input(type = InputType.text, name = "question") {  }
                            br{}
                            input (type = InputType.submit) {value = "Enviar"}
                        }
                        br{}
                        a(href="/"){
                            +"Inicio"
                        }
                    }
                }
            }

            post("/restore"){
                val form = call.receiveOrNull() ?: Parameters.Empty
                if(form["user"].toString()=="")
                    call.respondRedirect("/restorepassword?erro=Sem informaçao de usuario :(")
                else {
                    val f = File("logins/logins.txt")
                    var r = ""
                    var quest = ""
                    f.forEachLine {
                        if (it.contains(form["user"].toString())) {
                            r = it.substring(it.indexOf('§') + 1, it.indexOf('¬'))
                            quest = it.substring(it.indexOf('¬') + 1)//pergunta e resposta
                        }
                    }
                    if (r == "")
                        call.respondRedirect("/restorepassword?erro=Usuario nao encontrado :(")
                    else if (quest.substring(1) != form["question"].toString() || quest[0] != form["sel"].toString()[0])//se a pergunta(primeiro caractere) e a resposta(o resto) estiverem errados
                        call.respondRedirect("/restorepassword?erro=Pergunta e/ou resposta incorreta:(")//volta
                    //Nao passei a senha como parametro pro aviso pq o navegador salva, uma falha enorme
                    //entao a pagina e criada aqui
                    else {
                        call.respondHtml {
                            body {
                                p {
                                    +"Sua senha é: $r"
                                }
                                br {}
                                a(href = "/") {
                                    +"Inicio"
                                }
                            }
                        }
                    }
                }
            }

            post("/salva") {

                // guarda em recebidos tudo que foi conseguido com o form
                var recebidos = call.receiveMultipart()

                // indica se algum arquivo chegou pelo formulario
                var foiRecebido = false
                var iniciouSessao = true

                // percorre o "vetor" de dados recebidos
                for (infoLida in recebidos.readAllParts()) {

                    // se for um arquivo...
                    if (infoLida is PartData.FileItem) {

                        // armazena os bytes de informacao do arquivo
                        val conteudo = infoLida.streamProvider().readBytes()
                        if (conteudo.isNotEmpty()) {

                            // achamos um arquivo
                            foiRecebido = true
                        }

                        // obtem o nome da sessao atual
                        val sessao = call.sessions.get<Sessao>()?.usuario

                        // se houver sessao iniciada...
                        if (sessao != null) {
                            // e se houver arquivo recebido...
                            if (foiRecebido) {

                                // cria uma nova pasta com o nome do usuario, se ja nao existir
                                File("usuarios/${sessao}").mkdirs()

                                // obtem o tamanho, em bytes, da pasta
                                var arquivosPasta = File("usuarios/${sessao}").listFiles()
                                var tamanho = 0L
                                if (arquivosPasta != null)
                                    for (f in arquivosPasta) {
                                        if (f is File)
                                            tamanho += f.length()
                                    }

                                // se o diretório tiver menos que 5mb de conteudo, pode salvar lá
                                if (tamanho/1000000L + (conteudo.size/1000000L) < 5) {

                                    // cria um novo arquivo nessa pasta, com os bytes lidos
                                    Files.write(Paths.get("usuarios/${sessao}", infoLida.originalFileName
                                        ?.filter { x:Char -> x.toInt() in 1..254 }), conteudo)
                                    call.respondHtml {
                                        body {
                                            +"Foi salvo no usuario $sessao"
                                            br {}
                                            a(href = "/") {
                                                +"Inicio"
                                            }
                                        }
                                    }
                                } else {
                                    call.respondHtml {
                                        body {
                                            +"Você ultrapassou o limite de armazenamento!"
                                            br{}
                                            a(href = "/") {
                                                +"Inicio"
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            iniciouSessao = false

                            // se não foi iniciada nenhuma sessao...
                            call.respondHtml {
                                body {
                                    +"Você não iniciou uma sessão! "
                                    a (href = "/login") {
                                        +"Login?"
                                    }
                                    br{}
                                    a (href = "/") {
                                        +"Inicio"
                                    }
                                }
                            }
                        }
                    }
                }

                // so pra ter alguma resposta caso n tenha nenhum arquivo no form
                if (!foiRecebido && iniciouSessao) {
                    call.respondHtml {
                        head {
                            title("Erro!")
                        }
                        body {
                            +"Você não selecionou um arquivo! "
                            a (href = "/upload") {
                                +"Upload?"
                            }
                        }
                    }
                }
            }

            get("/sign"){
                //quase a mesma coisa que o login, mas com um espaco para repetir senha
                call.respondHtml {
                    head{
                        title("Sing in")
                        script(src = "/static/kotlin.js") {  }
                        script(src = "/static/MiniDriveEster.js") {  }
                    }
                    body {
                        form(action = "/confirma", method = FormMethod.post, encType = FormEncType.applicationXWwwFormUrlEncoded) {
                            name = "sign"
                            id = "sign"
                            +"Nome de usuário:"
                            input (type = InputType.text, name = "user")
                            br {}
                            +"Senha: "
                            input (type = InputType.password, name = "password")
                            br {}
                            +"Repita a senha:"
                            input (type = InputType.password, name = "password2")
                            br{}
                            select {
                                name = "sel"
                                option {
                                    value = "1"
                                    +"Qual o nome da usa mãe?"
                                }
                                option {
                                    value = "2"
                                    +"Qual o nome da rua onde você cresceu?"
                                }
                                option {
                                    value = "3"
                                    +"Qual o nome do seu animal de estimação na infância?"
                                }
                            }
                            input(type = InputType.text,name = "question") {  }
                            br{}
                            input(type = InputType.submit) {value = "Cadastro"}
                        }
                        br{}
                        a(href = "/"){
                            +"Voltar"
                        }
                    }
                }
            }

            post("/confirma") {
                //so verifica as condicoes da senha e do usuario, não tem página
                val signForm = call.receiveOrNull() ?: Parameters.Empty
                if(signForm == Parameters.Empty)
                    call.respondRedirect("/sign")
                else if(signForm["user"] == "" || signForm["password"] == "" || signForm["password2"] == "" || signForm["question"]=="")
                    call.respondRedirect("/sign?erro=Preencha todos os campos! >:(")
                else if(signForm["password"] != signForm["password2"])
                    call.respondRedirect("/sign?erro=Senhas nao correspodentes D:")
                else{
                    val f = File("logins/logins.txt")
                    var usuarioJaCadastrado = false

                    val diretorio = File("usuarios")
                    val usuarios = diretorio.list()

                    f.forEachLine {
                        if(it.contains(signForm["user"].toString()))
                            usuarioJaCadastrado=true
                    }
                    if(usuarioJaCadastrado)
                        call.respondRedirect("/sign?erro=Nome de usuario em uso :(")
                    else if (signForm["user"].toString().contains('§') || signForm["password"].toString().contains('§')||
                        signForm["user"].toString().contains('¬')|| signForm["password"].toString().contains('¬'))
                        call.respondRedirect("/sign?erro=Caracteree '§' e '¬' invalidos para cadastro!")
                    else if (signForm["user"].toString().length > 15 || signForm["user"].toString().length < 3)
                        call.respondRedirect("/sign?erro=Usuario entre 3 e 15 caracteres!")
                    else if (signForm["password"].toString().length > 20 || signForm["password"].toString().length < 6)
                        call.respondRedirect("/sign?erro=Senha entre 6 e 20 caracteres!")
                    else if (diretorio.isDirectory && usuarios != null && usuarios.size > 19) {
                        call.respondHtml {
                            head {
                                title("Sentimos muito :(")
                            }
                            body {
                                +"O servidor já está cheio... tente se cadastrar depois :D"
                                br{}
                                a(href = "/") {
                                    +"Inicio"
                                }
                            }
                        }
                    } else {
                        f.appendText("${signForm["user"].toString()}§${signForm["password"].toString()}¬${signForm["sel"].toString()}${signForm["question"].toString()}\n")
                        File("usuarios/${signForm["user"]}").mkdirs()
                        call.respondRedirect("/?cadastro=ok")
                    }
                }
            }

            get("/logout") {

                // essa chamada eh suficiente para desfazer a sessao atual
                call.sessions.clear<Sessao>()
                call.respondHtml {
                    head {
                        title("Logout")
                    }
                    body {
                        +"Você saiu!"
                        br {}
                        a (href = "/") {
                            +"Inicio"
                        }
                    }
                }
            }

            get("/") {
                call.respondHtml {
                    head {
                        title("LPF MiniDrive")
                        script(src = "/static/kotlin.js") {  }
                        script(src = "/static/MiniDriveEster.js") {  }
                    }
                    body {
                        if (call.parameters["cadastro"] == "ok") {
                            +"Faça login para acessar :)"
                            br{}
                        }
                        +"Olá ${call.sessions.get<Sessao>()?.usuario?:"visitante"}!"
                        if (call.sessions.get<Sessao>() == null) {
                            br{}
                            a(href = "/login") {
                                +"Login"
                            }
                            br{}
                            a(href="/sign"){
                                +"Sign in"
                            }
                            br{}
                            a(href="/restorepassword"){
                                +"Esqueceu sua senha?"
                            }
                        }
                        if (call.sessions.get<Sessao>() != null) {
                            br{}
                            a(href = "/logout") {
                                +"Logout"
                            }
                            br{}
                            a (href = "/upload") {
                                +"Upload"
                            }
                            br{}
                            a (href = "/arquivos") {
                                +"Arquivos Salvos"
                            }
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}
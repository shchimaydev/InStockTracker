package com.ist.instocktracker

//fun main() {
//    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
//        .start(wait = true)
//}


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

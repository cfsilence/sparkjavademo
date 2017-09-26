import groovy.json.JsonOutput

import javax.servlet.MultipartConfigElement
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

import static spark.Spark.*

class Bootstrap {

    static void main(String[] args) {

        staticFileLocation("/public")

        port(9000)

        get "/hello", { req, res -> "Hello World" }
        get "/goodbye", { req, res -> "Goodbye World" }

        get "/json", { req, res ->
            res.type("application/json")
            JsonOutput.toJson([json: true, uncool: false])
        }

        post "/qmap", { req, res ->

            def a = req.queryMap().get("user", "name").value()
            def b = req.queryMap().get("user").get("name").value()
            def c = req.queryMap("user").get("age").integerValue()
            def d = req.queryMap("user").toMap()

            JsonOutput.toJson(
                    [
                            a: a, b: b, c: c, d: d
                    ]
            )
        }

        post "/upload", { req, res ->
            def uploadDir = new File("c:/temp")
            Path tempFile = Files.createTempFile(uploadDir.toPath(), "", "")
            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            def upload = req.raw().getPart("uploadFile")
            def info = [:]

            InputStream is = upload.getInputStream()
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING)
            info = [
                    name: tempFile.fileName.toString(),
                    path: tempFile.normalize().toString(),
                    size: tempFile.size().toDouble(),
                    friendlySize: "${(tempFile.size().toDouble() / 1024 / 1024).round(2).toString()} MB",
                    originalName: upload.getHeader('content-disposition').tokenize(';').find {
                        it.contains('filename')
                    }.tokenize('=').last().replace('"', ''),
            ]
            tempFile.toFile().delete()

            return JsonOutput.toJson(info)
        }

        notFound("<html><body><h1>Custom 404 handling</h1></body></html>")

        internalServerError("<html><body><h1>Custom 500 handling</h1></body></html>")

        exception(Exception.class, { exception, req, res ->
            JsonOutput.toJson(
                    [exception: exception]
            )
        })

    }

}
import codes.recursive.domain.Person
import codes.recursive.service.PersonService
import groovy.json.JsonOutput
import nz.net.ultraq.thymeleaf.LayoutDialect
import spark.ModelAndView
import spark.template.thymeleaf.ThymeleafTemplateEngine

import javax.servlet.MultipartConfigElement
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

import static spark.Spark.*

class Bootstrap {

    static void main(String[] args) {

        PersonService personService = new PersonService()

        ThymeleafTemplateEngine engine = new ThymeleafTemplateEngine()
        engine.templateEngine.addDialect(new LayoutDialect())

        staticFileLocation("/public")
        staticFiles.expireTime(10)
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
                            "req.queryMap().get('user', 'name').value()": a,
                            "req.queryMap().get('user').get('name').value()": b,
                            "req.queryMap('user').get('age').integerValue()": c,
                            "req.queryMap('user').toMap()": d,
                    ]
            )
        }

        post "/upload", { req, res ->
            def uploadDir = new File(System.getProperty("java.io.tmpdir"))
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

        path "/mongo", {
            post "/create", {req, res ->
                Person person = new Person(firstName: req.queryMap().get('firstName').value(), lastName:  req.queryMap().get('lastName').value())
                personService.save(person)
                JsonOutput.toJson(
                        [
                                saved: true,
                                person: person,
                        ]
                )
            }
            get "/list", {req, res ->
                JsonOutput.toJson([people: personService.list()])
            }
        }

        get "/thymeleaf", { req, res ->
            def list = []
            def fNames = ['Hort', 'Cassandre', 'Wallache', 'Robbyn', 'Quent', 'Johannes', 'Evonne', 'Lolly', 'Sadie', 'Sara-ann', 'Marnia', 'Merle', 'Rafferty', 'Trueman', 'Paddy', 'Leshia', 'Oliviero', 'Della', 'Veronika', 'Joaquin', 'Jane', 'Evania', 'Livy', 'Koenraad', 'Daryl', 'Laverne', 'Neille', 'Ardra', 'Casper', 'Bink', 'Barbra', 'Cammie', 'Doroteya', 'Abbye', 'Pammy', 'Emerson', 'Keely', 'Jacquelyn', 'Rowe', 'Justus', 'Abby', 'Titos', 'Fabio', 'Darla', 'Dru', 'Hamilton', 'Shari', 'Elnore', 'Dallas', 'Stanislaus']
            def lNames = ['Ottewell', 'Rippingale', 'Stennet', 'Sigge', 'Hattrick', 'Rideout', 'Turfus', 'Purvey', 'Barhams', 'Corneille', 'Napoli', 'Colliard', 'Gerard', 'Capel', 'Klimkov', 'Conrath', 'Moultrie', 'Akeherst', 'Branson', 'Lyman', 'Honsch', 'Rishworth', 'Baumer', 'Pappin', 'Samter', 'Spillane', 'Gallaccio', 'Courtin', 'Connue', 'Kiggel', 'Lugden', 'Swetenham', 'Angove', 'Duggary', 'Carlens', 'Crummay', 'Bubear', 'Setter', 'Hender', 'Hacket', 'Butterfield', 'Domesday', 'Renvoise', 'Clilverd', 'Dawidman', 'Tiery', 'Windous', 'Twidle', 'Hutchence', 'Luppitt']
            def random = new Random()

            10.times {
                list << [id: it, firstName: fNames[random.nextInt(fNames.size())],  lastName: lNames[random.nextInt(lNames.size())]]
            }

            def model = [users: list]

            return engine.render(new ModelAndView(model, "thymeleaf"))
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
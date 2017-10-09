package codes.recursive.domain

import groovy.transform.ToString
import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.Entity
import org.mongodb.morphia.annotations.Id

@Entity("person")
@ToString(includeNames = true)
class Person {
    @Id
    ObjectId id
    String firstName
    String lastName

    String getId(){
        return id.toString()
    }
}

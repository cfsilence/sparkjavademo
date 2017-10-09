package codes.recursive.service

import codes.recursive.domain.Person
import com.mongodb.MongoClient
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.Morphia

class PersonService {
    Morphia _morphia
    Datastore _datastore

    def getMorphia() {
        if( !_morphia ) {
            _morphia = new Morphia()
            _morphia.mapPackage("codes.recursive.domain");
        }
        return _morphia
    }

    def getDatastore() {
        if( !_datastore ) {
            _datastore = morphia.createDatastore(new MongoClient("localhost", 27017), "mongodb");
            _datastore.ensureIndexes()
        }
        return _datastore
    }

    def save(Person person) {
        datastore.save( person )
    }

    def list() {
        def q = datastore.createQuery(Person.class)
        return q.asList()
    }

    def findById(String id){
        def q = datastore.createQuery(Person.class)
        return q.filter("id", new ObjectId(id)).asList()?.first()
    }
}

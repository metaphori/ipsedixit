package asw1022.repositories;

import asw1022.db.CardDB;
import asw1022.model.User;
import asw1022.db.UserDB;
import asw1022.kb.AppKB;
import asw1022.model.dixit.Card;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
public class CardRepository extends Repository<Card> {

    public CardRepository(String xmlDB) throws JAXBException{
        super(xmlDB, AppKB.XML_SCHEMA_DIR+"db/card_db.xsd", CardDB.class, Card.class);
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package asw1022.repositories;

import asw1022.db.MatchDB;
import asw1022.db.UserDB;
import asw1022.model.User;
import asw1022.model.dixit.Match;
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
public class MatchRepository extends Repository<Match> {

    public MatchRepository(String xmlDB) throws JAXBException{
        super(xmlDB, MatchDB.class, Match.class);
    }
    

}
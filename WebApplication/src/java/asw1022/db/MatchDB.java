package asw1022.db;

import asw1022.model.dixit.Match;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Roberto Casadei <roberto.casadei12@studio.unibo.it>
 */
@XmlRootElement(name="matches")
@XmlAccessorType (XmlAccessType.FIELD)
public class MatchDB implements IDB<Match> {
    
    @XmlElement(name="match")
    protected List<Match> matches;
    
    public List<Match> getItems(){ return matches; }
    public void setItems(List<Match> matches){ this.matches = matches; }
}


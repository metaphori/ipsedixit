package asw1022.db;

import asw1022.model.dixit.MatchConfiguration;
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
public class MatchConfigurationDB implements IDB<MatchConfiguration> {
    
    @XmlElement(name="matchConfigs")
    protected List<MatchConfiguration> matchConfigs;
    
    public List<MatchConfiguration> getItems(){ return matchConfigs; }
    public void setItems(List<MatchConfiguration> matches){ this.matchConfigs = matches; }
}


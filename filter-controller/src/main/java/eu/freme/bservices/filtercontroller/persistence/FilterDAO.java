package eu.freme.bservices.filtercontroller.persistence;

import eu.freme.common.persistence.dao.OwnedResourceDAO;
import org.springframework.stereotype.Component;

/**
 * Created by Arne on 11.12.2015.
 */
@Component
public class FilterDAO extends OwnedResourceDAO<Filter> {

    @Override
    public String className() {
        return Filter.class.getSimpleName();
    }

    @Override
    public Filter findOneByIdentifierUnsecured(String identifier){
        return ((FilterRepository)repository).findOneByName(identifier);
    }

}

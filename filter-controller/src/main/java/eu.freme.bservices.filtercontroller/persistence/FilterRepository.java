package eu.freme.bservices.filtercontroller.persistence;

import eu.freme.common.persistence.repository.OwnedResourceRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Arne on 11.12.2015.
 */
public interface FilterRepository extends OwnedResourceRepository<Filter> {
    Filter findOneByName(String name);
}

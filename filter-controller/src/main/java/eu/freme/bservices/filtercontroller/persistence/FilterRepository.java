package eu.freme.bservices.filtercontroller.persistence;

import eu.freme.common.persistence.repository.OwnedResourceRepository;

/**
 * Created by Arne on 11.12.2015.
 */
public interface FilterRepository extends OwnedResourceRepository<Filter> {
    Filter findOneByName(String name);
}

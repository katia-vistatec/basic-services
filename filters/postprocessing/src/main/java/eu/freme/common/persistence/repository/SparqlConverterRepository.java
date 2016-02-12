package eu.freme.common.persistence.repository;

import eu.freme.common.persistence.model.SparqlConverter;

/**
 * Created by Arne on 11.12.2015.
 */

public interface SparqlConverterRepository extends OwnedResourceRepository<SparqlConverter> {
    SparqlConverter findOneByName(String name);
}

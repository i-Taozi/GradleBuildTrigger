package io.dddbyexamples.factory.delivery.planning.definition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(
        path = "delivery-definitions",
        collectionResourceRel = "delivery-definitions",
        itemResourceRel = "delivery-definition")
public interface DeliveryPlannerDefinitionDao extends JpaRepository<DeliveryPlannerDefinitionEntity, String> {

}

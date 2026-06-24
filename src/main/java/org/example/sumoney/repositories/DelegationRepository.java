package org.example.sumoney.repositories;

import org.example.sumoney.entities.Delegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DelegationRepository extends JpaRepository<Delegation,Long>, JpaSpecificationExecutor<Delegation> {

    List<Delegation> findByUser_Id(Long userId);
    Optional<Delegation> findByIdAndUser_Id(Long delegationId, Long userId);
}

package com.valumetric.repository;

import com.valumetric.document.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("{ '$or': [ " +
            "{ 'alerts': { '$elemMatch': { 'alertType': 'LOW_HCROI', 'isResolved': false } } }, " +
            "{ 'alerts': { '$elemMatch': { 'alertType': 'LOW_SCORE', 'isResolved': false } } } " +
            "] }")
    List<Employee> findRedZoneEmployees();

    List<Employee> findByIsEnabledTrue();

    List<Employee> findByRole(Employee.Role role);
}

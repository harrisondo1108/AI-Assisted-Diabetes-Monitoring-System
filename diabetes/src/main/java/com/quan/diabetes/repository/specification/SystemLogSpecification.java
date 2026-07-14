package com.quan.diabetes.repository.specification;

import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.entity.SystemLog;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.entity.Role;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SystemLogSpecification {

    public static Specification<SystemLog> filterLogs(String keyword, String roleId, String action, LocalDate fromDate, LocalDate toDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join with User
            Join<SystemLog, User> userJoin = root.join("account", JoinType.LEFT);
            Join<User, Profile> profileJoin = userJoin.join("profile", JoinType.LEFT);
            Join<User, Patient> patientJoin = userJoin.join("patient", JoinType.LEFT);
            Join<User, Role> roleJoin = userJoin.join("role", JoinType.LEFT);

            // 1. Keyword search (AccountID, FullName, PhoneNumber)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                Predicate matchAccountId = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("userId")), likeKeyword);
                Predicate matchPhone = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("phoneNumber")), likeKeyword);
                Predicate matchProfileName = criteriaBuilder.like(criteriaBuilder.lower(profileJoin.get("fullName")), likeKeyword);
                Predicate matchPatientName = criteriaBuilder.like(criteriaBuilder.lower(patientJoin.get("fullName")), likeKeyword);

                predicates.add(criteriaBuilder.or(matchAccountId, matchPhone, matchProfileName, matchPatientName));
            }

            // 2. Role filter
            if (roleId != null && !roleId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(roleJoin.get("roleId"), roleId));
            }

            // 3. Action filter
            if (action != null && !action.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }

            // 4. Date filter
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate.atStartOfDay()));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate.atTime(23, 59, 59)));
            }

            // Fix for count query where order by or fetch might cause issue
            // Since we are returning a page, distinct might be needed if multiple joins return duplicates
            // But since profile and patient are OneToOne, it should be 1-to-1.
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.Booking;
import com.electricitybusiness.api.model.Terminal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TerminalRepositoryImpl implements TerminalRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Terminal> searchTerminalsWithCriteria(
            BigDecimal longitude,
            BigDecimal latitude,
            Double radius,
            Boolean occupied,
            LocalDateTime startingDate,
            LocalDateTime endingDate) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Terminal> query = cb.createQuery(Terminal.class);
        Root<Terminal> root = query.from(Terminal.class);

        List<Predicate> predicates = new ArrayList<>();


        // Add location radius filter
        if (longitude != null && latitude != null && radius != null) {
            // Haversine formula components
            Expression<Double> latRadians = cb.function("radians", Double.class, root.get("latitude"));
            Expression<Double> lonRadians = cb.function("radians", Double.class, root.get("longitude"));
            Expression<Double> latParamRadians = cb.function("radians", Double.class, cb.literal(latitude));
            Expression<Double> lonParamRadians = cb.function("radians", Double.class, cb.literal(longitude));

            Expression<Double> deltaLat = cb.diff(latRadians, latParamRadians);
            Expression<Double> deltaLon = cb.diff(lonRadians, lonParamRadians);

            // a = sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)
            Expression<Double> a = cb.sum(
                    cb.prod(cb.function("sin", Double.class, cb.prod(deltaLat, cb.literal(0.5))),
                            cb.function("sin", Double.class, cb.prod(deltaLat, cb.literal(0.5)))),
                    cb.prod(
                            cb.prod(
                                    cb.function("cos", Double.class, latRadians),
                                    cb.function("cos", Double.class, latParamRadians)
                            ),
                            cb.prod(
                                    cb.function("sin", Double.class, cb.prod(deltaLon, cb.literal(0.5))),
                                    cb.function("sin", Double.class, cb.prod(deltaLon, cb.literal(0.5)))
                            )
                    )
            );

            // Distance = 2 * R * asin(√a) where R = 6371 km (Earth radius)
            Expression<Double> distance = cb.prod(
                    cb.literal(12742.0), // 2 * 6371
                    cb.function("asin", Double.class, cb.function("sqrt", Double.class, a))
            );

            predicates.add(cb.le(distance, radius));
        }


        // Add occupied filter
        if (occupied != null) {
            predicates.add(cb.equal(root.get("occupied"), occupied));
        }

        // Add date availability filter
        if (startingDate != null && endingDate != null) {
            Subquery<Long> reservationSubquery = query.subquery(Long.class);
            Root<Booking> reservationRoot = reservationSubquery.from(Booking.class);

            reservationSubquery.select(cb.count(reservationRoot))
                    .where(
                            cb.and(
                                    cb.equal(reservationRoot.get("Terminal"), root),
                                    cb.greaterThan(reservationRoot.get("endingDate"), startingDate),
                                    cb.lessThan(reservationRoot.get("startingDate"), endingDate)
                            )
                    );

            predicates.add(cb.equal(reservationSubquery, 0L));
        }

        query.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }
}

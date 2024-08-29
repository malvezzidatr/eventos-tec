package com.eventostec.api.repositories;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eventostec.api.domain.event.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e from Event e LEFT JOIN FETCH e.address a WHERE e.data >= :currentDate")
    public Page<Event> findUpComingEvents(@Param("currentDate") Date currentDate, Pageable pageable);

    @Query("SELECT e FROM Event e LEFT JOIN e.address a " +
       "WHERE (:title = '' OR e.title LIKE %:title%) AND " +
       "(:city = '' OR a.city LIKE %:city%) AND " +
       "(:uf = '' OR a.uf LIKE %:uf%) AND " +
       "(e.data >= :startDate AND e.data <= :endDate)")
    public Page<Event> findFilteredEvents(@Param("title") String title,
                                          @Param("city") String city,
                                          @Param("uf") String uf,
                                          @Param("startDate") Date startDate,
                                          @Param("endDate") Date endDate,
                                          Pageable pageable);

}

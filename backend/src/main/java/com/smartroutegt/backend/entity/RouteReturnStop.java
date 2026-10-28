package com.smartroutegt.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "route_return_stop", uniqueConstraints = {
    @UniqueConstraint(name = "uq_return_order", columnNames = {"route_id", "orden"}),
    @UniqueConstraint(name = "uq_return_stop", columnNames = {"route_id", "stop_id"})
})
public class RouteReturnStop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "route_id", nullable = false)
    private Route route;
    @ManyToOne @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;
    @Column(nullable = false)
    private int orden;
    public RouteReturnStop() {}
    public RouteReturnStop(Route route, Stop stop, int orden) {
        this.route = route; this.stop = stop; this.orden = orden;
    }
    public Long getId() { return id; }
    public Route getRoute() { return route; }
    public Stop getStop() { return stop; }
    public int getOrden() { return orden; }
}

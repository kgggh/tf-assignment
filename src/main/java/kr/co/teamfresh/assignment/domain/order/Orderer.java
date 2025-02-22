package kr.co.teamfresh.assignment.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Orderer {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;
}

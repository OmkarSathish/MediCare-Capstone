package com.capstone.healthcare.diagnosticcenter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CenterTestOfferingKey implements Serializable {

    @Column(name = "center_id")
    private int centerId;

    @Column(name = "test_id")
    private int testId;
}

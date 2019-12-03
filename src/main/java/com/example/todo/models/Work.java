package com.example.todo.models;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Data
public class Work {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 2, max = 100)
    private String workName;

    @NotNull
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @NotNull
    @Temporal(TemporalType.DATE)
    private Date endDate;

    public enum Status {
        PLANNING,
        DOING,
        COMPLETE
    }
    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;
}
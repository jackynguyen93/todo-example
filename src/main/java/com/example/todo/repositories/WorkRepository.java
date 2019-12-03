package com.example.todo.repositories;

import com.example.todo.models.Work;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface WorkRepository extends PagingAndSortingRepository<Work, Long> {
}

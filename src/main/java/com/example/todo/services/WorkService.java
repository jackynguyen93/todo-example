package com.example.todo.services;

import com.example.todo.models.Work;
import com.example.todo.repositories.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkService {

    @Autowired
    private WorkRepository workRepository;

    public List<Work> getAllWorks(Integer pageNo, Integer pageSize, String sortBy, Sort.Direction direction) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(direction, sortBy));
        Page<Work> pagedResult = workRepository.findAll(paging);

        if(pagedResult.hasContent()) {
            return pagedResult.getContent();
        } else {
            return new ArrayList<>();
        }
    }

    public Optional<Work> getWork(Long id) {
        return workRepository.findById(id);
    }

    public Work createWork(Work work) {
        return workRepository.save(work);
    }

    public Work updateWork(Long id, Work work) {
        work.setId(id);
        return workRepository.save(work);
    }

    public void deleteWork(Long id) {
        workRepository.deleteById(id);
    }
}

package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.models.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    // Заявки пользователя — новые сверху
    List<ItemRequest> findByRequestor_IdOrderByCreatedDesc(Long requestorId);

    // Заявки других пользователей постранично, новые сверху
    Page<ItemRequest> findByRequestor_IdNotOrderByCreatedDesc(Long requestorId, Pageable pageable);

}
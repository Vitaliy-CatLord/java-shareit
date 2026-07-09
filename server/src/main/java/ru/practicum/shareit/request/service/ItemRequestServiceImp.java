package ru.practicum.shareit.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.models.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemRequestServiceImp implements ItemRequestService {

    ItemRequestRepository requestRepository;
    UserRepository userRepository;
    ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestCreateDto dto) {
        if (dto == null || dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ValidationException("У запроса должно быть описание");
        }
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователя с ID %s не найден", userId)));

        ItemRequest req = ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
        return ItemRequestMapper.toItemRequestDto(requestRepository.save(req));
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        userIsExist(userId);

        List<ItemRequest> list = requestRepository.findByRequestor_IdOrderByCreatedDesc(userId);
        return attachItems(list);
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId, int from, int size) {
        userIsExist(userId);
        if (from < 0 || size <= 0) throw new ValidationException("Неверно задана пагинация");
        Pageable page = PageRequest.of(from / size, size);
        Page<ItemRequest> requestPage = requestRepository.findByRequestor_IdNotOrderByCreatedDesc(userId, page);
        return attachItems(requestPage.getContent());
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        userIsExist(userId);
        ItemRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос на вещь не найден" + requestId));
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(req);
        List<ItemDto> related = itemRepository.findByRequest_Id(requestId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
        dto.setItems(related);
        return dto;
    }

    private void userIsExist(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("Пользователя с ID %s не найден", userId));
        }
    }

    private List<ItemRequestDto> attachItems(List<ItemRequest> list) {
        List<ItemRequestDto> dtos = list.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();

        List<Long> requestIds = list.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<Item>> itemsByRequest = itemRepository.findByRequest_IdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(
                        i -> i.getRequest().getId()
                ));

        return dtos.stream().map(d -> {
            List<ItemDto> related = itemsByRequest.getOrDefault(d.getId(), List.of())
                    .stream()
                    .map(ItemMapper::toItemDto)
                    .toList();
            d.setItems(related);
            return d;
        }).toList();
    }

}

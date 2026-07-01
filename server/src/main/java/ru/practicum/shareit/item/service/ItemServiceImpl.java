package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.models.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mappers.CommentMapper;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.item.models.Comment;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.models.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemServiceImpl implements ItemService {

    CommentRepository commentRepository;
    BookingRepository bookingRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;
    ItemRequestRepository requestRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto dto) {
        if (dto == null) {
            throw new ValidationException("Предмет должен быть не нулевым");
        }
        if (dto.getName().isEmpty() || dto.getName().isBlank()) {
            throw new ValidationException("У предмета должно быть название");
        }
        if (dto.getDescription().isEmpty() || dto.getDescription().isBlank()) {
            throw new ValidationException("У предмета должно быть описание");
        }
        if (dto.getAvailable() == null) {
            throw new ValidationException("У предмета должна быть обозначена Доступность");
        }
        Optional<User> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new NotFoundException(String.format("Пользователя с ID %s не существует", userId));
        }
        // Если передан requestId — подтянем заявку и свяжем вещь
        ItemRequest request = null;
        if (dto.getRequestId() != null) {
            request = requestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Реквест не найден: " + dto.getRequestId()));
        }

        Item item = ItemMapper.toItem(dto, owner.get(), request);
        item.setId(null);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto dto) {
        Optional<User> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new NotFoundException(String.format("Пользователя с ID %s не существует", userId));
        }
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            throw new NotFoundException(String.format("Предмет с ID %s не найден", itemId));
        }
        if (!item.get().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId +
                    " не является владельцем вещи с id = " + itemId);
        }
        Item oldItem = item.get();

        String name = dto.getName();
        if (name != null && !name.isBlank()) {
            oldItem.setName(name);
        }
        String description = dto.getDescription();
        if (description != null && !description.isBlank()) {
            oldItem.setDescription(description);
        }
        Boolean isAvailable = dto.getAvailable();
        if (isAvailable != null) {
            oldItem.setAvailable(isAvailable);
        }
        if (dto.getRequestId() != null) {
            ItemRequest req = requestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request не найден: " + dto.getRequestId()));
            oldItem.setRequest(req);
        }
        itemRepository.save(oldItem);
        return ItemMapper.toItemDto(oldItem);

    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        Optional<User> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new NotFoundException(String.format("Пользователя с ID %s не существует", userId));
        }
        Optional<Item> itemGet = itemRepository.findById(itemId);
        if (itemGet.isEmpty()) {
            throw new NotFoundException(String.format("Вещь с ID %s не существует", itemId));
        }

        Item item = itemGet.get();

        // комментарии всегда
        List<CommentDto> commentsDto = commentRepository.findByItem_IdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        // last/next — показываем ТОЛЬКО владельцу
        BookingShortDto last = null;
        BookingShortDto next = null;
        if (Objects.equals(item.getOwner().getId(), userId)) {
            LocalDateTime now = LocalDateTime.now();
            last = bookingRepository
                    .findTop1ByItemIdAndStartDateBeforeAndStatusOrderByStartDateDesc(itemId, now, BookingStatus.APPROVED)
                    .map(b -> BookingShortDto.builder().id(b.getId()).bookerId(b.getBooker().getId()).build())
                    .orElse(null);
            next = bookingRepository
                    .findTop1ByItemIdAndStartDateAfterAndStatusOrderByStartDateAsc(itemId, now, BookingStatus.APPROVED)
                    .map(b -> BookingShortDto.builder().id(b.getId()).bookerId(b.getBooker().getId()).build())
                    .orElse(null);
        }

        return ItemMapper.toItemDto(item, last, next, commentsDto);
    }

    @Override
    public List<ItemDto> getOwnerItems(Long userId) {
        List<Item> ownerItems = itemRepository.findByOwner_IdOrderByIdAsc(userId);
        if (ownerItems.isEmpty()) return List.of();

        List<Long> itemsId = ownerItems.stream().map(Item::getId).toList();

        // сгруппированные по id предмета комменты
        Map<Long, List<Comment>> commentsByItemId = commentRepository.findByItem_IdInOrderByCreatedDesc(itemsId).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();

        return ownerItems.stream().map(i -> {
            BookingShortDto last = bookingRepository
                    .findTop1ByItemIdAndStartDateBeforeAndStatusOrderByStartDateDesc(i.getId(), now, BookingStatus.APPROVED)
                    .map(b -> BookingShortDto.builder()
                            .id(b.getId())
                            .bookerId(b.getBooker().getId())
                            .build())
                    .orElse(null);

            BookingShortDto next = bookingRepository
                    .findTop1ByItemIdAndStartDateAfterAndStatusOrderByStartDateAsc(i.getId(), now, BookingStatus.APPROVED)
                    .map(b -> BookingShortDto.builder()
                            .id(b.getId())
                            .bookerId(b.getBooker().getId())
                            .build())
                    .orElse(null);

            List<CommentDto> commentsDto = commentsByItemId.getOrDefault(i.getId(), List.of()).stream()
                    .map(CommentMapper::toCommentDto)
                    .toList();

            return ItemMapper.toItemDto(i, last, next, commentsDto);
        }).toList();
    }


    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public void removeItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, CommentCreateDto commentDto, Long itemId) {
                if (commentDto == null || commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Optional<User> userGet = userRepository.findById(userId);
        if (userGet.isEmpty()) {
            throw new NotFoundException(String.format("Пользователя с ID %s не существует", userId));
        }
        User user = userGet.get();

        Optional<Item> itemGet = itemRepository.findById(itemId);
        if (itemGet.isEmpty()) {
            throw new NotFoundException("Предмета с ID " + itemId + " не существует");
        }
        Item item = itemGet.get();

        LocalDateTime now = LocalDateTime.now();

        // Проверяем есть ли завершённое APPROVED бронирование
        boolean allowed = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndDateBefore(
                userId, itemId, BookingStatus.APPROVED, now);

        if (!allowed) {
            throw new ValidationException(
                    "У пользователя должно быть завершённое (APPROVED) бронирование предмета для возможности комментирования"
            );
        }
        Comment comm = Comment.builder()
                .text(commentDto.getText())
                .created(now)
                .item(item)
                .author(user)
                .build();
        return CommentMapper.toCommentDto(commentRepository.save(comm));
    }
}

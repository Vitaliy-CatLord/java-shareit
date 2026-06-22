package ru.practicum.shareit.booking.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.user.models.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "start_date", nullable = false)
    LocalDateTime startDate;
    @Column(name = "end_date", nullable = false)
    LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    Item item;
    @ManyToOne
    @JoinColumn(name = "booker_id", nullable = false)
    User booker;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    BookingStatus status;
}
